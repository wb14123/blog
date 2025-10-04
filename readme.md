## Install Ruby dependencies

Jekyll needs some ruby dependencies. The best way to install them is through [Bundler](https://bundler.io/). After install Bundler, run the following command to install all dependencies:

```
cd ./jekyll
bundle install
```

## Start local server

Start a local server to preview changes:

```
bundle exec jekyll serve --watch
```

## Create a new blog post

This will create a file under `jekyll/_posts` and fill in basic information like date and layout. The date is set to the current date.

```
./newpost.sh <blog title>
```

## Add A Book as Read

Make sure `sbt` and Java >= 21 is installed.

Then:

```
./add-book/add-book.sh <book_goodreads_url> jekyll/_books
```

## Publish updates

```
./update.sh <commit message>
```

This command will:

* Build static site to `wb14123.github.com` folder.
* Commit changes in `wb14123.github.com`.
* Commit all the changes in both root repo and the `wb14123.github.com` repo.
* Push all changes. It will publish all changes to the blog website.
