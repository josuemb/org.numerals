# Contributing to org.numerals

Thank you for your interest in contributing. org.numerals is a Java 21 / Groovy 5.1 library (with a CLI) that converts numbers into their cardinal words across multiple locales. This guide explains how to build the project, how the code is organized, and how to add or extend language support.

## Building and testing

The project builds with Gradle and targets JDK 21.

```
./gradlew clean test
```

All tests must pass before a change can be merged. Run the full suite locally before opening a pull request.

## Project layout

The library is organized by locale. Each supported language has:

- A rules class at `src/main/groovy/org/numerals/rules/<locale>/CardinalRules.groovy`
- A matching edge-case test at `src/test/groovy/org/cardinalnumbers/rules/<locale>/CardinalEdgeCasesTest.groovy`

Currently supported locales: `es`, `en`, `en_GB`, `pt`, `it`, `ca`, `gl`, `ro`, `oc`, `ast`, `an`.

## Key convention: rules files are written in their own language

Each language's rules file is authored in that language. Identifiers, comments, and Javadoc in a locale's `CardinalRules.groovy` are written in the language that file implements: the Spanish rules are documented in Spanish, the Italian rules in Italian, and so on. This is a deliberate convention that keeps each ruleset readable and reviewable by native speakers of that language.

Note that this convention applies to the code around the data, not to the produced words themselves. The number words a locale generates follow that language's own grammar.

## Adding a new language

1. Create the rules class at `src/main/groovy/org/numerals/rules/<locale>/CardinalRules.groovy`, following the naming and structure of an existing locale that is grammatically close to the one you are adding.
2. Write the rules file in the target language, per the convention above.
3. Create the matching edge-case test at `src/test/groovy/org/cardinalnumbers/rules/<locale>/CardinalEdgeCasesTest.groovy`.
4. Verify the output empirically. Confirm the cardinal words against authoritative references for that language rather than by analogy with another locale, since number grammar (gender agreement, contractions, apocopation, conjunctions between tens and units) varies significantly between languages.
5. Keep scale magnitudes authentic to the language. Many languages use the long scale (where "billion" means a million million); do not assume the short scale. Use the naming and grouping that native usage and standard references define for that language.
6. Add tests that cover the boundaries: zero, single digits, teens, tens, hundreds, scale transitions (thousands, millions and above), and any irregular forms specific to the language.

## Style and formatting

- No emojis anywhere. Use plain text in code, comments, documentation, commit messages, and pull requests.
- Follow the existing conventions of neighboring files in the same locale.

## Workflow

1. Create a feature branch off `master`.
2. Make your change (add or extend a locale, fix a bug, improve docs).
3. Run `./gradlew clean test` and confirm the suite is green.
4. Open a pull request against `master` and fill in the pull request template.
5. If your change alters the set of supported languages, update the README accordingly.
