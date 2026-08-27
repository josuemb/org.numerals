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
java -jar target/numerals-cli.jar <number> [<number> ...] [--locale <language[_COUNTRY]>]
```

The `--locale` flag selects the language and may appear anywhere in the argument
list, as `--locale es` or `--locale=es`; the value accepts both `es` and
`es_MX` / `es-MX`. When omitted, the JVM default locale is used. Pass `--help`
(or `-h`) for usage and examples.

Example:

```text
$ java -jar target/numerals-cli.jar 45789 --locale es
[45789]=[cuarenta y cinco mil setecientos ochenta y nueve]
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

To generate numerals from your own code, use `org.numerals.CardinalEngine`:

```java
import org.numerals.CardinalEngine;
import java.util.Locale;

String words = CardinalEngine.cardinal("45789", new Locale("es", "MX"));
// -> "cuarenta y cinco mil setecientos ochenta y nueve"
```

See its javadoc for the available methods and details on how to call it.

### Thread safety

`CardinalEngine` is thread-safe: `cardinal(...)` may be called concurrently from
any number of threads with no external synchronization. Generation holds no
mutable per-call state, so CPU-bound bulk conversion can be parallelized
directly:

```java
List<String> words = numbers.parallelStream()
    .map(n -> CardinalEngine.cardinal(n, locale))
    .toList();
```

## Java vs Groovy

This project is a pure-Java port of the original Groovy implementation
([josuemb/groovy-numerals](https://github.com/josuemb/groovy-numerals)). Both
produce identical output — verified byte-for-byte across a dense 0–100000 sweep
plus the full digit scale, in all 11 locales — but the Java version is a
considerably lighter and faster artifact:

| Aspect                 | Java              | Groovy                  |
| ---------------------- | ----------------- | ----------------------- |
| Library jar size       | ~66 KB            | ~253 KB                 |
| Runtime dependencies   | none              | groovy runtime (~7.7 MB)|
| Total footprint to run | ~66 KB            | ~8 MB                   |
| Throughput (100k nums) | ~1.5M numbers/s   | ~69K numbers/s          |

The Java jar has no runtime dependency, so consumers download tens of kilobytes
rather than the multi-megabyte Groovy runtime. In a warm-JIT micro-benchmark
generating the Spanish cardinal of 0–99999 (single thread, same JVM), the Java
version was roughly 20x faster; the gap comes from static dispatch and lambda
inlining versus Groovy's dynamic method resolution. The throughput numbers are
from an informal micro-benchmark, not JMH, so treat them as an order of
magnitude rather than exact figures.

## History and related projects

- [josuemb/groovy-numerals](https://github.com/josuemb/groovy-numerals) — the original Groovy implementation this project was ported from, preserving the full commit history from 2011 onward.
