---
layout: post
title: My Recent Work About Neural Networks
tags: [neural network, deep learning, programming]
index: ['/Computer Science/Machine Learning']
---

These days I've written some code about neural networks. There is nothing important, but worth to be recorded.

Choose Deep Learning Frameworks
------------------

The first thing is to decide which framework I should use. There are many frameworks about neural networks, I tried some famous ones. Here is the details:

### [Caffe](https://github.com/BVLC/caffe)

Caffe is a famous framework, mainly used with convolution neural networks. It is written with C++ but uses protocol buffer to describe the network. It is known as its well structured code, high performance. But the use of protocol buffer doesn't make me comfortable because I'm not afraid of write some code and the file is less flexible.

### [Theano](http://deeplearning.net/software/theano/)

Theano is a framework written in Python. You can define an expression and Theano can find the gradient for you. The training process of deep learning is mainly find the gradients for each layer, so it simplified the work a lot. It's performance is good, too.

But it is more like a compiler for me. I cannot see the low level things and the code is not just normal python code, it has too much hacking.

Theano is more like a optimize library than a neural network framework. [PyLearn2](https://github.com/lisa-lab/pylearn2) is a framework based on it, which provides many network structures and tools. But like Caffe, it uses a YAML config file to describe the structure of the network, which makes me uncomfortable.

### [Deeplearning4j](http://deeplearning4j.org)

This is a framework written in Java. It is not so famous, but interesting to me. I'm more familiar with Java than C++. A Java framework means a better IDE, more libraries to use. And it may supports Scala, which I use a lot in these days. So I tried it a little, but it is not as good as I thought.

First of all, it is in heavily developing. And the name of methods and variables is too long and the API is not so great to use. The most important is, the performance seems not so good and the integration with the GPU is not very easy. And it is not popular in research field so the communication with others may becomes a problem.


### [Torch7](http://torch.ch/)

This is the framework I finally use. Actually, it is the first framework I've used.

Some big companies use it, including Deep Mind (Google), Facebook and so on. It is written in Lua, which is a language I've always want to learn. It is easy to understand. When there comes to the low level, you just need to read the C code, which is more easy to read than the C++ code. There is no magic between the high level and the low level, I can just dig it. It's performance is great, and the ecosystem is big and healthy.

But it also has some cons. For example, the error hint is not so great, and the code is too flexible so that you must read the document to know how to use some modules. But I can deal with it.


Write My Own Library
----------------

I write my own library with Scala and [Breeze](https://github.com/scalanlp/breeze), in order to understand neural networks better.

It is very easy to write a neural network library (which is not distributed), as long as you understand it. While I wrote it, I realize that the core of neural networks is just gradient optimize (with many tricks). One layer of a network is just a function, layers are just function after function. So the gradient of each layer is computed by chain rule. When you need a new layer, just write how to compute the output and the gradient, then you can push it into the network structure.

Use Scala to write it feels good, because OOP makes it feel nature to write layers. And the trait system makes it more pleasure. But the performance is not as good as the libraries above. Make it to support GPU is hard, too. Running a large size network makes it running like forever. So I gave up after wrote the convolution network and use Torch7 instead.


Run Some Examples
-----------------

[This article](https://timdettmers.wordpress.com/2014/08/14/which-gpu-for-deep-learning/) gives some great advices to choose a GPU for deep learning. Titan X is great but is too expensive. So I decide to wait until next year while NVIDIA will release their new GPU with 10X power. At the same time, I have a GPU with 1G RAM in my office.

I wrote some simple code like MLP and simple convolution networks to train with MNIST data. Then I decided to run some large examples.

First, I run the example from [fbcunn](https://github.com/facebook/fbcunn/tree/master/examples/imagenet), which is a AlexNet training on ImageNet. The ImageNet data is too big and the bandwidth of my office is too small. So I run the example with an `g2.2xlarge` instance on AWS. It still took lots of time, which trained 2 days to archive a precision of about 30% (not ended yet). Then I realized it is too expensive to run it on AWS and stop it.

The Second example I run is an RNN example. The code is from [here](https://github.com/karpathy/char-rnn). I run it on the machine with 1G RAM GPU. It looks good on small networks. But when I use the data from Chinese Wikipedia (with 1G plain text), the memory of GPU could only train a network of 512 parameters, which is too small to get good result.

The time of training large neural networks is really long. So I will wait for the new hardware to continue my research. At the mean time, I will review the knowledge about linear algebra and probability theory.
