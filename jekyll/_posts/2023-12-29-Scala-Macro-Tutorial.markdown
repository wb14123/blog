---
layout: post
title: Scala 2 Macro Tutorial
tags: [Scala, macro, meta programming, AOP]
index: ['/Computer Science/Programming Language/Scala']
---

Macros are powerful but complex. Especially when the language itself like Scala is already complex. The lack of learning resource and documents makes it more so. In this article, I'll write down some of my learnings and hopefully it can help someone else who is new to it as well. I'll keep the examples small and simple so it's easier to understand. Since I'm still learning it, I may continue to update this article on the way, or write a new article if there is a big topic. Either way, I'll make notes here so you know there are updates.

Scala's macro syntax and APIs can be different from version to version. Especially it's almost completely redesigned in Scala 3. This article only targets Scala 2 and I've only tested the examples on Scala 2.13.

## 1. What is Macro

The basic idea of macro is to modify the code with code. For example, let's imagine a macro `plusToMinus` that modifies all the plus operations of integers to minus:

```scala
plusToMinus { 1 + 1 }
```

This will be compiled to `1 - 1` and ends up as `0`.

Of cause this is not a practical example and not all the languages' macro system can do it. But this demonstrate what macros can do where normal code cannot. Here is a more practical example: when we log something in different log levels, the API usually looks like this:

```scala
val v = ...
logger.info(s"This is a info log. Value: $v")
logger.warn(s"This is a warning. Value: $v")
```

However, with this kind of interface, the string `s"..."` need to be computed before passed in to the method, which is a waste since not all the strings need to be logged based on the log level configuration. Especially when `v.toString` needs a lot of resource to compute. So in language like Java, the values are usually passed in as separate parameters:

```java
String v = ...
logger.info("This is a info log. Value: {}", v);
logger.warn("This is a warning. Value: {}", v);
```

Even though it resolves the problem, the interface is kind of awful. And not all the users know this kind of details so they may still just construct the string directly instead of pass in separate parameters. However, with the help of macros, you can still keep the logger interface in the intuitive way. As macros, `logger.info` and `logger.warn` can modify the code directly during the compile time. For example, it can modify the code like this:

From

```scala
logger.info(s"This is a info log. Value: $v")
```

To

```scala
if (loggerLevel >= INFO) {
  println(s"This is a info log. Value: $v")
}
```

So that the actually string computation is not done unless log level is configured to print it.

## 2. How to Write a Macro

Different languages have different syntaxes to write a macro. On the simpler side, macros in C can only do text substitution. On the powerful side, Lisp languages can modify the AST (abstract syntax tree) very easily because the code itself is written as a tree structure. The macro in Scala is on the powerful side since it is able to modify the AST even though it may not be as intuitive as Lisp. There are multiple ways to do it. But essentially, the process it to take the current AST as input and output a new AST. The APIs of reading AST input is very similar to reflection APIs (and in fact, sometimes they share some APIs). Generating a new AST part is more complex. In the following sections, we will walk through how to setup a SBT project to write macros, how to read an AST and how to generate a new AST.

## 3. Project Setup with SBT

In Scala, the implementation of macros and the use of macros need to be compiled separately. So if you are using SBT, they need to be in different sub projects. Here is an example of `build.sbt`:

```scala
lazy val root = (project in file("."))
  .aggregate(core, coretest
  .settings(
    name := "archmage"
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % "2.13.12",
      "co.fs2" %% "fs2-core" % "3.9.3",
    )
  )

lazy val coretest = (project in file("coretest"))
  .settings(
    name := "core-test"
  ) dependsOn core
```

It creates two sub projects. You can implement the macros in `core` and use them in `coretest`.

If you want to debug the generated code from macros, add debug flags to Scala like this in `build.sbt`:

```scala
ThisBuild / scalacOptions += "-Ymacro-debug-lite"
```

## 4. How to Read AST

### 4.1 Read macro parameters:

Here is the basic syntax of a macro. First, define a macro implementation:

```scala
def macroImpl(c: blackbox.Context)(s: c.Expr[String]) : c.Expr[String] = {
  println(s.tree.symbol.fullName)
  s
}
```

