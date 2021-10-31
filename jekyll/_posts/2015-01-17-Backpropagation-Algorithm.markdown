---
layout: post
title: Backpropagation Algorithm
tags: [algorithm, deep learning]
index: ['/Computer Science/Machine Learning']
---

These days I start to learn neural networks again and write some Matlab codes from scratch. I try to understand everything I do while I write the code, so I derive the equations in the back propagation while try to keep it clear and easy to understand.

Neural Network Functions
---------------

A multi layer neural network could be defined as this:

<div>
\begin{equation}
a_1(x, w, b) = x \\
\end{equation}

\begin{equation}
z_i(x, w, b) = a_{i-1}(x, w, b) \cdot w_{i-1} + b_{i-1}
\end{equation}

\begin{equation}
a_i(x, w, b) = \sigma(z_i(x, w, b))
\end{equation}

</div>

Assume <span>$$ layer_i $$</span> means the number of neural in layer i, then the variables in the equations could be explained as below:

* `x` is the input, which is a row vector, it has <span>$$ layer_1 $$</span> elements.
* <span>$$ w_i $$</span> meas the weights in layer `i`, which is a matrix of <span>$$ layer_i $$</span> rows and <span>$$ layer_{i+1} $$</span> columns.
* <span>$$ b_i $$</span> means biases in layer i, which is a row vector of <span>$$ layer_{i+1} $$</span> elements.
* <span>$$ a_i $$</span> means activation function in the ith layer, which the output is a row vector, it has <span>$$ layer_i $$</span> elements.
* `l` means the last layer. The output of <span>$$a_l$$</span> is the output of the neural network.

And <span>$$ \sigma(z) $$</span> may be different in different use cases. This one is an example:

<div>
\begin{equation}
\sigma(z) = {1 \over 1 + e^{-z}}
\end{equation}
</div>


Gradient Descent Algorithm
---------------

We need a cost function to measure how well do we do for now. And the training of the network becomes a optimization problem. The method we use in the problem is gradient descent. Let me try to explain it.

Assume we have a cost function, and it is always non-negative. For example, this function is a good one:

<div>
\begin{equation}
C(x, y, w, b) = {1 \over 2} (y - a_l(x, w, b)) ^ 2
\end{equation}
</div>

Then the goal is try to make the output of the cost function smaller. Since `x` and `y` is fixed, the change of cost function while change `w` and `b` little could be shown as this:

<div>
\begin{equation}
\Delta C = {\partial C \over \partial w} \Delta w + {\partial C \over \partial b} \Delta b 
\end{equation}
</div>

In order to make the cost function smaller, we need to make <span>$$ \Delta C$$</span> negative. We can make <span>$$ \Delta w = - {\partial C \over \partial w} $$</span> and <span>$$ \Delta b = - {\partial C \over \partial b} $$</span>. So that <span>$$ \Delta C = - ({\partial C \over \partial w}) ^ 2 - ({\partial C \over \partial b}) ^ 2$$</span> which is always negative.


Compute Partial Derivative
------------

So the goal is to compute the partial derivative <span>$$ \partial C \over \partial w$$</span> and <span>$$ \partial C \over \partial b$$</span>. Using equations (1), (2), (3), (5) and chain rule, we can get this:

<div>

\begin{equation}
{\partial C \over \partial a_l} = a_l - y
\end{equation}

\begin{equation}
{\partial C \over \partial a_i} = {\partial C \over \partial a_{i+1}} {\partial a_{i+1} \over \partial \sigma} {\partial \sigma \over \partial z_{i+1}} {\partial z_{i+1} \over \partial a_i} = {\partial C \over \partial a_{i+1}} \odot {\sigma^{'}(z_{i+1})} w_i^{'}
\end{equation}

\begin{equation}
{\partial C \over \partial w_i} = {\partial C \over \partial a_{i+1}} {\partial a_{i+1} \over \partial \sigma} {\partial \sigma \over \partial z_{i+1}} {\partial z_{i+1} \over \partial w_i} = a_i^{'} {\partial C \over \partial a_{i+1}} \odot {\sigma^{'}(z_{i+1})}
\end{equation}

\begin{equation}
{\partial C \over \partial b_i} = {\partial C \over \partial a_{i+1}} {\partial a_{i+1} \over \partial \sigma} {\partial \sigma \over \partial z_{i+1}} {\partial z_{i+1} \over \partial b_i} = {\partial C \over \partial a_{i+1}} \odot {\sigma^{'}(z_{i+1})}
\end{equation}

</div>

Note that we use <span>$$ \odot $$</span> before <span>$$ \sigma^{'} $$</span> is because function <span>$$ \sigma(z) $$</span> is element wise.

We can find there are many same parts in these equations: <span>$$ {\partial C \over \partial a_{i+1}} \odot {\sigma^{'}(z_{i+1})} $$</span>. So we can define <span>$$ \delta_i = {\partial C \over \partial a_i} \odot {\sigma^{'}(z_i)} $$</span>, and rewrite these equations like this to avoid compute these parts many times:

<div>
\begin{equation}
\delta_l = (a_l - y) \odot \sigma^{'}(z_l)
\end{equation}

\begin{equation}
\delta_i = \delta_{i+1} w_i^{'} \odot \sigma^{'}(z_i)
\end{equation}

\begin{equation}
{\partial C \over \partial w_i} = a_i^{'} \delta_{i+1}
\end{equation}

\begin{equation}
{\partial C \over \partial b_i} = \delta_{i+1}
\end{equation}
</div>

With these equations, we can write the back propagation algorithm easily.
