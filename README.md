# Order Annotation Sorting

An IntelliJ IDEA plugin that sorts classes in the Project view by their `@Order(n)` annotation.

- A package with no annotated classes keeps the IDE's default ordering.
- Otherwise, annotated classes come first by ascending value (ties are sorted by name), followed by
  non-annotated classes and other files, sorted by name.
- Any annotation named `Order` is recognized, such as Spring's `@Order` or your own. Values can be literals or
  constant expressions. `@Order` with no value counts as `Integer.MAX_VALUE`.

Requires IntelliJ IDEA 2026.1 (build 261) or later with the Java plugin.

## Development

```bash
./gradlew test       # run tests
./gradlew runIde     # start a sandbox IDE with the plugin and the sample-project/ opened
./gradlew buildPlugin  # build build/distributions/order-annotation-plugin-<version>.zip
```

## Publishing

1. Bump `pluginVersion` in `gradle.properties` and add an entry to `changeNotes` in `build.gradle.kts`.
2. Check compatibility: `./gradlew verifyPlugin` (downloads the recommended IDE builds and runs the Plugin Verifier).
3. Upload the **first** version manually at <https://plugins.jetbrains.com/plugin/add>. There you choose the
   license and add the source code URL and the other listing details.
4. Publish later versions from Gradle. The signing key and token are read from environment variables:

   ```bash
   export CERTIFICATE_CHAIN="$(cat chain.crt)"     # https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
   export PRIVATE_KEY="$(cat private.pem)"
   export PRIVATE_KEY_PASSWORD="..."
   export PUBLISH_TOKEN="..."                       # https://plugins.jetbrains.com/author/me/tokens
   ./gradlew publishPlugin                          # builds, signs and uploads
   ```
