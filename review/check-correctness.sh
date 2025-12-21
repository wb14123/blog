#!/bin/bash

set -e
set -x

file=$1

claude "Review @$file based on the review guide @review/correctness-review.md".
