# Chronicle Core Test Batches (TESTS_TODO)

## Scope & Objectives
- Protect the reordered ChronicleInit bootstrap, stricter reference counting rules, and the new documentation contract by exercising the most sensitive surfaces before each publish.
- Keep every command runnable from `/home/peter/Build-All/Chronicle-Core` with Java 17 and Maven on `PATH`.
- Default to the ISO-8859-1 / British English policy when logging outcomes or adding new notes.

## Batch Execution Matrix
1. **Baseline Regression Sweep**
   - Command: `mvn -q verify`
   - Purpose: full unit/integration test pass plus JaCoCo thresholds; acts as the gating signal before exploring targeted suites.

2. **Deterministic Resource Lifecycle**
   - Command: `mvn -q -Dtest=BackgroundResourceReleaserTest,ReferenceTracingIntegrationTest,ReferenceCountingFuzzTest test`
   - Focus: stress the asynchronous releaser, reference tracing diagnostics, and fuzzed reserve/release interleavings after the lifecycle refactor.

3. **Filesystem & Temp Directory Hardening**
   - Command: `mvn -q -Dtest=IOToolsTempDirectoryTest,IOToolsCreateDirectoriesTest,IOToolsTest test`
   - Focus: validates `IOTools` helpers across prefix/suffix handling, permission failures, and cleanup semantics tied to the new docs.

4. **ChronicleInit & System Property Ordering**
   - Command: `mvn -q -Dtest=ChronicleInitPrecedenceTest,JvmTest,JvmParseSizeTest test`
   - Focus: ensures init hooks still fire before any `Jvm` consumer, system property parsing remains backward compatible, and failure modes are logged.

5. **Time Provider Monotonicity & Clock Maths**
   - Command: `mvn -q -Dtest=SetTimeProviderTest,SystemTimeProviderTest,UniqueMicroTimeProviderTest test`
   - Focus: asserts the richer ISO parsing, auto-increment behaviour, and micro/nano time helpers stay monotonic under concurrent use.

6. **Threading & Cleaner Interactions**
   - Command: `mvn -q -Dtest=CleaningThreadLocalIntegrationTest,CleaningThreadLocalOrphanPruningTest,CleaningThreadTest test`
   - Focus: covers `CleaningThreadLocal` ownership, leak detection, and shutdown guarantees that tie back to CORE-FN-040+.

7. **Native / OS Boundary Coverage**
   - Command: `mvn -q -Dtest=OSPageAlignmentTest,OSMapAlignTest,ARMMemoryObjectOffsetMisalignmentTest,ARMMemoryAddIntAndMessagesTest test`
   - Focus: catches regressions in alignment logic, SAFE_PAGE_SIZE overrides, and ARM-specific memory helpers affected by the documentation update.

8. **Analytics & Telemetry Contracts**
   - Command: `mvn -q -Dtest=AnalyticsFacadeTest,AnalyticsFallbackTest,HistogramTest,RecordingHistogramTest test`
   - Focus: exercises the measurement APIs that downstream dashboards consume, ensuring the new `CORE-DATA-*` catalogue stays honoured.

9. **Exception Handling & Logging Contract**
   - Command: `mvn -q -Dtest=ThreadLocalisedExceptionHandlerTest,Slf4jExceptionHandlerTest,ChainedExceptionHandlerTest test`
   - Focus: verifies per-thread handler overrides, ANSI-free fallback logging, and chained handler pruning so `CORE-OPS-003` logging guarantees remain intact.

10. **Scoped Resources & Thread-Confinement Guard Rails**
   - Command: `mvn -q -Dtest=ScopedThreadLocalLifecycleTest,ScopedThreadLocalTest,WeakReferenceScopedResourceTest test`
   - Focus: validates bounded resource pools (strong vs weak), confirms overflow closes the newest instances, and surfaces cross-thread access via the confinement asserter.

11. **Build Metadata & System Property Sanity**
    - Command: `mvn -q -Dtest=InternalPomPropertiesMatrixTest,InternalPomPropertiesPresenceTest,InternalPomPropertiesCorruptTest,InternalPomPropertiesMixedFieldsTest,SystemPropertiesPrecedenceTest test`
    - Focus: ensures Maven metadata stays readable across corrupt/missing files and that `system.properties` loading honours CLI overrides while still picking up classpath resources.

> Run the matrix sequentially: stop immediately if a batch fails, capture the offending logs (in plain ASCII), fix forward, then restart from Batch 1.

## Proposed Batch Additions (to build)

## Execution Checklist
- `git status -sb` and confirm `adv/review2` is checked out with the latest changes pulled.
- Ensure `JAVA_HOME` targets a supported JDK 17 build; `mvn -version` should report the same runtime before invoking tests.
- Export any required `MAVEN_OPTS` (e.g. `-Xmx2g -XX:+UseG1GC`) to keep the fuzz tests stable on smaller boxes.
- Capture pass/fail plus notable log excerpts per batch to feed into PR descriptions or the release checklist.

## Upcoming Test Work
| ID | Area | Goal | Plan |
|----|------|------|------|
| NT-1 | Temp directory permissions | Simulate read-only parents during `IOTools.createDirectories` to ensure AccessDeniedException carries the new descriptive message. | Add a JUnit test that `chmod 0555` on a temp parent (skip on Windows) and asserts the thrown message matches expectations. |
| NT-2 | Background releaser soak | Flood `BackgroundResourceReleaser` with thousands of mock closeables to verify `dumpQueue()` drains and counters reset. | Extend `BackgroundResourceReleaserTest` with a parameterised soak scenario guarded by `Assume.assumeTrue(!Jvm.isArm())` if necessary. |
| NT-3 | Nano-clock concurrency | Multi-threaded `SetTimeProvider.advanceNanos()` usage should remain monotonic and never regress. | New test harness using `ExecutorService` plus `AtomicLong` assertions; sits inside `src/test/java/net/openhft/chronicle/core/time`. |
| NT-4 | ChronicleInit failure paths | Ensure a `chronicle.init.runnable` that throws `SecurityException` logs the failure yet leaves `Jvm` initialised. | Add a bespoke runnable in `src/test/java/net/openhft/chronicle/core/init` and assert log capture plus fallback state. |
| NT-5 | QueueOffsetSpec adapter | Cover the builder helper that maps queue offset specs down in Core to guard against parsing regressions. | Introduce a lightweight Core-level test (without Chronicle Queue) that mimics the builder parsing and verifies invalid tokens. |

## Notes
- Keep build logs under `logs/` with timestamps if you need to share diagnostics; delete them before committing.
- When adding new tests, follow the AGENTS.md language rules and update the relevant AsciiDoc (functional/data requirements) in the same change set.
