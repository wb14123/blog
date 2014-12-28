---
layout: post
title: Why Use Reflections to Write A Web Framework
tags: [web, programming]
---

Lots of people don't like reflections. And I said if I could see the whole things I might not use reflections (with Annotation) to write the web framework [Bard](https://github.com/wb14123/bard). But I think it is not true, I really need reflections to do the things.


What to Do?
--------------

So what we need to do? There are two goals:

1. Auto generate API documents for the web service.
2. Easy to use.

Let me explain: Once the web service is written, the framework should be able to auto generate the API documents for you. Because there is the code, which means there are all the necessary informations.

The second one is a little more complex. We will see the details later.


Things Are About Injectors
---------------

The thing that cannot do without reflections is injectors. (Which should satisfied the two goals above).

Let's see a common usage of injectors. I'd like to get a URL's query string as integer in a handler. The framework should know this in order to generate documents.


### The First Way

The first way to this is write the injectors in the framework information. For example:

```
framework.addInjector(handler, getQueryString("a", Integer.class))
```

In this way, the framework knows the information. But it is not easy to use this framework. The problem is how to write the handler? How do we store the integer we've just get? The general way is store it into the context and takes it from context in the handler. It comes with two problems:

1. We need a key to identify the value in the context. We need remember this key in order to use it and not duplicate with other values' key.

2. We need write at least one line of code in the handler, too.

### The Second Way

The second way is just get the query string in to handler's code. For example:


```
void handler(context) {
	Integer a = getQueryString(context, "a", Integer.class)

	# then do other things:

	...

}
```

This is very straightfoward. But in this way, we cannot get the information from the framework: the framework has no way to know this API needs a parameter "a" typed int in URL query string.


### The Reflections

Things get easy when we use reflections with annotations. We can write the handler like this:

```
void handler(@QueryParam("a") int a) {
	# do things in the handler:

	...


}
```

The framework knows what parameter this API needs. And the user know how to use the injected value. We are all happy now.

So stop hate reflections, they are working sometimes.

