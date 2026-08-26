# org.numerals

A small library for generating the cardinal name of a number ("numerals to words"), usable both as a command-line tool and as a library from your own code.

This is a pure-Java implementation with no runtime dependencies. It began as a Groovy project; that original Groovy implementation, with its full commit history, lives at [josuemb/groovy-numerals](https://github.com/josuemb/groovy-numerals). This repository is the Java port and is the version published to Maven Central.

Supported languages, selected by locale: Spanish (`es`), English (`en`, plus British English `en_GB` with the "and" connector), Portuguese (`pt`), Italian (`it`), Catalan (`ca`), Galician (`gl`), Romanian (`ro`), Occitan (`oc`), Asturian (`ast`), and Aragonese (`an`). Maximum length: 24 digits.

## Requirements

- Java 21 or newer
- No separate Maven install required to build from source if you have Maven available; the project is a standard Maven build (`mvn`)

## Quick start

Add the library as a dependency from Maven Central (see [Use as a library](#use-as-a-library)):

```xml
<dependency>
    <groupId>io.github.josuemb</groupId>
    <artifactId>numerals</artifactId>
    <version>0.2.0</version>
</dependency>
```

Or clone and build the command-line tool from source:

```bash
git clone https://github.com/josuemb/org.numerals.git
cd org.numerals
mvn package
```

This produces a runnable command-line jar at `target/numerals-cli.jar` (see [Usage](#usage) below).

## Build

The project uses Maven. Run the standard lifecycle:

- Run the tests:

  ```bash
  mvn test
  ```

- Build the jars:

  ```bash
  mvn package
  ```

`mvn package` writes two artifacts under `target/`:

- `numerals.jar` — the library jar.
- `numerals-cli.jar` — a self-contained runnable jar for the console tool.

## Usage

After building, run the console version with the runnable jar:

```bash
java -jar target/numerals-cli.jar <number> [--locale <language[_COUNTRY]>]
```

The `--locale` flag selects the language; when omitted, the JVM default locale is used.

Example:

```text
$ java -jar target/numerals-cli.jar 123 --locale es
[123]=[ciento veintitrés]
```

## Use as a library

The library is published to Maven Central under the coordinates `io.github.josuemb:numerals`. Add it to your build:

- Maven:

  ```xml
  <dependency>
      <groupId>io.github.josuemb</groupId>
      <artifactId>numerals</artifactId>
      <version>0.2.0</version>
  </dependency>
  ```

- Gradle (Kotlin DSL):

  ```kotlin
  implementation("io.github.josuemb:numerals:0.2.0")
  ```

- Gradle (Groovy DSL):

  ```groovy
  implementation 'io.github.josuemb:numerals:0.2.0'
  ```

To generate numerals from your own code, use `org.numerals.CardinalUtil`. See its javadoc for the available methods and details on how to call it.

## History and related projects

- [josuemb/groovy-numerals](https://github.com/josuemb/groovy-numerals) — the original Groovy implementation this project was ported from, preserving the full commit history from 2011 onward.
