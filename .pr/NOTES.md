# PR: Chronicle-Core#331 (VALID_FEATURE)

**Track C — Feature/docs PR**  ·  priority tier 6  ·  base `ea`  ·  branch `feat/Chronicle-Core-331-inventory-remaining-finalisers-sch`
Issue: https://github.com/OpenHFT/Chronicle-Core/issues/331

## Planned change
Inventory remaining finalisers; schedule Cleaner/phantom migration for first post-Java-8 release; test with finalisation disabled.

## Quality bar (must clear before this PR merges)
- One issue, one PR; link with `Fixes #331`; scope limited to this issue.
- Regression test that fails before / passes after (re-enable the ignored test where one exists, else add one).
- Cross-repo discipline: Chronicle-Queue exposes integration points only; retention/roll-maintenance policy lives in CQE.
- Do not fork the in-flight QUEUE-143/144 PRs; branch off current `ea` and rebase.
- Docs + changelog updated; author credit retained on any rebase; CI proven green.

## Status
Ready to implement on this branch.
_This PR-NOTES commit is the local addressment scaffold; the code change lands on top._
