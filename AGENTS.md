# AGENTS.md

## Scope
- Chronicle-Core provides core infrastructure and utilities for Chronicle libraries.
- Keep changes focused, reviewable, and performance-aware.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Module-scoped example:
  - `mvn -pl <module> -am verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -Dtest=ClassName test -l logs/mvn-test.log`
- Assertions profile:
  - `mvn clean install -Passertions -l logs/mvn-assertions.log`
- Benchmarks:
  - `mvn clean install -Prun-benchmarks -l logs/mvn-bench.log`
- CPD check:
  - `mvn -q pmd:cpd-check -l logs/mvn-cpd.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Repo map
- Multi-release JAR layout: Java 8 in `src/main/java`, Java 11+ in `src/main/java11`.
- Docs and decision logs live under `src/main/docs/`.

## Constraints
- Java baseline: 8 (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs; binary compatibility is enforced during `verify`.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.
- Release resources deterministically and document ownership.
- For new resources, extend `AbstractCloseable` or `AbstractReferenceCounted` and document threading expectations.
- Call `singleThreadedCheckReset()` when handing work between threads.

## Docs and review checklist
- Keep AsciiDoc, tests, and code in sync; update `.adoc` files when behaviour changes.
- Javadoc must add behavioural contracts, edge cases, thread safety, units, or performance notes.
- For large mechanical changes, declare the transformation rule and keep it consistent.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and decision record templates.
- `src/main/docs/project-requirements.adoc` and `src/main/docs/decision-log.adoc`.
