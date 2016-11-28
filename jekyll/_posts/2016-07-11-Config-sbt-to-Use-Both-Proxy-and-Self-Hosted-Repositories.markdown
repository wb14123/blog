---
layout: post
title: Config sbt to Use Both Proxy and Self Hosted Repositories
tags: [sbt, scala]
---

While building Scala projects, we usually use a proxy to make the build faster. On the other hand, we usually use another repository to host our internal dependencies, which usually has a password to protect it from unwanted access. Both things are good and necessary. But if you want to use both of them, you will find it's very tricky.

Use Proxy Repositories
-------

sbt has a [document](http://www.scala-sbt.org/0.13/docs/Proxy-Repositories.html) that described how to set proxy repositories:

### Config repositories in `~/.sbt/repositories` like this:

```
[repositories]
  local
    my-ivy-proxy-releases: http://repo.company.com/ivy-releases/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
    my-maven-proxy-releases: http://repo.company.com/maven-releases/
```

### Add `-Dsbt.override.build.repos=true` while use sbt command.

The second step will override all the resolvers defined in your project, like in the file `build.sbt`. It is necessary because if you don't do this, sbt will still send requests to default repos like typesafe and scala-sbt.


Use Self Hosted Repositories
---------

sbt has a [document](http://www.scala-sbt.org/0.13/docs/Resolvers.html) about this, too. You will add something like this in your `build.sbt`:

```
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```

And if you have a password for this repo, you should also add something like this:

```
credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org",
  "username", "password")
```

Use Both
-------

You may see the problem to use both of them. In order to force sbt to use proxy repos, you should use the option `-Dsbt.override.build.repos=true`, which will override your self hosted repo written in `build.sbt`.

After tried some methods, I find I can just write the repos defined in `build.sbt` into `~/.sbt/repositories`, and sbt will still be able to find the credentials for it while building the project.

This method will make sbt print some error logs while loading the project: sbt will attempt to download some dependencies from this repo but cannot find the credential since it hasn't loaded the project yet. If you are comfortable to ignore the error log, this method would be fine.
