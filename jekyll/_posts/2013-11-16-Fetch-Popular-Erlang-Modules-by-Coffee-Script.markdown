---
layout: post
title: Fetch Popular Erlang Modules by Coffee Script
tags: [erlang, coffeescript, node.js]
index: ['/Projects', '/Computer Science/Programming Language/Erlang']
---

These days I write [a small app](https://github.com/wb14123/erlang_module) which fetches the popular erlang modules. It is inspired by [nodejsmodules.org](http://nodejsmodules.org). But it is simpler. Only fetch the dependencies of a repo.

How It Works?
-------------------------

1. Fetch top 1000 stared Erlang repos form Github's search API.
2. Fetch `rebar.config` of the repo from `https://raw.github.com/<repo's full name>`.
3. Use regex to get the repo's dependencies.
4. Sort the repos by how many repos are depend on it.

Keep It Simple and Stupid
------------------------

As its function is simple, so I keep the code simple.

* The app is written by coffeescript, which is perfect to parse Github's restful API and JSON.
* No database is required. Just use some arrays and objects to store informations.
* It generates static html files with Jade. So it is faster to visit and easier to deploy.

Develop: Bitbucket VS Github
------------------------

I think [Bitbucket](http://bitbucket.org) is wonderful to use:

* Support private repositories for free.
* Good diff with side-by-side support.

As a developer. I prefer bitbucket. But if I want to share my code with others, I'd rather to use Github. So I mainly use Bitbucket and its issue tracker. And then sync it to Github.

Deploy: From Appfog to Heroku
-----------------------------

There are many free node.js hosters. [Heroku](http://heroku.com) is a very famous one. [Appfog](http://appfog.com) is a good one to use, too. It supports some services for free without credit cards. I used it to deploy some node.js apps. But it does not support custom domain names for free, so I use Heroku at last.

Heroku is very easy to use. If you want to deploy new version, just push to the Heroku remote. If you want to roll back to some version, just click in the web page. I use the Heroku CLI which is installed from Archlinux AUR's `heroku-toolbelt` package.

There are some tricks:

* Use environment variables to config secret things such as database password, OAuth client secret and so on.
* Use Express as the static file server.
* Set timeout to update every day.
* Use redirect to force the user to visit through my custom domain name.
