#!/bin/bash

if [ $# -lt 1 ]
then
    echo "newpost: You need to specify the post name."
    exit 1
fi

POST_NAME=$1
POST_DATE=`date +%Y-%m-%d`
POST_DIR="./jekyll/_posts"
FILE_NAME=`echo "$POST_DIR/$POST_DATE-$POST_NAME.markdown" | sed "s/ /-/g"`
if [ -e $FILE_NAME ] ; then
	echo "File $FILE_NAME already exists"
	exit 1
fi

echo "---
layout: post
title: $POST_NAME
tags: []
---
" > $FILE_NAME

vim -c "set spell" $FILE_NAME

