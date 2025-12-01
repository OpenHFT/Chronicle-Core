# Gemini AI Assistant Guide for Chronicle-Core

This document provides guidance for AI assistants (like Gemini) and human developers working on the Chronicle-Core project. It outlines the project's purpose, build procedures, and development conventions.

## Project Overview

Chronicle-Core is a low-level, high-performance Java library designed for advanced interaction with the operating system, memory management, and resource handling. It serves as a foundational library for other Chronicle projects like Queue, Wire, and Map.

### Key Features

*   **OS Abstraction:** Provides uniform access to process/host metadata, file mapping, and thread affinity hints (`net.openhft.chronicle.core.OS`).
*   **JVM Utilities:** Offers tools for system property management, JVM initialization hooks, safepoint helpers, and class metrics (`net.openhft.chronicle.core.Jvm`).
*   **Off-Heap Memory Access:** Enables direct manipulation of native memory (`net.openhft.chronicle.core.memory`).
*   **Deterministic Resource Management:** Implements `Closeable` and reference-counted resource lifecycles with tracing for leak detection (`net.openhft.chronicle.core.io`).
*   **Threading Primitives:** Includes single-threaded guards and utilities for thread management (`net.openhft.chronicle.core.threads`).
*   **Diagnostics:** Provides exception handlers, logging bridges, and stack trace utilities (`net.openhft.chronicle.core.onoes`).

## Building and Running

The project uses Apache Maven for its build system.

### Build and Test

To build the project and run all unit tests, execute the following command from the root of the repository:

```bash
mvn clean install
```

The latest local runs on Java 8 and Java 21 completed successfully; see `build-java8.log` and `build-java21.log` in the repository root for the full output.
CI pipelines may still prefer `mvn -q verify` for quieter output, but the scope of checks is the same.

### Running Benchmarks

The project includes benchmarks that can be run using a specific Maven profile:

```bash
mvn clean test -Prun-benchmarks
```

## Development Conventions

Adherence to the project's development conventions is crucial for maintaining code quality and consistency.

### Language and Character Set

*   **Language:** Use British English spelling (e.g., `organisation`, not `organization`).
*   **Character Set:** All files must be ISO-8859-1 compatible. Avoid Unicode characters and "smart quotes".

### Javadoc

*   Javadoc should provide information that is not obvious from the method signature.
*   Focus on behavioural contracts, edge cases, thread-safety, and performance characteristics.
*   Avoid redundant comments like "gets the value" for a getter.

### Documentation

*   The project uses AsciiDoc for documentation, located in `src/main/docs`.
*   A "doc-first" approach is preferred for new features. Documentation should be updated in real-time as code changes.
*   Use the Nine-Box tagging scheme (`CORE-FN-*`, `CORE-NF-P-*`, `CORE-RISK-*`, etc.) when updating requirements, decision logs or PR descriptions.
*   Keep build artefact references current; the latest local build logs are `build-java8.log` and `build-java21.log`.

### Commit Messages

*   **Subject Line:** Keep it under 72 characters and use the imperative mood (e.g., "Fix resource leak in...").
*   **Body:** Explain the root cause of the change, the fix, and the impact. Reference JIRA or GitHub issues if applicable.

### Testing

*   The project uses JUnit 5 for unit testing.
*   Tests are located in `src/test/java`.
*   A base class `CoreTestCommon` is used for common test setup.
*   The `ThreadDump` utility is used to ensure no new threads are leaked during tests.
