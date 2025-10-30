# Chronicle Core Follow-ups

- [ ] CORE-SB-201: Replace Netty-style GC retry in `OS.map0` once segmented mapping lands (track under CORE-NF-P-201).
- [ ] CORE-SB-202: Rework `CpuCoolers.SERIALIZATION` to avoid `XMLDecoder` when the load harness is refactored (CORE-TEST-202).
- [ ] CORE-SB-203: Introduce injectable clock for `SystemTimeProvider` so static field can become final (CORE-TEST-203).
- [ ] CORE-PMD-301: Restore stricter Checkstyle/PMD coverage after legacy clean-up; revisit thresholds and rule set (CORE-DOC-301).
- [ ] CORE-COV-001: Lift JaCoCo gates from the interim 0.73/0.62 line/branch targets to the standard 0.80/0.70 once new tests land.
- [ ] CORE-PMD-302: Re-enable NullAssignment/AvoidInstantiatingObjectsInLoops once legacy reference-counting and interner refactors complete.
