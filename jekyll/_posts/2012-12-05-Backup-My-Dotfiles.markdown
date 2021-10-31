---
layout: post
title: Backup My Dotfiles
categories: workspace
tags: [vim, zsh, config, git]
index: ['/Computer Science/Operating System/Linux']
---

These days I re-configure my vim and zsh. Then backup them in [github](http://github.com). You can see it at [here](https://github.com/wb14123/dotfiles).

Using github to backup dotfiles is really a comfortable way. In this way, you can:

+ Feel free to change your configures, since if you don't like the configure some day, you can roll it back.
+ Update your configures anywhere. Just use git to clone your configure files from github.
+ Keep different versions for different machines (Such as for desktop and laptop). Just use branch to control them.
+ If you are using vim plugins from git repo or something like [oh-my-zsh](https://github.com/robbyrussell/oh-my-zsh), you could simple use git submodule to keep track of them.

The basic idea is put configure files in a directory such as `dotfile` and use git to track it. Then make a symbolic link to home. However, manual make symbolic links is boring. Firstly I am thinking of use the same directory structure as `~/` and then automatic link them. But there comes some problems: for some directories such as `.vim`, we should make a link for them while for some directories such as `.config`, we may only want to link `openbox` under it to `~/.config/openbox`. It seems impossible to use an automatic way totally. But some shell code could make life a little easy:

	check_link() {
		LINK=`pwd`/$1
		TARGET=~/$2
		if [ -h $TARGET ] ; then
			echo -n "remove symbolic link: "
			rm -v $TARGET
		elif [ -e $TARGET ] ; then
			echo -n "move: "
			mv -v $TARGET $TARGET.old
		fi
		echo -n "link: "
		ln -sv $LINK $TARGET
	}

Usage: `check_link <path_in_dotfiles> <path_in_home>`, such as `check_link vim/vimrc .vimrc`. See [my github repo](https://github.com/wb14123/dotfiles/blob/master/link.sh) for details.