The first parameter `c: blackbox.Context` is a must have for a macro implementation. There is also a `whitebox.Context` but we will not cover it in this article. More details about whitebox can be found in [the official document](https://docs.scala-lang.org/overviews/macros/blackbox-whitebox.html).

The remaining parameters of the implementation method are parameters for the macro. For example, if you want to take a parameter of type `String` for the macro, then the implementation of macro will take `c.Expr[String]` as a parameter, which `c.Expr[String]` is the tree representation of the macro's `String` parameter. The same applies to the return type of the macro. You can also  use `c.Tree` instead of `c.Expr[T]`. They can be converted between each other, which we will see in section 4.4.

This example prints out the variable name of the passed in parameter and return the parameter without modification. Note that the printing happens at compile time since that's when the implementation of the macro is ran. Only the returned tree or `c.Expr` is used at run time. So this macro is not doing anything useful, it's just a demo of how to read the input tree.

Once we have the macro implementation, we can define the macro like this:

```scala
def macroTest(arg: String): String = macro macroImpl
```

Then we can use it in another (sub) project so that the compilation is separated:

```scala
val a = "abc"
macroTest(a)
```

It will print out the full name of `a` like this during compilation:

```
me.binwang.archmage.coretest.MethodMetaTest.a
```

The API of `c.Expr` is very similar as reflection API. You can experiment with it by print out different things from it and see what you can get.


### 4.2 Read type parameters:

Macro can also take generic type as parameters. The example below takes a parameter of any type and print out its type at compile time.

```scala
def macroImpl[T: c.WeakTypeTag](c: blackbox.Context)(s: c.Expr[T]) : c.Expr[T] = {
  println(c.weakTypeOf[T])
  s
}

def macroTest[T](s: T): T = macro macroImpl[T]
```

Which can be used like this:

```scala
macroTest("abc")
macroTest(1)
```

The output during compilation will be:

```
String
Int
```


### 4.3 Read implicit parameters:

Macro can have implicit parameters, but the macro implementation shouldn't define them as implicit. Otherwise Scala compiler will give confusing errors. See [this issue](https://github.com/scala/bug/issues/6494) for more details.

In the following example, `macroTest` takes an implicit double variable and return it as the new generated tree:

```scala
def macroImpl(c: blackbox.Context)(s: c.Expr[String])(num: c.Expr[Double]) : c.Expr[Double] = {
  println(s"Name of implicit num: ${num.tree.symbol.fullName}")
  num
}

def macroTest(s: String)(implicit num: Double): Double = macro macroImpl
```

Note how `num` in `macroImpl` doesn't have any `implicit` definition.

Then the test code:

```scala
implicit val num: Double = 1.1
println(macroTest("abc"))
```

It will print this during the compile time:

```
Name of implicit num: me.binwang.archmage.coretest.MethodMetaTest.num
```

And this during the run time:

```
1.1
```


### 4.4 Read code block with by-name parameter

Macros can also take [by-name parameter](https://docs.scala-lang.org/tour/by-name-parameters.html). However, it needs to use `c.Tree` instead of `c.Expr` as parameter in the macro implementation:

```scala
def macroImpl(c: blackbox.Context)(s: c.Tree) : c.Expr[String] = {
  println(s)
  c.Expr[String](s)
}

def macroTest(s: => String): String = macro macroImpl
```

See how `c.Tree` is converted to `c.Expr`. You can also convert `c.Expr` to `c.Tree` by using the `.tree` method, which we've seen in the examples above.

Test it with this code:

```scala
macroTest {
  val a = "a"
  val b = "b"
  println("hello!")
  a + b
}
```

It will print out this during compile time:

```
{
  val a: String = "a";
  val b: String = "b";
  scala.Predef.println("hello!");
  a.+(b)
}
```

### 4.5 Use Quasiquotes

[Quasiquotes](https://docs.scala-lang.org/overviews/quasiquotes/intro.html), or `q"..."`, is a very powerful syntax for Scala macro. It can both match a tree and generate a tree. For example, the following code can match different parts of a if else clause to `c.Tree` variables:

```scala
def macroImpl(c: blackbox.Context)(s: c.Tree): c.Tree = {
  import c.universe._
  val q"if ($cond) $thenp else $elsep" = s
  println(cond)
  println(thenp)
  println(elsep)
  q"$cond"
}

def macroTest(s: => Any): Any = macro macroImpl
```

`cond`, `thenp` and `elsep` are all matched parts from the input tree.

`q"$cond"` generates a new tree using the matched condition part of the tree. We will see more details in how to use quasiquotes to generate trees in section 5.4.

Test it with this code:

```scala
val bigNum = 2
val smallNum = 1
val result = macroTest {
  if (bigNum > smallNum) {
    "no surprise"
  } else {
    "surprise!"
  }
}
println(result)
```

During the compile time, it will print out the different parts of the tree that we have asked it to match:

```
bigNum.>(smallNum)
"no surprise"
"surprise!"
```

And during the run time, it will print out the value of condition instead of either if or else clause:

```
true
```

More examples about how to match the tree can be found in [the document](https://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html). Click on each example to see more details.

## 5. How to Generate Tree

### 5.1 Construct Tree Directly with API

An AST can be constructed from the classes that represent the tree. For example, a constant of string can be created by `Literal(Constant("I replaced you!"))`. The following example replace any string to `I replaced you`:

```scala
def macroImpl(c: blackbox.Context)(s: c.Expr[String]) : c.Expr[String] = {
  import c.universe._
  c.Expr[String](Literal(Constant("I replaced you!")))
}

def macroTest(s: String): String = macro macroImpl
```

With the code below, it will print `I replaced you!` instead of `abc`:

```scala
println(macroTest("abc"))
```

This is a very simple example. When the tree becomes larger and larger , it's more and more difficult to construct a tree with this approach. It's like a much worse version of lisp. So in the following sections, we will see some easier ways to construct a tree.

### 5.2 Use `c.parse`:

`c.parse` can parse a string as Scala code and generate an AST. For example, the following macro returns the variable name of a `String`:

```scala
def macroImpl(c: blackbox.Context)(s: c.Expr[String]): c.Expr[String] = {
  val name = s.tree.symbol.fullName
  c.Expr(c.parse(s""" "Name of var is: $name" """))
}

def macroTest(s: String): String = macro macroImpl
```

Then use it like this:

```scala
val a = "abc"
println(macroTest(a))
```

It will print out:

```
Name of var is: me.binwang.archmage.coretest.MethodMetaTest.a
```

Note the output is at run time instead of compile time like the examples in the last section, because we've replaced the tree with new code.

### 5.3 Use `reify`

`c.parse` is easy to use and understand. But when generating more and more code with it, it can be pretty messy since it is just a string. There is no syntax checks in IDE. Even worse, you cannot get any run time information to use in the generated tree.

`reify` is a much better option. You can write code as usual. The code in `reify` block is the code that will be generated. You can refer to another `Expr` (in the old tree) by using its `.splice` method. Here is an example to print out both the variable name and it's value:

```scala
def macroImpl(c: blackbox.Context)(s: c.Expr[String]): c.Expr[String] = {
  import c.universe._
  val name = c.Expr(c.parse("\"" + s.tree.symbol.fullName + "\""))
  reify {
    s"${name.splice}: ${s.splice}"
  }
}
```

`macroTest` and the test code is the same above. Running the test code will get output like this:

```
me.binwang.archmage.coretest.MethodMetaTest.a: abc
```

### 5.4 Use Quasiquotes

As we've seen in section 4.5, `q"..."` can be used to match a tree. It can be used to generate a tree as well. For example, in the following code:

```scala
def macroImpl(c: blackbox.Context)(s: c.Tree) = {
  import c.universe._
  val q"if ($cond) $thenp else $elsep" = s
  q"if ($cond) $elsep else $thenp"
}

def macroTest[T](s: T): T = macro macroImpl
```

It uses the parts that have been matched by `q"..."` and generates a new tree using those parts. It swaps the if and else clause. Run it with this test code:

```scala
macroTest(if (true) println("a") else println("b"))
```

It will print `b` instead of `a`.



### 5.5 Avoid Name Conflict

When generating a new tree, we may generate some variables that have conflict names with the existing ones. Use `c.freshName` to get a unique name to avoid the conflict.

### 5.6 Type Checked and Unchecked Tree

There are two kinds of AST in Scala's internal compiler: type checked and unchecked. See more details in [this Stack Overflow answer](https://stackoverflow.com/questions/20936509/scala-macros-what-is-the-difference-between-typed-aka-typechecked-and-untyped). Some APIs can only accept either type checked or unchecked tree. And sometimes the compiler throws out weird errors if using the wrong type of tree. If that's the case, try to use `c.untypecheck` and `c.typecheck` to covert trees.

For example, here is some code that cannot be compiled:

```scala
def macroImpl(c: blackbox.Context)(blockTree: c.Tree) : c.Expr[Seq[String]] = {
  import c.universe._
  val block = c.Expr[Seq[String]](blockTree)
  reify {
    Seq("a").flatMap{_ => block.splice}
  }
}

def macroTest(blockTree: => Seq[String]): Seq[String] = macro macroImpl

// Testing code in another sub project:
val s = "abc"
macroTest {
  val a = s
  Seq(a)
}
```

The compiler will throw error:

```
[error] Error while emitting XXX.scala
[error] value a
[error] one error found
```

To fix this, we need to convert `blockTree` to unchecked tree:

```scala
def macroImpl(c: blackbox.Context)(blockTree: c.Tree) : c.Expr[Seq[String]] = {
  import c.universe._
  val cleanedBlock = c.untypecheck(blockTree.duplicate)
  val block = c.Expr[Seq[String]](cleanedBlock)
  reify {
    Seq("a").flatMap{_ => block.splice}
  }
}
```
