# org.numerals

A small library for generating the cardinal name of a number ("numerals to words"), usable both as a command-line tool and as a library from your own code.

Supported languages, selected by locale: Spanish (`es`), English (`en`, plus British English `en_GB` with the "and" connector), Portuguese (`pt`), Italian (`it`), Catalan (`ca`), Galician (`gl`), Romanian (`ro`), Occitan (`oc`), Asturian (`ast`), and Aragonese (`an`). Maximum length: 24 digits.

## Requirements

- Java 21 or newer
- No separate Gradle install required: the project ships the Gradle wrapper (`gradlew` / `gradlew.bat`)

## Quick start

Add the library as a dependency from Maven Central (see [Use as a library](#use-as-a-library)):

```groovy
implementation 'io.github.josuemb:numerals:0.1.0'
```

Or clone and build the command-line tool from source:

```bash
git clone https://github.com/josuemb/org.numerals.git
cd org.numerals
./gradlew distZip
```

Unzip the generated package and run the console tool (see [Usage](#usage) below).

## Build

The project uses Gradle. Use the bundled wrapper (`./gradlew` on Linux/macOS, `gradlew.bat` on Windows); if you prefer a system-wide install, follow the [Gradle installation guide](https://docs.gradle.org/current/userguide/installation.html) and replace `./gradlew` with `gradle`.

Build a distributable package for console use:

- Zip package:

  ```bash
  ./gradlew distZip
  ```

- Tar GZ package:

  ```bash
  ./gradlew distTar
  ```

The package is written under `build/distributions/`. Uncompress it (zip or tar gz) to obtain a runnable directory named `numerals-<version>` containing a `bin/` directory with the launcher scripts.

## Usage

After uncompressing the distribution, run the console version from its `bin/` directory:

- Windows:

  ```bat
  numerals number
  ```

- Linux / macOS / other:

  ```bash
  ./numerals number
  ```

Example (Linux execution with the JVM default locale set to `es_MX`):

```text
user@host:/tmp/numerals-0.1/bin$ ./numerals 123

[123]=[ciento veintitres]
```

## Use as a library

The library is published to Maven Central under the coordinates `io.github.josuemb:numerals`. Add it to your build:

- Gradle (Groovy DSL):

  ```groovy
  implementation 'io.github.josuemb:numerals:0.1.0'
  ```

- Gradle (Kotlin DSL):

  ```kotlin
  implementation("io.github.josuemb:numerals:0.1.0")
  ```

- Maven:

  ```xml
  <dependency>
      <groupId>io.github.josuemb</groupId>
      <artifactId>numerals</artifactId>
      <version>0.1.0</version>
  </dependency>
  ```

To generate numerals from your own code, use `org.numerals.CardinalUtil`. See its javadoc for the available methods and details on how to call it.
