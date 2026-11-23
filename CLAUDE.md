# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An IntelliJ IDEA plugin that sorts classes in the Project view by an `@Order(n)` annotation (matched by simple name `Order`, so Spring's or a custom one both work):

- Package with no annotated class → default ordering, nodes untouched.
- Otherwise → annotated classes first (ascending value, ties by name), then non-annotated by name. `@Order` with no/uncomputable value = `Integer.MAX_VALUE` (Spring semantics).

## Architecture (`src/main/java/com/korenevskiy/orderplugin`)

- `OrderTreeStructureProvider` — `treeStructureProvider` (`order="last"`, after the Java provider creates class nodes). For children of a `PsiDirectoryNode`, replaces stock `ClassTreeNode`/`PsiFileNode` (exact class match only) with subclasses from `OrderedNodes` that carry an `OrderSortKey`.
- Why subclass nodes instead of reordering the list: the Project view re-sorts after providers via `GroupByTypeComparator`, which uses `getSortKey()` (sort by name) / `getTypeSortKey()` (sort by type). Its `compare` catches `ClassCastException` for mixed key types, so foreign nodes are safe. Sort-by-time and manual order intentionally override ours.
- `OrderAnnotations` — reads the value via PSI + constant evaluation; avoids reference resolution so it works in dumb mode (`IndexNotReadyException` → default).
- `OrderAnnotationChangeListener` — PSI listener; refreshes the containing directory when a modifier list changes (the view otherwise only refreshes the edited node, not siblings).
- `DumbModeRefresher` — refreshes the view after indexing so constant-valued `@Order(X.CONST)` resolve.

## Build

Gradle (wrapper committed) + IntelliJ Platform Gradle Plugin 2.x, Java 21. Target IDE version is `platformVersion` in `gradle.properties` (or `-PlocalIdePath=...`); `sinceBuild = 261`.

```bash
./gradlew build                                              # compile + test + plugin zip (build/distributions)
./gradlew test                                               # tests (JUnit 4, LightJavaCodeInsightFixtureTestCase)
./gradlew test --tests '*OrderTreeStructureProviderTest.testEqualOrderFallsBackToName'
./gradlew runIde                                             # sandbox IDE with the plugin installed
```

Release: version is `pluginVersion` in `gradle.properties`; change notes live in `build.gradle.kts` (`pluginConfiguration.changeNotes`); Marketplace description is in `plugin.xml`. Signing/publishing read `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, `PUBLISH_TOKEN` from the environment — never commit them. See README for the release steps. `sample-project/` is a manual-test fixture opened by `runIde`, not part of the build.

Tests run the registered providers over a package node and sort with `GroupByTypeComparator`, mirroring the real Project view.
