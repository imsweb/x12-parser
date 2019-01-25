# X12 Parser 

[![CircleCI](https://circleci.com/gh/imsweb/x12-parser.svg?style=svg)](https://circleci.com/gh/imsweb/x12-parser)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/x12-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/x12-parser)

A parser for ANSI ASC X12 documents.  This project was originally based on the Python project [pyx12](https://github.com/azoner/pyx12).

The library supports the following file types:

- ANSI 835 5010 X221
- ANSI 835 4010 X091
- ANSI 837 4010 X096
- ANSI 837 4010 X097
- ANSI 837 4010 X098
- ANSI 837 5010 X222
- ANSI 837 5010 X223

## Download

Java 8 is the minimum version required to use the library.

Download [the latest JAR][1] or grab via Maven:

```xml
<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>x12-parser</artifactId>
    <version>1.4</version>
</dependency>
```

or via Gradle:

```groovy
compile 'com.imsweb.com:x12-parser:1.4'
```

[1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.imsweb&a=x12-parser&v=LATEST
