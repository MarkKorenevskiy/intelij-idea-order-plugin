# Order plugin sample project

Open with `./gradlew runIde` from the plugin root (this project opens automatically),
then expand `src/main/java/com/example` in the Project view.

Expected order:

| Package      | Expected order                                                              |
|--------------|-----------------------------------------------------------------------------|
| `mixed`      | `sub`, Delta (-999), Charlie (10), Echo (20), Foxtrot (20), Bravo (30), Golf (no value), AaaPlain, Alpha |
| `allordered` | Date (-1), Cherry (1), Banana (2), Apple (3)                                |
| `plain`      | Apricot, Kiwi, Mango, Zebra (default, no annotations)                       |

Things to try:
- Change `@Order(30)` on `Bravo` to `@Order(0)` — it should jump above Charlie.
- Add `@Order(1)` to a class in `plain` — the package switches to ordered mode.
- Remove `@Order` from `Delta` — it drops into the non-annotated group.
- Toggle Project view ⚙ → Sort by Type — order should stay the same.
