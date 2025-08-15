---
layout: post
title: Comment And Search Are Available
categories: workspace
tags: [blog, disqus, google]
index: ['/Projects/Blog']
---

As you can see, my blog is able to comment and search now!

Comment
---

I always think comment is very important for a blog. You can know how helpful it is or if there are some issues in your article. [Disqus](http://disqus.com) is a great tool to put comment on a website. It is neat and powerful as you can see. It may be a little slow in China. But considering my blog is mostly written in English, it is not a big deal.

Search
---

The search engine I am using is [Google custom search engine](http://www.google.com/cse). Isn't it awesome that I have Google's technology on my blog? It is easy to use, too. The downside of this choice is that Google may take some time to put the website's content into its index. And Google is not even available in China sometimes.

Scripts to Make Life Easy
---

As I put the website on [github page](http://pages.github.com/), I don't want to upload the jekyll directory. Just the static site is enough. So I must copy the site to git repository, add, commit and then push it. It is boring work. I write two scripts to make it easy. I don't know if there is some other way to do this, but I'm OK with it.

update.sh: generate static site and upload to github.

    #!/bin/bash

    BASE_DIR=`pwd`
    J_DIR="./jekyll"
    GIT_DIR="./wb14123.github.com"

    cd $J_DIR
    jekyll
    cd $BASE_DIR
    rm -r $GIT_DIR/*
    cp -r $J_DIR/_site/* $GIT_DIR/
    cd $GIT_DIR
    git add -A .
    git commit -m "Changed at $(date)"
    git push origin master
    exit $?

newpost.sh: use vim to open a new post with current time and some meta information.

    #!/bin/bash
    
    if [ $# -lt 1 ]
    then
        echo "newpost: You need to specify the post name."
        exit 1
    fi
    
    POST_NAME=$1
    POST_DATE=`date +%Y-%m-%d`
    POST_DIR="./jekyll/_posts"
    FILE_NAME=`echo "$POST_DIR/$POST_DATE-$POST_NAME.md" | sed "s/ /-/g"`
    
    echo "
    ---
    layout: post
    title: $POST_NAME
    categories: misc
    tags: []
    ---
    " > $FILE_NAME
    
    vim $FILE_NAME

