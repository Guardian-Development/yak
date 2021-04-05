# yak

[![main-build](https://github.com/Guardian-Development/yak/actions/workflows/yak-main-build.yml/badge.svg)](https://github.com/Guardian-Development/yak/actions/workflows/yak-main-build.yml)

yet-another-cache implementation in Java, optimised for known size binary values.

## milestones

- run yak in-process (JVM) with minimal allocations after startup.
- run yak in both RAM and file persistence mode.
- run yak with an LRU eviction strategy.
- run yak standalone, using TCP to communicate from client/server.
- run yak with metrics enabled.
- run yak with different eviction strategies.

- run yak in clustered-mode, where a cache turns into an eventually consistent CRDT.

