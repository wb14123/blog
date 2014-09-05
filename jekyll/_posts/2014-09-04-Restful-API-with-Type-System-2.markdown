---
layout: post
title: Restful API With Type System (2)
tags: [web, language]
---

Some days ago, I wrote [an article about Restful API combined with type system](/2014-08-02-RESTful-API-with-Type-System.html). The main idea is, we should do some limitations on the handler input arguments. It is not just the type, it also should include some other validations, such as if it is could be null, if it should be in some range, and so on. And more, it could make parse variables from HTTP request easy.

In this article, I will continue to talk about how to do this.

I've listed some ways to do this:

1. Encoding the type message in the HTTP body. It is too complex and is not good.
2. Just write the function, use reflect to get the information. But you could only get the type limitation in this way, not others informations such as nullable or range limitation.
3. Write type information in configuration. It is complex and difficult to maintain.

Those are the bad ways. I will introduce some good ways:

1. Write modifiers for functions. It's should be called annotations in Java and decorators in Python. Annotations could be used on arguments and class fields instead of just used on functions or class. So I will use Java as an example.
2. Write macro. Lisp could do this. But I'd prefer Elixir because it's syntax seems more "normal".
3. Just write normal functions. Put them in library and use when needed.

These ways are not just useful to validate and parse params. They could also do things like router mapping, injection and so on. We will see examples of how things are done for each good way.

Use Annotations In Java
------------

Annotation is the my favorite feature in Java. It makes codes clean and simple. The framework could use reflect to get the information about annotations and do the logic things for us.

Annotations are heavily used in Java. They are much cleaner than XML configuration files. Java already has a standard that uses annotations in RESTFul web service. That is [JAX-RS](http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services#Specification).

For example, here is a snap of code (using [Jersey](https://jersey.java.net/)):

```
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
}
```

Use Macros
------------
