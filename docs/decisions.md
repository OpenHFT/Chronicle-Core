# Decisions

This log records **decisions to _not_ do something** — deliberate choices to park, defer, or scope
down work, and notable "chose approach A over B" calls — so the reasoning is captured where the code
lives and is not silently re-litigated.

**When to add an entry:** whenever an issue/PR is deliberately not progressed to its full scope, or a
design fork is settled. Any repo adopts this file on its first such decision.

**Rules:** append-only — never delete or rewrite an entry; supersede it with a new dated one. For a
weighty decision needing the long form, use the template at
`Open-Understanding/templates/decision-record.md` and link it here.

**Entry format:** `### DEC-NN — <title>` with Status · Date · Issue/PR · Decider, then Decision ·
Options considered (incl. do-nothing) · Implemented vs deliberately not done · What would change this ·
Evidence.

---

### DEC-01 — Core #331: retain the finalize() safety nets (do not remove)
- **Status:** accepted · **Date:** 2026-08-20 · **Issue/PR:** Chronicle-Core#331 · branch `feat/Chronicle-Core-331-inventory-remaining-finalisers-sch` · **Decider:** Peter Lawrey
- **Decision:** Keep the finalize()-based safety-net finalisers; when finalize() is removed, re-home them on `java.lang.ref.Cleaner` — do **not** drop the leak protection.
- **Options considered:** (A) remove the finalisers (issue as worded) — *rejected* (loses un-happy-path leak protection); (B) retain now, re-home on `Cleaner` when finalize is removed — *accepted*; (C) keep finalize() indefinitely — rejected (deprecated for removal).
- **Implemented vs deliberately not done:** shipped a finaliser inventory (3 sites), a `--finalization=disabled` probe, and retention-rationale javadoc (`e6c29286`). Deliberately not done: removing the safety nets; the `Cleaner` re-homing (future, gated on real finalize removal); replacing the test-only detector `gcAndWaitForCloseablesToClose()`.
- **What would change this:** nothing changes the retention; the JDK actually removing finalization only changes *timing* (promotes the `Cleaner` re-homing to "now").
- **Evidence:** the happy-path tests pass without finalization, but the nets guard the un-happy path — a production caller that forgot to close leaks a file descriptor (see `report.md`, `FinalizeProbe.java`).
