---
title: Common Problems
---

# Deploying Marathon on CoreOS (Docker)

By default, CoreOS does not populate `/etc/hosts` which may lead to Marathon crashing on startup.
You may see something like this in Marathon logs (or syslog)
`F0728 00:56:32.080438 1571 process.cpp:889] Name or service not known`

The easy way to fix is to allow CoreOS to manage `/etc/hosts` with the `manage_etc_hosts: localhost` attribute in your cloud config. Refer to the [CoreOS Cloud-Config documentation](https://coreos.com/os/docs/latest/cloud-config.html) for more details.

Check lookups work by running `hostname -f` and verify it returns successfully (exit code 0)

# Running Marathon on Azure Linux

Similar to CoreOS, sometimes Azure Linux VMs (observed mostly on Ubuntu) are missing a hostname entry in `/etc/hosts`. If you find running sudo commands takes a long time or `hostname -f` exits with an error, modify your `/etc/hosts` file and prepend it with an entry for your current hostname. E.g. '127.0.0.1   {my-host-name}'

Once `hostname -f` returns successfully, Marathon should be able to start.

# Task Timeout using Docker Containerizer

The default executor timeout on mesos slaves or Marathon's `task_launch_timeout` are too low for Docker containerizers (docker pull can take some time...). Refer to the docs on [Docker Containers]({{ site.baseurl }}/docs/native-docker.html)

# Error reading authentication secret from file

When using framework-authentication on the master, be sure to set a secret that has a minimum of eight characters. Secrets shorter than that length may not be accepted by Marathon.
