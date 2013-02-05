#!/bin/bash

COMMENT=$1

BASE_DIR=`pwd`
J_DIR="./jekyll"
GIT_DIR="./wb14123.github.com"

push() {
	git add -A .
	git commit -m "$COMMENT"
	git push origin master
}

echo "Jekyll generate site..."
cd $J_DIR
jekyll

echo "Upload site..."
cd $BASE_DIR
cd $GIT_DIR
git checkout master
cd $BASE_DIR
rm -r $GIT_DIR/*
cp -r $J_DIR/_site/* $GIT_DIR/
cd $GIT_DIR
push

echo "Upload blog source code..."
git submodule update
cd $BASE_DIR
push

exit $?
