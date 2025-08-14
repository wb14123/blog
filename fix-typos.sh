#!/bin/bash

set -e

START_FILE="$1"

for f in `ls -r jekyll/_posts` ; do
	if [ -n "$START_FILE" ] && [[ "$f" > "$START_FILE" ]]; then
		echo "Skip $f since it's not after $START_FILE"
		continue
	fi

	p="jekyll/_posts/$f"
	echo "Fixing $f ..."
	claude -p "Fix typos in @$p . Never change the file name, links in the article. Skip and exit if it's not an English article."
done
