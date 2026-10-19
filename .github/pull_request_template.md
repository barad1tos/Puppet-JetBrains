## Summary

<!-- Briefly describe what this PR changes. -->

## Changes

-

## Test Plan

- [ ] `./gradlew build --stacktrace`
- [ ] `./gradlew test --stacktrace`
- [ ] `./gradlew detekt ktlintCheck`
- [ ] `./gradlew verifyPlugin --stacktrace`
- [ ] `python3 -m unittest discover -s tools -p 'test_*.py'`
- [ ] `python3 scripts/verify-docs.py`

## Related Issues

<!-- Fixes #123, Closes #456, or leave blank. -->

## Checklist

- [ ] One logical change only.
- [ ] Grammar changes come with parser fixtures or regression tests.
- [ ] Language data was regenerated with `tools/generate-openvox-data.py`, not edited by hand (except `facts.tsv`).
- [ ] Marketplace-facing text avoids unsupported affiliation, compatibility, or feature claims.
