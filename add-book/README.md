# Add Book

A Scala-based tool to add books to the blog from different sources. Current it support:

* Goodreads
* Douban (豆瓣)

## Prerequisites

- `sbt` (Scala Build Tool)
- Java >= 21

## Usage

From the blog root directory:

```
./add-book/add-book.sh <book_url> jekyll/_books
```

The script will:
- Automatically build the project if needed (creates a fat JAR)
- Fetch book information from the provided Goodreads URL
- Create a new book entry in the specified directory

## Development

To manually build:

```
sbt assembly
```

This creates a fat JAR at `target/scala-2.13/add-book.jar`.
