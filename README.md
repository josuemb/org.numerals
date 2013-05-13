org.numerals
============

API for easy Numerals Generation from console or API use.

Currently we support languages: SpanishAnd English with a maximum length of 24 digits.

BUILDING:

For building packages we need Gradle Build Tool.

Please download and istall it: http://www.gradle.org/docs/current/userguide/installation.html

If you are using Linux/Unix or any other OS with Bash shell support, maybe the besto option will be: http://gvmtool.net/

After install gradle, you can build for console use:

  Zip package:
    gradle distZip

  Tar GZ package:
    gradle distTar

  Now, you can uncompress the file (zip either tar gz) and obtain a directory.

EXECUTING:

  Now you can execute the console version as:
    Windows:
      numerals number

    Other:
  numerals number

  Example (Linux execution with JVM default locale as ES_MX):

  user@host:/tmp/numerals-0.1/bin$ ./numerals 123
  
  [123]=[ciento veintitres]

CONSUMING:

  Use org.numerals.CardinalUtil and see javadoc for using it.
