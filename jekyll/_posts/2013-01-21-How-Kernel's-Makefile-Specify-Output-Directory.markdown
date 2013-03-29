---
layout: post
title: How Kernel's Makefile Specify Output Directory
tags: [kernel, make]
---

When compile Linux kernel, we could output files to a split directory with "make O=". The kernel's way to do it is a little tricky. Since kernel's Makefile is very big, we could have a simpler version to analyse:

	ifeq ($(KBUILD_SRC),)
	ifeq ("$(origin O)", "command line")
		KBUILD_OUTPUT := $(O)
	endif

	ifneq ($(KBUILD_OUTPUT),)
	$(filter-out submake $(CURDIR)/Makefile, $(MAKECMDGOALS)): sub-make
		@:

	sub-make:
		make -C $(KBUILD_OUTPUT) -f /home/wangbin/maketest/Makefile \
			KBUILD_SRC=$(PWD) \
			$(MAKECMDGOALS)
		@echo " sub-make KBUILD_OUTPUT: $(KBUILD_OUTPUT)"
	skip-makefile := 1
	endif #end KBUILD_OUTPUT
	endif #end KBUILD_SRC

	ifeq ($(skip-makefile),)

	target1:
		touch target1
		@echo "target KBUILD_OUTPUT: $(KBUILD_OUTPUT)"

	target2:
		touch target2
		@echo "target KBUILD_OUTPUT: $(KBUILD_OUTPUT)"
	endif

You could try to execute `make O=../build target1`, it will output files to `../build`. Let's see how it works.

When you execute make, `KBUILD_SRC` is not defined at first, so it will make `sub-make` as a dependency of any target you input(except some `filter-out` target such as `sub-make`). Change the directory to `KBUILD_OUTPUT`(`-C` option), set `KBUILD_SRC` and then invoke itself again(`-f` option).

At the second time, `KBUILD_SRC` is defined so it will make the real targets.

The thing to notice is, while you make the real `target1`, **variables between `ifeq ($(KBUILD_SRC),)` is not defined**. You could see the output:

	make -C ../build -f /home/wangbin/maketest/Makefile \
	                KBUILD_SRC=/home/wangbin/maketest \
	                target1
	make[1]: Entering directory `/home/wangbin/build'
	touch target1
	target KBUILD_OUTPUT: 
	make[1]: Leaving directory `/home/wangbin/build'
	sub-make KBUILD_OUTPUT: ../build

