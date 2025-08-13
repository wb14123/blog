#!/bin/bash

set -e
set -x

for f in `ls -r jekyll/_posts` ; do
	p="jekyll/_posts/$f"
	echo "Fixing $f ..."
	git diff --quiet HEAD -- $p && claude -p "Fix typos in @$p . Never change the file name, links in the article. Skip and exit if it's not an English article."
done
