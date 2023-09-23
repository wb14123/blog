---
layout: post
title: Migrate Scala2grpc to Cats Effect 3
tags: [Programming, Scala, gRPC, Cats, Functional Programming]
index: ['/Projects/Scala2grpc', '/Computer Science/Programming Language/Scala']
---

[Scala2grpc](https://github.com/wb14123/scala2grpc) is a library and SBT plugin I wrote so that you can integrate gRPC to Scala code in a non-invasive way. In a previous [blog post](/2022-05-02-A-Library-to-Make-It-Easier-to-Use-Scala-with-GRPC.html), I talked about the motivation behind it.

The library requires each service method to return Cats Effect's `IO` or fs2 stream. However, it's still using Cats Effect 2.x version. There are big changes in Cats Effect 3 and almost all up to date libraries already support it. So it's time to migrate it to Cats Effect 3 as well.

## Replace Akka gRPC with fs2-grpc

This library was using [akka-grpc](https://doc.akka.io/docs/akka-grpc/current/index.html). But I want to replace it for a few reasons:

* It uses [streamz](https://github.com/krasserm/streamz) to convert between Akka streams and fs2 streams. This library doesn't support Cats Effect 3 and hasn't been updated for a while. This is the biggest reason that I need to migrate from akka immediately.
* Akka changed its open source license to a very expensive one which I didn't know at the time writing this library. Even the license doesn't cost anything if the revenue doesn't reach a certain point, I don't want it to be a liability.
* It's good to have a gRPC library that supports for Cats Effect and fs2 streams natively.

So in the newer version, I replaced Akka gRPC with [fs2-grpc](https://github.com/typelevel/fs2-grpc), a library under Typelevel umbrella and natively supports Cats Effect and fs2. The document is not as good and I spent quite some time to figure out how to actually use it, but I'm happy it finally worked out.

## Add Hooks to gRPC Calls

In the previous version of Scala2grpc, there is a feature to log every request. But it is a pretty hacky implementation: I just throw the logging logic into the generated code. Since this version is a breaking change, it's a good opportunity to revisit the approach and see how to add generic hooks before and after each gRPC calls.

My implementation wraps the gRPC response into a context in the generated code. The context includes the response with type of `IO` or `fs2.Stream`. Because of the referential transparency, you can take the response and add hooks before or after it. More detailed document is in [this section of readme](https://github.com/wb14123/scala2grpc#4-optional-define-custom-grpc-hook).

## Non-invasive Nature of the Library

The migration brings lots of breaking changes, so it is a good test for the non-invasive nature of the library. I have 2 side projects that are using this library and I migrated one of them recently. Since it's also using Cats Effect, there are some migration steps unrelated to this library. But regarding of the related parts, the migration process is very smooth: I don't need to change any implementation code of the services. The generated gPRC protocol files are also not changed either. The only thing I need to change is the single object that implements `GRPCGenerator` to [pass in some new parameters](https://github.com/wb14123/scala2grpc#2-create-an-object-to-implement-grpcgenerator). It is impossible if I used akka gRPC directly and changed it to fs2-grpc since their interface are all different.

I'm so happy with the result: it adds gRPC to pure Scala code so easily without ever touch it. It saves me so much time to build a service and keeps the code clean at the same time. It continue to be a must have for my future Scala gRPC projects.
