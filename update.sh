#!/bin/bash

BASE_DIR=`pwd`
J_DIR="./jekyll"
GIT_DIR="./wb14123.github.com"

push() {
	git add -A .
	git commit -m "Changed at $(date)"
	git push origin master
}

echo "Jekyll generate site..."
cd $J_DIR
jekyll

echo "Upload blog source code..."
cd $BASE_DIR
push

echo "Upload site..."
rm -r $GIT_DIR/*
cp -r $J_DIR/_site/* $GIT_DIR/
cd $GIT_DIR
push

exit $?
