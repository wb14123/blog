---
layout: post
title: Notes On Go Scheduler
tags: [golang, scheduler]
---

I read the Go source code about scheduler these days. They are under `src/pkg/runtime`, mainly in `runtime.h`, `proc.c` and some Assembly files. The scheduler's policy is easy to understand, because there are already many articles on this. I'm more interesting in the details. I learned how to switch the current running goroutine, which is a little mystery for me before.

Basic Structures
-----------

Go scheduler mainly uses three structures:

* `G` as a Goroutines.
* `M` as an OS thread.
* `P` as a context or process. Running under threads and control the goroutines.

The number of `P` is setted by `GOMAXPROCS`.

Each `M` has a `G` called `g0` to do schedule jobs. (`mcall` will use `g0` to execute functions).

The Beginning
------------

As the comments in the source code, the bootstrap sequence is:

* call osinit
* call schedinit
* make & queue new G
* call runtime·mstart

Well, it is not very detailed. Let's read the code. The bootstrap code is Assembly. You can find one example in `asm_386.s`:

```
ok:
	// set up m and g "registers"
	get_tls(BX)
	LEAL	runtime·g0(SB), CX
	MOVL	CX, g(BX)
	LEAL	runtime·m0(SB), AX

	// save m->g0 = g0
	MOVL	CX, m_g0(AX)
	// save g0->m = m0
	MOVL	AX, g_m(CX)

	CALL	runtime·emptyfunc(SB)	// fault if stack check is wrong

	// convention is D is always cleared
	CLD

	CALL	runtime·check(SB)

	// saved argc, argv
	MOVL	120(SP), AX
	MOVL	AX, 0(SP)
	MOVL	124(SP), AX
	MOVL	AX, 4(SP)
	CALL	runtime·args(SB)
	CALL	runtime·osinit(SB)
	CALL	runtime·hashinit(SB)
	CALL	runtime·schedinit(SB)

	// create a new goroutine to start program
	PUSHL	$runtime·main·f(SB)	// entry
	PUSHL	$0	// arg size
	ARGSIZE(8)
	CALL	runtime·newproc(SB)
	ARGSIZE(-1)
	POPL	AX
	POPL	AX

	// start this M
	CALL	runtime·mstart(SB)

	INT $3
	RET
```

It init an `M`, and a `G` as this `M`'s `g0`.

`runtime·schedinit` init the global scheduler, read `GOMAXPROCS` and start the `P`s.

`runtime.main` and `runtime.newproc` create and queue the main Goroutine.

`runtime.mstart` start this `M` at last.

`runtime.mstart` will call `schedule`, `schedule` will find a runnable `G` and call `execute` on it, `execute` will call `runtime·gogo` which is defined in the Assembly files, set the program pointer and stack to run a `G`. Now the program starts running and will never return. This main goroutine is locked to the main OS thread.

Here is what `runtime.gogo` do, for example in `asm_386.s`:

```
// void gogo(Gobuf*)
// restore state from Gobuf; longjmp
TEXT runtime·gogo(SB), NOSPLIT, $0-4
	MOVL	4(SP), BX		// gobuf
	MOVL	gobuf_g(BX), DX
	MOVL	0(DX), CX		// make sure g != nil
	get_tls(CX)
	MOVL	DX, g(CX)
	MOVL	gobuf_sp(BX), SP	// restore SP
	MOVL	gobuf_ret(BX), AX
	MOVL	gobuf_ctxt(BX), DX
	MOVL	$0, gobuf_sp(BX)	// clear to help garbage collector
	MOVL	$0, gobuf_ret(BX)
	MOVL	$0, gobuf_ctxt(BX)
	MOVL	gobuf_pc(BX), BX
	JMP	BX
```

The Start of the Other Goroutines
--------------

When the program use the keyword `go`, it will start a new Goroutine. `runtime·proc` could start a `G` given the function. It put this new `G` in the current `P`'s run queue, then call `wakep` in order to call `startm`, which will get an idle `P` and run a `G` on it. If the `P` don't have an `M` for now, it will call `newm` which will alloc an `M` using `startm` as its executing function. As we already known, `startm` will start this `M`, call scheduler to find a `G` to run.

How the `P` finds the executable `G` will be explained in the next section.

Change the Current Executing Goroutine
--------------

### When to Change

We know when the `G` is executing, the code will never return. It means that the scheduler of Go is non-preemptive. So when to change the current running `G`?

When the current Goroutine call a system call, it is the chance. It will call `handoffp`, which will release the current `P` (and set it's state to idle), start a new `M` to run the system call `G`, and then call `startm`, which will find an idle `P` and run it.

From Go 1.2, it increases the chance to run the scheduler. For example, when the goroutine call a function, it will check the memory and deside whether to do a scheduling.

This could be verified by a piece of code:

```
package main

import (
	"fmt"
	"sync"
	"time"
)

type Counter struct {
	Count int
	Mu    sync.Mutex
}

func loop1() {
	loop1()
}

func loop2() {
	for {
	}
}

func main() {
	counter := Counter{Count: 0}
	loop2()

	for j := 0; j < 10; j++ {
		go func() {
			// get uid
			counter.Mu.Lock()
			id := counter.Count
			counter.Count += 1
			counter.Mu.Unlock()

			/*
			   It will print the current number of goroutine.
			   It is a system call, so it will give other goroutines a chance to run,
			   but it just happens in the beginning,
			   so it will just allow a limited number of Goroutines to run.
			*/
			fmt.Println(id)

			/*
			  loop2() is a dead end loop. So it will block other Goroutines to run.
			  loop1() keeps call functions, so it give other Goroutines a chance to run.
			  You can see what will happen when change it to loop1()
			*/
			loop2()
		}()
	}
	fmt.Println("hi")
	time.Sleep(time.Second * 1000)
}
```

Run it with the debug options:

```
GODEBUG=schedtrace=1000,scheddetail=1 GOMAXPROCS=10 go run a.go
```

### How to Change

We've know that the first Gorutine is run by the main program and never returns. So how to change the current Goroutine? The answer is it just change the current SP to the `g0` by the Assembly code. The current Goroutine's context is saved in order to be swapped back in the future. `g0` will then call the scheduler to decide which Goroutine to run next.

### Which One to Change

In the last section, we know that the new started `G` is just put under the `P` that calls the `go` keyword. So the queue don't ensure fairness. However, while the `P` is able to run a new `G` but could not find any `G` in its queue, it will look up other `P`s and steal half of their `G`s if there are any. This is more effective that ensure the fairness while put `G` in the run queue since it need not lock all the scheduler to check all the `P`s run queue to ensure the fairness.

The code could be found in `findrunnable`.
