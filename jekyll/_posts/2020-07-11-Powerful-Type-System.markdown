---
layout: post
title: Powerful Type System
tags: [technology, programming language, type system, machine learning]
index: ['/Computer Science/Programming Language/Type System']
---

This is not an article to introduce type systems. It's only some of my experience and thoughts about it.

## Basic Type System

When I first learned programming, I was very interested in languages with dynamic type system like Javascript and Python. It seems so straight forward. The need of annotate types on variables and methods seems so annoying.

But as I wrote more programs, the more I feel the power of type system. Basically there are some reasons why type system is useful:

1. It serves as document. So when you read the code which is wrote by other people (or by yourself a long time ago), you can understand the methods and objects more easily.
2. It can prevent some basic errors. For example, some code change the type of variables on the fly and you don't know about it. Or you accidentally assign a wrong type of value to it. Or just a typo in the field name. The type system can find a lot of these errors at compile time.
3. The tools can be powerful with type system. With type system, it's so pleasant to write code with modern IDEs. The IDE can complete the field names for you. It can jump to the definitions of the methods or objects very easily. It can show all the usages of the methods. I'm not saying it cannot be done with dynamic type system, it's just more difficult and inefficient. For example, when I open Intellij with a reasonable large Javascript project, the fans will run like crazy.

All these advantages are well known by most programmers now. And it's a trend to use type system more often. For example, Python already added the type hint so you can optional write type annotations and use some tools to check the code. So I will not go any deeper about the basic type system.

## Dependent Type System

I was missing type system so much when I wrote machine learning program with TensorFlow. With TensorFlow, you need to construct the computing graph with some awful API (it's before the eager execution mode), so it's very easy to get lost what's the type and shape of the current tensor. Though TensorFlow can actually check the correctness of shape when construct the graph, but the error message doesn't help a lot if it has dynamic shapes. It will only report the shape as `?`. So the error messages are like "`?` doesn't match", but you don't know exactly where is `?` in the shapes come from.

So at that time, I thought it would be so nice if I can annotate the shape of the tensors just like the type system. So I can know every tensor's shape when I'm writing the code. The API will also has the more information by default. For example, imagine if we can write the code like this:

```python

def reverseImage(images: Tensor[BatchSize, ImageHeight, ImageWidth]): Tensor[BatchSize, ImageWidth, ImageHeight]:
  pass

```

So that we know the what's the meaning of each of the demission. And if we operate on the return value, we can keep track on the properties like `BatchSize`.

But after a deeper thinking, the type system I knew at that time cannot actually handle the information like shapes. Because the shape of the tensor is dynamic and could be infinity. It's impossible to define a type like `BatchSize`, it's more like a variable.

That was the time I found dependent type system. More specifically, I found a language that has dependent type system called [Idris](https://www.idris-lang.org/). Here is an example of how it works from [its official website](https://www.idris-lang.org/pages/example.html):

```idris
app : Vect n a -> Vect m a -> Vect (n + m) a
app Nil       ys = ys
app (x :: xs) ys = x :: app xs ys
```

The syntax is a little like Haskell, but you get the idea: `Vect` is a type that can have it's own properties. And the return type's property can based on the parameter's type: `n+m`.

## Formal Proof

Such a powerful type system exists surprised me. Even more, Idris introduces how it can proof theorem. Normally, we will write tests for methods. But there is almost no guarantee the methods will work correctly for all cases. For example, if we write a method `plus(a:int, b:int)`, and we want to test if `plus(a, b) == plus(b, a)`. In most languages, we will only find some values for `a` and `b` to see the result. And hopefully we can find some edge cases. But with Idirs, you can actually define a theorem to declare `plus(a, b) == plus(b, a)`, and prove it with the code. If you don't prove it the right way, the compilation will fail. This ability really opens a new world to me.

I find this ability can be very useful in machine learning. Because machine learning involves a lot of math. The model often needs to have some properties to make it work right. For example, maybe you need the function to be differentiable. To write down these theorem and prove it, makes it have the same advantages as type system: a much better documentation and make sure the program is working as intended.

As I explored the formal proof, I find it has a much wider use case than I though. For example, I found [Raft has a formal proof](https://github.com/uwplse/verdi/pull/16). The thing I like about programming is I can always find how it works if I spent enough time: it's all in the code and can only be interpreted in one way. It would be so much fun if a complex theorem in distributed system can be proved in such a way: I can learn the theorem step by step and make sure I understand them correct.

Even though I discovered all these information 2 years ago. Only until recently I have some time to actually learn some formal proof language. There is a very good series of books called [Software Foundations](https://softwarefoundations.cis.upenn.edu/), which introduces Coq, a very popular language to prove theorems. It has many exercises in the book's source code so I can read and modify the code to do exercises. It's really a fun experience. A lot of the concepts are not found in popular languages so it's refreshing and challenging. I'm tracking the progress in [a Git repo](https://softwarefoundations.cis.upenn.edu/) and wish I can finish them all.
