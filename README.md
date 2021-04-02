# org.guardiandev.yak

[![main-build](https://github.com/Guardian-Development/org.guardiandev.yak/actions/workflows/org.guardiandev.yak-main-build.yml/badge.svg)](https://github.com/Guardian-Development/org.guardiandev.yak/actions/workflows/org.guardiandev.yak-main-build.yml)

yet-another-cache implementation in Java, optimised for known size binary values.

## milestones

- run org.guardiandev.yak in-process (JVM) with minimal allocations after startup.
- run org.guardiandev.yak in both RAM and file persistence mode.
- run org.guardiandev.yak with an LRU eviction strategy.
- run org.guardiandev.yak standalone, using TCP to communicate from client/server.
- run org.guardiandev.yak with metrics enabled.
- run org.guardiandev.yak with different eviction strategies.

- run org.guardiandev.yak in clustered-mode, where a cache turns into an eventually consistent CRDT.

