---
layout: post
title: Setup SSH Authentication with YubiKey
tags: [linux, ssh, security, yubikey, yubico, pam, pam-u2f]
index: ['/Computer Science/Operating System/Linux']
---

[YubiKey](https://www.yubico.com/) is a kind of hardware security token. The idea is to authenticate a person not only based on something he knows (password), but also on something he owns. It can be a digital file, but a more secure option would be a hardware token like Yubikey since no one can steal it without physical access. I use it for a lot of services. Not surprisingly, it can also be used in ssh authentication. But the official Yubikey tutorials are not very straightforward and the ~~Archlinux wiki pages are more generic instead of Yubikey specific~~. So in this article, I'll introduce how to setup ssh to include Yubikey in the authentication process. The operating system I'm using is Arch Linux, but the process for other Linux systems should be very similar.

## Generate OpenSSH Hardware Token

The easiest way is to generate a ssh key file based on Yubikey. OpenSSH supports this since 8.2.

1. Run `ssh-keygen -t ecdsa-sk`
2. Touch the Yubikey for a few seconds.

Then you can use the generated ssh key like other key files with `-i` option. After typing in the login command, you need to touch Yubikey for a few seconds, then you should be able to login.


## ~~Use PAM~~

**Update: this way only works while the key is plugged into the ssh host, which makes it useless for SSH. However, it's still useful for things like local login.**

A more generic way is to use [PAM](https://en.wikipedia.org/wiki/Linux_PAM) with Yubikey. It's a modular authentication mechanism not only for SSH, but also for lots of other things like local login.

### 1. Install packages

PAM should be installed by default for Arch Linux. So the only package we need to install is the PAM module for Yubikey `pam-u2f`:

```
sudo pacman -S pam-u2f
```

### 2. Generate u2f mapping file

Run this command first:

```
pamu2fcfg -u<username> # Replace <username> by your username
```

Touch your Yubikey for a few seconds and save the command result to a configuration file, for example, `/etc/u2f_mappings`.

### 3. Config PAM for SSH

The PAM config file for ssh is located at `/etc/pam.d/sshd`. In order to add Yubikey as part of the authentication, add this line to the file:

```
auth required pam_u2f.so authfile=/etc/u2f_mappings
```

`required` means Yubikey authentication is necessary. The other options are `requisite`, `sufficient` and `optional`. Refer to [Redhat document](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/7/html/system-level_authentication_guide/pam_configuration_files) for more details.

The parts after `pam_u2f.so` are the parameters. `authfile` is one of them. For all the supported parameters, refer to [Yubico pam-u2f document](https://developers.yubico.com/pam-u2f/).

### 4. Config SSH to include password authentication

In order to actually use PAM in ssh, the ssh server needs to include password as part of authorization methods. The configuration is `AuthenticationMethods` in `sshd_config`. For example, if you want to use password + Yubikey + ssh key file, you can configure it like this:

```
AuthenticationMethods "publickey,password"
```

And make sure `PasswordAuthentication` and `ChallengeResponseAuthentication` are both `yes`:

```
PasswordAuthentication Yes
ChallengeResponseAuthentication Yes
```

After this, restart sshd then you can login with Yubikey authentication: type in the ssh login command, input user password and press enter, touch the Yubikey for a few seconds, then you should be able to login!
