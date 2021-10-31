---
layout: post
title: Install BLAS Library for MXNet
tags: [deep learning, programming, mxnet, mac os x]
index: ['/Computer Science/Machine Learning']
---

[MXNet](https://github.com/dmlc/mxnet) is a deep learning library. I read its doc and some of its source code. It looks very good. So I'd like to install and try it. While I'm following the [installing guide](https://mxnet.readthedocs.org/en/latest/build.html#build-mxnet-library) to install it on Mac OS X, it failed to compile with the error `cblas.h` not found. The message pointed out I may miss the BLAS library.

After some search, I find Mac OS X seems to come with its default BLAS library. But I cannot find its headers. And as an article said, the default BLAS library may not as fast as some third party ones. So I install `OpenBLAS` with homebrew:

```
brew install openblas
```

It is installed under `/usr/local/opt/openblas`, so we need to change these lines in the MXNet's `config.mk` (it should be copied from `make/osx.mk` as the installing guide specified) :

```
# the additional link flags you want to add
ADD_LDFLAGS = '-L/usr/local/opt/openblas/lib'

# the additional compile flags you want to add
ADD_CFLAGS = '-I/usr/local/opt/openblas/include'
```

Then the compile should pass.

