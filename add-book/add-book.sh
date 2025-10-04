#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ORIGINAL_DIR="$(pwd)"

cd "$SCRIPT_DIR"

# Build fat JAR if it doesn't exist or if source is newer
if [ ! -f target/scala-2.13/add-book.jar ] || [ src/main/scala/AddBook.scala -nt target/scala-2.13/add-book.jar ]; then
    echo "Building project..."
    sbt assembly
fi

# Return to original directory and run the JAR
cd "$ORIGINAL_DIR"
java -jar "$SCRIPT_DIR/target/scala-2.13/add-book.jar" "$@"
