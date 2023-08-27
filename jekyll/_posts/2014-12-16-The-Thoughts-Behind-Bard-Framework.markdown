---
layout: post
title: The Thoughts Behind Bard Framework
tags: [web, programming]
index: ['/Projects/Bard']
---

These days I'm working on a really exciting project: a web framework. My job is to write web services, and there is no framework I thought is doing things right. So I just write one myself. It is really an interesting thing. I will talk about some thoughts behind it in this article.

What Do We Need
-------------

What we need is really simple. In order to write a web service, we just need three things:

* A router framework to define which function to handle the incoming request.
* A library, which makes it easy to read HTTP request and write HTTP response.
* A mechanism to handle errors. (Which already exists in many programming language).

So the design is very simple. Nothing special, just like the normal programming.


Thoughts About Injectors
--------------

I find injections are everywhere in many web frameworks, but we really do not need it. All the injections could be done by just functions, which could be provided by a library.

For example, let's review two normal usages of injections.

The first one is inject the login user. But you can just write a function that get token or session from HTTP request, and query your storage whether this token/session belongs to any user. No need with injections.

The second one is inject database connection. But you can write a function to get your global database pool and get a connection from it. No need with injections, either.


Annoying While Always Write Some Functions In Many Handlers?
--------------

Sometimes we find, some functions in the library is so common that we almost use them in every handler. For example, if all the handlers need the user to login, we will write a function like `validateLogin(request)` in every handler. In order to resolve this problem, we could pass the function `valudateLogin` to the framework, tell it to run it on every request. This is the design of filter: filters are functions that read request, then write some response for you.

The API to define filters may like this:

```
framework.addFilter([handlerA, handlerB], [filterA, filterB])
```


We Need Injectors While Performance Matters
--------------

When there comes filters, we should use injectors in order to improve performance. For example, in one filter, you query the login user, and in your handler, you query the login user, too. If you store the login user while you first query it, you can query it just one time.

Injectors could be designed to use like filters. But it write things into the context instead of response.

Even at this time, some of the injectors like parse HTTP query string as number or something like that is not necessary.

The API could be as same as filters':

```
framework.addInjector([handlerA, handlerB], [injectorA, injectorB])
```


How to Define The Route
--------------

Just one handler should handle the HTTP request. So how we define the rule? HTTP has defined it in its standard, based on URL, HTTP method and so on. So almost all the framework provides API based on these rules. For example, it provides API to let you specify handler A should handle path '/abc' and method GET, it may looks like this:

```
framework.get('/abc', handlerA)
```

But what if we what some more control over the request? We should be able to custom and extend it. My design is a function called adapter. It is a function that returns a function, which takes HTTP request as parameter and  return a result that tell the framework whether this request suits this adapter. For example, `pathAdapter("/abc")` could return a function, that return true if the HTTP URL is "/abc" otherwise return false.

So the API may looks like this:

```
framework.addAdapter([handlerA], [pathAdapter("/abc"), methodAdapter("GET")])
```


All the Components Should Be Equal
---------------

We can add adapters, filters and injectors on handlers for now. But when we define filters, adapters and injectors, we will find the same problem as when we define handlers. So why not we could also use these middlewares on them? It is very simple, just run the middlewares on it before run it.

For example:

```
framework.addFilter([injectorA], [filterA])
```

`filterA` will be run before we run `injectorA`.

This framework seems very simple yet very powerful for now. Let's sum it up, we have these components:

* Handler: really handle the request.

And these components that could be used on the other components:

* Filter: read request and write response.
* Injector: read request and store some information into the context.
* Adapter: read request and tell the framework whether we should handle it.

Filters and injectors may seem like the same, in fact, they are. So why not merge them into one? I will explain it later.


Get the Meta Information
----------------

Many frameworks doing things well util now. But almost all of them could not get meta informations from it.

Meta informations are very useful. It could be used to auto generate API documents, auto generate client code and so on. In the previous work, the framework could generate a tree with filters, injectors, adapters and handlers. This is the meta information, we could generate everything from it.

In Bard framework, it generates a more friendly structure. There is another function bind on middlewares used to generate the more friendly structure.


Use Annotations Instead of Plain Code
----------------

Use plain code is easy to understand. But use annotations in Java makes it more easy to use. Filters and adapters are annotated on methods, and injectors are annotated on parameters. It is straightforward. This way solve some problems about injectors, for example, where to put the injected variable. In this way, injectors are like filters on parameters.

In fact, I thought it is really cool while I see the annotation way at the first time, like in Jersey and Flask. So I start to write my framework in this way. If I could see the whole picture like today, I'm wondering if I still prefer the annotation approach.


These are not all the thoughts behind it. But something are more general than a web framework, such as handle errors. I will talk about them in other articles.
