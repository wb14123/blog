---
layout: post
title: RESTful API with Type System
tags: [web, langauge]
---

Long long ago, some programming languages already have type system. They can find some bugs while compiling the code, instead of let the program run and fail. Though there are many popular languages that do not have static type system, type system has its advantage and has been accepted widely.

Sometimes ago, RESTful API becomes popular. It is a very good way to communication between programs and systems that distributed around the world. It usually uses HTTP because of its simplicity. But HTTP cannot encode type information into it. For example, in a GET request: `http://www.google.com/?query=1&page=2`, `2` should be an integer, but we can not ensure the client always pass a validate integer, so we must check it. It is annoying and DRY (Do Repeat Yourself).

The type system becomes more important here. In programming, we write the code by ourself. So if we are clever and careful enough, we could ensure not pass the invalidate values to the functions. But while we are dealing with the outside world, we must set a guard for it. If we can do it automatic, that would be a type system for the RESTful API.

How to do it? We can provide the type information for the web framework. If the values in the HTTP request is invalidate, just return errors to the client. Not only the language has type system could be able to do it, any language could, as long as it is flexible enough.

How to make the web framework know the type information? There are these ways in my head for now:

0. Encoding the message in protocols with type information.

	Such as Google Protocol Buffer. But it make things complex and is not in the scope of this article.

1. Just write the information in the handler functions. The framework uses reflect to get the information.

	This is the most friendly way for the user. But may be too hack for some languages. And if the language do not have type system, we can not do it. I expected to find it in Haskell frameworks, but I found it seems that Haskell do not support reflect very well. And it seems that Play 2 supports it. A Go framework Revel also supports it.

2. Write modifiers for functions.

	This is a good way, too. It also needs the language feature to support it. As far as I know, some Java frameworks use this way.

3. Write macros.

	This need the language support macro. It is a way to extend the language so that it can suit our needs. I think Elixir could be able to do it.

4. Write type informations in config files.

	We can use XML files for example. But I don't like this way. Many Java frameworks uses XML as config files and it is approved to make people headache.

5. Just provide library functions.

	Just privde some functions such as `parseInt`. If error occurs while parsing, return it to client for you. It is the most normal way. But you need remember to use it.
