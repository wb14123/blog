---
layout: post
title: ZFS Profiling on Arch Linux
tags: [ZFS, Linux, Profiling, dkms, kernel]
index: ['/Computer Science/Storage']
---

I bought a new video game recently but found `z_rd_int` processes took almost all the CPU time when I was playing it. That doesn't make much sense to me since I install games on a non-compressed ZFS dataset. Even though I don't have a powerful CPU, I don't expect ZFS to use all of them and only read about 60-70MiB/s from each of the NVMe SSDs. To double-check, I used `iostat -x 1` to confirm the iowait is very low. So disk IO is not the bottleneck.

Without finding any root cause from the Internet, I decided to do some profiling by myself. From OpenZFS' GitHub issues, people are using [perf](https://perf.wiki.kernel.org/index.php/Main_Page) to do profiling. It is trivial enough to do it from a glance. But letting `perf` show debug symbols for ZFS spent me a lot of time. So in this article, I will document the steps to enable debug symbols for ZFS and hopefully it can help more people that are facing difficulties doing it. After that, I will continue with how I find the root cause and the solution. If you've seen my previous blog [A Boring JVM Memory Profiling Story](/2023-09-30-A-Boring-JVM-Memory-Profiling-Story.html), this is an even more boring profiling story. But the tool set is important. Use them efficiently and hopefully all the profiling stories become boring.

## 1. Enable Debug Info for ZFS

On Arch Linux, if you run `perf top`, you can see kernel has debug symbols attached like this:

```
2.95%  [kernel]                                        [k] entry_SYSCALL_64
```

But for some other processes like ZFS ones, it only has an address like this:

```
2.65%  [zfs]                                           [k] 0x00000000002990cf
```

This is because perf cannot find debug info for the ZFS module. Let's enable it now.

### 1.1 Use DKMS Package

