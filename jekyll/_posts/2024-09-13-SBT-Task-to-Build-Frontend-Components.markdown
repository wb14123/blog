---
layout: post
title: SBT Task to Build Frontend Components
tags: [Scala, SBT, Javascript, CSS, frontend, webpack, npm]
index: ['/Computer Science/Programming Language/Scala']
---

Even writing a website using something else than Javascript to render content from server, sometimes it's inevitable to have some Javascript or CSS code. So managing Javascript dependencies and build packages is needed. The easiest way may be just don't use any tool: download all the dependency files into a directory and import them in the html file directly. That's what I was doing for [RSS Bran](https://rssbrain.com) before. But it get messy pretty quickly and it's hard to keep track of the dependencies. So it's time for me to resolve the problem. Since the project is written in Scala, I'll note down how I do it with Scala's build tool SBT.

## Frontend Package Management and Build

I put all the frontend related code into a separate sub-directory and treat it like a frontend project. This makes things much easier and less hacky. I use npm to manage the dependencies and use webpack to build it. Here is a simplified example of the code tree structure from my project [RSS Brain](https://github.com/wb14123/rss_brain_release):


```
▾ js/
  ▾ css/
      google-fonts.css
      main.css
      pico.jade.min.css
  ▾ dist/
      f20305dee9d396fea5c7.ttf
      f5ef242406fdcf40a232.otf
      main.css
      main.js
      main.js.LICENSE.txt
  ▾ fonts/
      google-material-icons-outlined.otf
      google-material-icons.ttf
  ▸ node_modules/
  ▾ src/
      boolean-checkbox.js
      error-handler.js
      global-htmx.js
      index.js
      match-id.js
      popover-menu.js
      register-service-worker.js
      service-worker.js
      set-theme.js
      source-images.js
    package-lock.json
    package.json
    readme.md
    webpack.config.js
▸ project/
▸ src/
  build.sbt
  LICENSE.txt
	readme.md
```

You can see other than the `js` directory, it's a pretty standard structure for a Scala project managed by SBT.

When look into `js` directory, it's a frontend project managed by npm and built with webpack.

`js/src/index.js` bundles all the dependencies in node modules and local files. Here is an example:

```javascript
// css

import 'somment/somment.css';
import 'lite-youtube-embed/src/lite-yt-embed.css';
import 'toastify-js/src/toastify.css';
import '../css/google-fonts.css';
import '../css/pico.jade.min.css';
import '../css/main.css';

// js
import './boolean-checkbox.js';

import 'htmx.org';
import './global-htmx.js';

import Alpine from 'alpinejs';
window.Alpine = Alpine;

import * as FloatingUIDOM from '@floating-ui/dom';
window.FloatingUIDOM = FloatingUIDOM;

import 'lite-youtube-embed';
import '@splidejs/splide';
import Toastify from 'toastify-js';
window.Toastify = Toastify;

import DOMPurify from 'dompurify';
window.DOMPurify = DOMPurify;

import 'imgs-html';
import 'somment';

import './error-handler.js';
import './popover-menu.js';
import './match-id.js';
import './set-theme.js';
import './source-images.js';
import './register-service-worker.js';

Alpine.start();
```

Here is an example of `webpack.config.js`:

```javascript
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = {
  module: {
    rules: [
      {
        // If you enable `experiments.css` or `experiments.futureDefaults`, please uncomment line below
        // type: "javascript/auto",
        test: /\.(sa|sc|c)ss$/i,
        use: [
          MiniCssExtractPlugin.loader,
          "css-loader",
          "postcss-loader",
        ],
      },
    ],
  },
  plugins: [new MiniCssExtractPlugin()],
};
```

Since this is more related to frontend tech and is very basic, I will not go too much into details. But the point is, when run `npx webpack` under `js` directory, it will build bundled files into `js/dist`. We will write a SBT task to trigger this command and copy the dist files into resources to package.

## SBT Task to Trigger Build and Package Dist Files

SBT is very flexible since you can basically write Scala code to define the tasks. Here we define the first task to install npm dependencies and trigger webpack build (in `build.sbt`):

```scala
lazy val webpack = taskKey[Unit]("Run webpack in js directory")
webpack :=  {
  val workDir = new File("./js")
  Process("npm" :: "install" :: Nil, workDir) #&& Process("npx" :: "webpack" :: Nil, workDir) !
}
```

It defines a task called `webpack`, so when you run `sbt webpack`, it will run `npm install && npx webpack` under `js`.

Then we define another task to copy all the dist files to generated resource directory:

```scala
Compile / resourceGenerators += Def.task {
  webpack.value
  val file = (Compile / resourceManaged).value / "webview" / "static" / "dist"
  IO.copyDirectory(new File("./js/dist"), file, overwrite = true)
  IO.listFiles(file).toSeq
}.taskValue
```

Here we added some steps when SBT generate resource files: first we let it run `webpack` task we defined above, then copy all the files under `js/dist` to `webview/static/dist` under generated resources. Here resources means Java resource files, like the files under `src/main/resources`, but auto generated to `target/scala-2.13/resource_managed` and will be packaged together as resource files.

So when you run `sbt package` here, the generated jar package will include all those files as resource files. For example, in my project, the generated jar package have these if you open it with vim (which can view zipped package):

```
81663 webview/static/dist/f20305dee9d396fea5c7.ttf
81664 webview/static/dist/f5ef242406fdcf40a232.otf
81665 webview/static/dist/main.css
81666 webview/static/dist/main.js
81667 webview/static/dist/main.js.LICENSE.txt
```

## Serve Resource Files in Http Server

Now you can serve the files under `webview/static/dist` with your web server. Different web server or framework do it differently. Here is an example of http4s:

```scala
// include the following route into the http4s web server
// IMPORTANT: every resource file under `/webview` will be public accessible
val assetsRoutes = resourceServiceBuilder[IO]("/webview").toRoutes
```

Then you can use them in HTML:

```html
<link rel="stylesheet" href="/static/dist/main.css">
<script src="/static/dist/main.js" defer="defer"></script>
```

