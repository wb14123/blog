---
layout: post
title: RESTful API with Type System
tags: [web, language]
index: ['/Computer Science/Programming Language/Type System']
---

Long long ago, some programming languages already had type systems. They can find some bugs while compiling the code, instead of letting the program run and fail. Though there are many popular languages that do not have static type systems, type systems have their advantages and have been accepted widely.

Some time ago, RESTful API became popular. It is a very good way to communicate between programs and systems that are distributed around the world. It usually uses HTTP because of its simplicity. But HTTP cannot encode type information into it. For example, in a GET request: `http://www.google.com/?query=1&page=2`, `2` should be an integer, but we cannot ensure the client always passes a valid integer, so we must check it. It is annoying and violates DRY (Don't Repeat Yourself).

The type system becomes more important here. In programming, we write the code ourselves. So if we are clever and careful enough, we could ensure not to pass invalid values to the functions. But while we are dealing with the outside world, we must set a guard for it. If we can do it automatically, that would be a type system for the RESTful API.

How to do it? We can provide the type information for the web framework. If the values in the HTTP request are invalid, just return errors to the client. Not only languages that have type systems could be able to do it, any language could, as long as it is flexible enough.

How to make the web framework know the type information? There are these ways in my head for now:

0. Encoding the message in protocols with type information.

	Such as Google Protocol Buffer. But it makes things complex and is not in the scope of this article.

1. Just write the information in the handler functions. The framework uses reflect to get the information.

	This is the most friendly way for the user. But may be too hacky for some languages. And if the language does not have a type system, we cannot do it. I expected to find it in Haskell frameworks, but I found it seems that Haskell does not support reflection very well. And it seems that Play 2 supports it. A Go framework Revel also supports it.

2. Write modifiers for functions.

	This is a good way, too. It also needs the language feature to support it. As far as I know, some Java frameworks use this way.

3. Write macros.

	This needs the language to support macros. It is a way to extend the language so that it can suit our needs. I think Elixir could be able to do it.

4. Write type information in config files.

	We can use XML files for example. But I don't like this way. Many Java frameworks use XML as config files and it is proven to give people headaches.

5. Just provide library functions.

	Just provide some functions such as `parseInt`. If error occurs while parsing, return it to client for you. It is the most normal way. But you need to remember to use it.