First we need to use [DKMS](https://wiki.archlinux.org/title/Dynamic_Kernel_Module_Support) package instead of a pre-compiled one so that we can control the compiling behavior when building the ZFS kernel module. In Arch Linux, the package name is `zfs-dkms` either in AUR or [archzfs](https://github.com/archzfs/archzfs) repo. Be aware packages are different from those different repos even though they have the same name. Personally I like the archzfs repo more since it's more well maintained and has better dependency management.

### 1.2 Enable debuginfo Flags

#### TL;DR:

Add these three lines to `/etc/sysconfig/zfs`, (re)install the zfs dkms package and reboot.

```bash
ZFS_DKMS_ENABLE_DEBUG=y
ZFS_DKMS_ENABLE_DEBUGINFO=y
ZFS_DKMS_DISABLE_STRIP=y
```

Decompress the installed ko file.

```bash
sudo unzstd /lib/modules/<your kernel version>/updates/dkms/zfs.ko.zst
```

Now you should be able to see ZFS symbols in `perf top`.

Remember to cleanup the files after profiling.

If you care about the reason behind these changes, continue reading. Otherwise you can skip the rest of this section.

#### What is `/etc/sysconfig/zfs`?

The package `zfs-dkms` only installs the code that will be compiled by dkms to `/usr/src/zfs-<zfs-version>`. (I learned this by reading `PKGBUILD` of the AUR package). Then when `dkms` commands are run, `dkms` copies the files to `/var/lib/dkms/zfs/<zfs-version>/build` to build it and then install the built ko files to `/lib/modules/<your kernel version>/updates/dkms`. So in order to build the ZFS module with debug symbols, we need to let dkms use the correct compile flags.

Under `/usr/src/zfs-<zfs-version>`, there is `dkms.conf` that tells DKMS how to use the source code to build and install modules. We can find some key information there:

```bash
PRE_BUILD="configure
  --prefix=/usr
  --with-config=kernel
  --with-linux=\$(
    if [ -e "\${kernel_source_dir/%build/source}" ]
    then
      echo "\${kernel_source_dir/%build/source}"
    else
      echo "\${kernel_source_dir}"
    fi
  )
  --with-linux-obj="\${kernel_source_dir}"
  \$(
    [[ -n \"\${ICP_ROOT}\" ]] && \\
    {
      echo --with-qat=\"\${ICP_ROOT}\"
    }
  )
  \$(
    [[ -r \${PACKAGE_CONFIG} ]] \\
    && source \${PACKAGE_CONFIG} \\
    && shopt -q -s extglob \\
    && \\
    {
      if [[ \${ZFS_DKMS_ENABLE_DEBUG,,} == @(y|yes) ]]
      then
        echo --enable-debug
      fi
      if [[ \${ZFS_DKMS_ENABLE_DEBUGINFO,,} == @(y|yes) ]]
      then
        echo --enable-debuginfo
      fi
    }
  )
"
```

There is `--enable-debug` and `--enable-debuginfo`. Run `./configure --help` shows the meaning of these two flags:

```
  --enable-debug          Enable compiler and code assertions [default=no]
  --enable-debuginfo      Force generation of debuginfo [default=no]
```

So if those two flags are enabled, the ZFS module should be built with debug info. The code above checks `ZFS_DKMS_ENABLE_DEBUG` and `ZFS_DKMS_ENABLE_DEBUGINFO` in file `${PACKAGE_CONFIG}`. If they are `y` or `yes`, the corresponding flags are enabled. At the beginning of `dkms.conf` we can find `PACKAGE_CONFIG` is defined as `/etc/sysconfig/zfs`.

However, only defining `ZFS_DKMS_ENABLE_DEBUG` and `ZFS_DKMS_ENABLE_DEBUGINFO` is not enough. I learned it the hard way. Checking `dkms.conf` more closely, we can see this code below:

```bash
STRIP[0]="\$(
  [[ -r \${PACKAGE_CONFIG} ]] \\
  && source \${PACKAGE_CONFIG} \\
  && shopt -q -s extglob \\
  && [[ \${ZFS_DKMS_DISABLE_STRIP,,} == @(y|yes) ]] \\
  && echo -n no
)"
```

`man dkms` shows the meaning of `STRIP`:

```
STRIP[#]=
       By default strip is considered to be "yes". If set to  "no",  DKMS  will
       not  run strip -g against your built module to remove debug symbols from
       it.  STRIP[0] is used as the default for any unset entries in the  STRIP
       array.
```

If `STRIP` is not set to `no`, `dkms` will strip the debug info! So we also need to set `ZFS_DKMS_DISABLE_STRIP` in `/etc/sysconfig/zfs` to `y` or `yes` so that `STRIP[0]` will be `no`.

#### Why unzstd?

In my system, the dkms modules are compressed with zstd when installing. But it seems `perf` is not able to read the compressed module file in order to find the debug symbols, so we need to uncompress it at the same location.


## 2. Profiling ZFS

`perf top` can show the CPU usage for each function in real time. But in order to analyze it better, we can record it with `perf record -g -p <pid>`. It should generate a `perf.data` file in the current directory. Press `Ctrl + C` to stop the recording and flush the file.

Then use `sudo perf report` to show the report of the recording. Mine is like this (press `+` to extend a row of interest in `perf report`):

```
Samples: 277K of event 'cycles:P', Event count (approx.): 244633155596
Children      Self  Command   Shared Object     Symbol
+   96.59%     0.01%  z_rd_int  [zfs]             [k] zio_do_crypt_uio
+   96.58%     0.00%  z_rd_int  [zfs]             [k] crypto_decrypt
+   96.57%     0.01%  z_rd_int  [zfs]             [k] aes_decrypt_atomic
+   75.53%     8.17%  z_rd_int  [zfs]             [k] aes_encrypt_block
+   49.76%     0.00%  z_rd_int  [zfs]             [k] crypto_update_uio
+   49.76%     0.00%  z_rd_int  [zfs]             [k] aes_decrypt_contiguous_blocks
+   49.76%     4.52%  z_rd_int  [zfs]             [k] ccm_mode_decrypt_contiguous_blocks
+   46.42%     2.08%  z_rd_int  [zfs]             [k] ccm_decrypt_final
+   42.15%     6.94%  z_rd_int  [zfs]             [k] aes_aesni_encrypt
-   24.72%    24.36%  z_rd_int  [zfs]             [k] kfpu_end
     24.36% ret_from_fork_asm
        ret_from_fork
        kthread
        0xffffffffc02b15eb
        zio_execute
        zio_done
        zio_pop_transforms
        zio_decrypt
        spa_do_crypt_abd
        zio_do_crypt_data
        zio_do_crypt_uio
        crypto_decrypt
      + aes_decrypt_atomic
-   21.20%    20.96%  z_rd_int  [zfs]             [k] kfpu_begin
     20.96% ret_from_fork_asm
        ret_from_fork
        kthread
        0xffffffffc02b15eb
        zio_execute
        zio_done
        zio_pop_transforms
        zio_decrypt
        spa_do_crypt_abd
        zio_do_crypt_data
        zio_do_crypt_uio
        crypto_decrypt
      + aes_decrypt_atomic
+   14.42%    14.21%  z_rd_int  [zfs]             [k] aes_encrypt_intel
+    7.36%     7.14%  z_rd_int  [zfs]             [k] aes_xor_block
+    6.31%     6.16%  z_rd_int  [zfs]             [k] aes_copy_block
+    1.27%     0.03%  z_rd_int  [zfs]             [k] arc_read_done
+    1.17%     0.02%  z_rd_int  [zfs]             [k] zio_vdev_io_done
+    1.14%     0.00%  z_rd_int  [zfs]             [k] abd_iterate_func
```

## 3. Find Root Cause

From the profiling report, we can easily see that the CPU is mostly used on decrypting the content on ZFS. That makes some sense because decryption does need CPU power. But there is no reason it uses so much CPU at that throughput. In fact, I found some performance issues related to encryption and did something to rule out some causes:

1. I made sure the AES hardware acceleration is enabled for my CPU by checking `lscpu | grep aes`.
2. My system can decrypt and encrypt at a much higher speed (2000+ MB/s) by running `cryptsetup benchmark`.

That's why I need the profiling to confirm where the bottleneck comes from.

Even though the code path is related to decryption, the hotspot is at `kfpu_begin` and `kfpu_end`. I read the code and have totally no idea what they are doing. I asked ChatGPT and it explained to me that it's saving and restoring FPU state. I don't know if its answer is correct or not, but that at least gave me some direction to search for issues. At last I found this GitHub issue [ICP: Improve AES-GCM performance](https://github.com/openzfs/zfs/pull/9749). It says exactly that there is a performance issue with saving FPU state when doing encryption. And the PR improves it for the AES-GCM algorithm. It states AES-CCM can benefit from a similar fix but the performance improvement will not be as great. So in the discussion of the PR, they decided to change the default encryption algorithm to AES-GCM instead of AES-CCM.

I [started using ZFS](/2020-01-28-Migrate-Arch-Linux-to-Zfs.html) before this PR. So I checked the encryption algorithm on my system by `zfs get all <dataset> | grep encryption`. And it is indeed using AES-CCM. In order to confirm it is causing a performance issue, I did some benchmarks on AES-CCM, AES-GCM and non-encrypted datasets.

First, I created the datasets:

```bash
sudo zfs create -o encryption=aes-256-ccm -o compression=off -o atime=off zroot/root/ccm-test
sudo zfs create -o encryption=aes-256-gcm -o compression=off -o atime=off zroot/root/gcm-test
sudo zfs create -o encryption=off -o compression=off -o atime=off zroot/local_steam_unencrypt
```

Then I wrote a script to benchmark it:

```bash
#!/bin/bash

set -e

function print_cputime() {
	pname=$1
	for pid in `pgrep $pname` ; do
		ps -p $pid -o cputime,etime
	done
}


function benchmark {
	test_name=$1
	test_file=$2

	file_size="20480"

	echo "### Start benchmark $test_name"

	echo "### Print z_wr_iss cpu time before the write test"
	print_cputime z_wr_iss
	echo "### Start write test"
	time dd if=/dev/random of=$test_file bs=1M count=$file_size oflag=direct
	echo "### Print z_wr_iss cpu time after the write test"
	print_cputime z_wr_iss

	echo "### Print z_rd_int cpu time before the read test"
	print_cputime z_rd_int
	echo "### Start read test"
	time dd if=$test_file of=/dev/null bs=1M count=$file_size
	echo "### Print z_rd_int cpu time after the read test"
	print_cputime z_rd_int
}

benchmark ccm-test /ccm-test/test-file
benchmark gcm-test /gcm-test/test-file
benchmark non-encrypt-test /data/local_steam/test-file
```

My ZFS cache is set to 8GB. So I write and read files with 20GB. It uses dd to write and read a file. Before the read and write, it uses `ps -o cputime,etime` to print out CPU time and wall time used by each related ZFS process.

Running this script creates lots of output. The full output can be found in the appendix at the end. Here are the key lines:

```
### Start benchmark ccm-test
// ... output omitted ...
21474836480 bytes (21 GB, 20 GiB) copied, 107.307 s, 200 MB/s
// ... output omitted ...
### Start benchmark gcm-test
// ... output omitted ...
21474836480 bytes (21 GB, 20 GiB) copied, 13.7417 s, 1.6 GB/s
// ... output omitted ...
### Start benchmark non-encrypt-test
// ... output omitted ...
21474836480 bytes (21 GB, 20 GiB) copied, 9.03496 s, 2.4 GB/s
// ... output omitted ...
```

During the test, AES-CCM makes `z_rd_int` take all CPU time as observed before. For AES-GCM, it's much better, `z_rd_int` takes less than 50% and for non-encrypted it's less than 20%. The testing output prints the CPU time and wall time for each of the `z_rd_int` processes before and after the test. So you can calculate the percentage.

From the test result, we can see AES-CCM indeed affects read performance a lot. It's even slower than writes. We can confirm this is the root cause of our problem.


## 4. Solution and Workaround

The solution is obvious: just change the encryption from AES-CCM to AES-GCM. But it cannot be done without migrating the dataset to another place and then moving it back. It takes time. In the meantime, I moved my Steam library to a non-encrypted dataset since I have enough disk space to do the migration. It doesn't have sensitive information. Yes it exposes the machine to [evil maid attack](https://en.wikipedia.org/wiki/Evil_maid_attack), but my setup on the machine doesn't prevent it anyway. See my previous blog [Personal ZFS Offsite Backup Solution](/2021-09-19-Personal-ZFS-Offsite-Online-Backup-Solution.html) for more information on putting a machine into an untrusted environment.

I'll do the migration from AES-CCM to AES-GCM in the future and report back how it works. Stay tuned!

## 5. Appendix

Here is the full output from the benchmark script:

```
### Start benchmark ccm-test
### Print z_wr_iss cpu time before the write test
    TIME     ELAPSED
00:47:56  3-03:39:21
    TIME     ELAPSED
00:22:34  3-03:39:21
    TIME     ELAPSED
00:47:54  3-03:39:21
    TIME     ELAPSED
00:47:55  3-03:39:21
    TIME     ELAPSED
00:00:01  3-03:39:17
    TIME     ELAPSED
00:00:00  3-03:39:17
    TIME     ELAPSED
00:04:50    15:30:06
    TIME     ELAPSED
00:04:49    15:29:57
    TIME     ELAPSED
00:04:51    15:29:56
    TIME     ELAPSED
00:04:51    15:29:18
    TIME     ELAPSED
00:00:00    10:07:30
    TIME     ELAPSED
00:00:00       55:49
### Start write test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 91.4066 s, 235 MB/s

real	1m31.414s
user	0m0.059s
sys	0m53.252s
### Print z_wr_iss cpu time after the write test
    TIME     ELAPSED
00:49:23  3-03:40:53
    TIME     ELAPSED
00:22:34  3-03:40:53
    TIME     ELAPSED
00:49:21  3-03:40:53
    TIME     ELAPSED
00:49:22  3-03:40:53
    TIME     ELAPSED
00:00:01  3-03:40:49
    TIME     ELAPSED
00:00:00  3-03:40:49
    TIME     ELAPSED
00:04:50    15:31:38
    TIME     ELAPSED
00:04:50    15:31:28
    TIME     ELAPSED
00:04:51    15:31:28
    TIME     ELAPSED
00:04:51    15:30:50
    TIME     ELAPSED
00:00:00    10:09:01
    TIME     ELAPSED
00:00:00       57:21
### Print z_rd_int cpu time before the read test
    TIME     ELAPSED
00:24:46  3-03:40:53
    TIME     ELAPSED
00:00:02  3-03:40:49
    TIME     ELAPSED
00:01:50       06:47
    TIME     ELAPSED
00:01:49       06:47
### Start read test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 107.307 s, 200 MB/s

real	1m47.372s
user	0m0.060s
sys	0m8.091s
### Print z_rd_int cpu time after the read test
    TIME     ELAPSED
00:26:24  3-03:42:41
    TIME     ELAPSED
00:00:02  3-03:42:37
    TIME     ELAPSED
00:03:28       08:34
    TIME     ELAPSED
00:03:27       08:34
### Start benchmark gcm-test
### Print z_wr_iss cpu time before the write test
    TIME     ELAPSED
00:49:35  3-03:42:41
    TIME     ELAPSED
00:22:34  3-03:42:41
    TIME     ELAPSED
00:49:33  3-03:42:41
    TIME     ELAPSED
00:49:33  3-03:42:41
    TIME     ELAPSED
00:00:01  3-03:42:37
    TIME     ELAPSED
00:00:00  3-03:42:37
    TIME     ELAPSED
00:04:50    15:33:26
    TIME     ELAPSED
00:04:50    15:33:16
    TIME     ELAPSED
00:04:51    15:33:16
    TIME     ELAPSED
00:04:51    15:32:38
    TIME     ELAPSED
00:00:00    10:10:49
    TIME     ELAPSED
00:00:00       59:08
### Start write test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 56.9529 s, 377 MB/s

real	0m56.960s
user	0m0.045s
sys	0m53.566s
### Print z_wr_iss cpu time after the write test
    TIME     ELAPSED
00:49:42  3-03:43:38
    TIME     ELAPSED
00:22:35  3-03:43:38
    TIME     ELAPSED
00:49:39  3-03:43:38
    TIME     ELAPSED
00:49:39  3-03:43:38
    TIME     ELAPSED
00:00:01  3-03:43:34
    TIME     ELAPSED
00:00:00  3-03:43:34
    TIME     ELAPSED
00:04:51    15:34:23
    TIME     ELAPSED
00:04:50    15:34:14
    TIME     ELAPSED
00:04:52    15:34:13
    TIME     ELAPSED
00:04:52    15:33:35
    TIME     ELAPSED
00:00:00    10:11:46
    TIME     ELAPSED
00:00:00    01:00:06
### Print z_rd_int cpu time before the read test
    TIME     ELAPSED
00:26:24  3-03:43:38
    TIME     ELAPSED
00:00:02  3-03:43:34
    TIME     ELAPSED
00:00:00       00:05
    TIME     ELAPSED
00:00:00       00:05
### Start read test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 13.7417 s, 1.6 GB/s

real	0m13.743s
user	0m0.071s
sys	0m11.215s
### Print z_rd_int cpu time after the read test
    TIME     ELAPSED
00:26:31  3-03:43:52
    TIME     ELAPSED
00:00:02  3-03:43:48
    TIME     ELAPSED
00:00:07       00:19
    TIME     ELAPSED
00:00:07       00:19
### Start benchmark non-encrypt-test
### Print z_wr_iss cpu time before the write test
    TIME     ELAPSED
00:49:42  3-03:43:52
    TIME     ELAPSED
00:22:35  3-03:43:52
    TIME     ELAPSED
00:49:40  3-03:43:52
    TIME     ELAPSED
00:49:39  3-03:43:52
    TIME     ELAPSED
00:00:01  3-03:43:48
    TIME     ELAPSED
00:00:00  3-03:43:48
    TIME     ELAPSED
00:04:51    15:34:37
    TIME     ELAPSED
00:04:50    15:34:28
    TIME     ELAPSED
00:04:52    15:34:28
    TIME     ELAPSED
00:04:52    15:33:49
    TIME     ELAPSED
00:00:00    10:12:01
    TIME     ELAPSED
00:00:00    01:00:20
### Start write test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 56.0508 s, 383 MB/s

real	0m56.052s
user	0m0.042s
sys	0m53.060s
### Print z_wr_iss cpu time after the write test
    TIME     ELAPSED
00:49:46  3-03:44:49
    TIME     ELAPSED
00:22:35  3-03:44:49
    TIME     ELAPSED
00:49:44  3-03:44:49
    TIME     ELAPSED
00:49:43  3-03:44:49
    TIME     ELAPSED
00:00:01  3-03:44:44
    TIME     ELAPSED
00:00:00  3-03:44:44
    TIME     ELAPSED
00:04:51    15:35:33
    TIME     ELAPSED
00:04:50    15:35:24
    TIME     ELAPSED
00:04:52    15:35:24
    TIME     ELAPSED
00:04:52    15:34:46
    TIME     ELAPSED
00:00:00    10:12:57
    TIME     ELAPSED
00:00:00    01:01:16
### Print z_rd_int cpu time before the read test
    TIME     ELAPSED
00:26:31  3-03:44:49
    TIME     ELAPSED
00:00:02  3-03:44:45
    TIME     ELAPSED
00:00:07       01:16
    TIME     ELAPSED
00:00:07       01:16
### Start read test
20480+0 records in
20480+0 records out
21474836480 bytes (21 GB, 20 GiB) copied, 9.03496 s, 2.4 GB/s

real	0m9.036s
user	0m0.032s
sys	0m8.207s
### Print z_rd_int cpu time after the read test
    TIME     ELAPSED
00:26:33  3-03:44:58
    TIME     ELAPSED
00:00:02  3-03:44:54
    TIME     ELAPSED
00:00:09       01:25
    TIME     ELAPSED
00:00:09       01:25
```
