# X12 Parser

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=imsweb_x12-parser&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=imsweb_x12-parser)
[![integration](https://github.com/imsweb/x12-parser/workflows/integration/badge.svg)](https://github.com/imsweb/x12-parser/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.imsweb/x12-parser.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.imsweb/x12-parser)

A parser for ANSI ASC X12 documents. This project was originally based on the Python project [pyx12](https://github.com/azoner/pyx12).

The library takes ANSI X12 claim files and reads the data into a Loop object that replicates the X12 structure described in the standard's specifications. The current supported standards of X12 that
are supported are :

- ANSI 835 5010 X221
- ANSI 835 4010 X091
- ANSI 837 4010 X096
- ANSI 837 4010 X097
- ANSI 837 4010 X098
- ANSI 837 5010 X222
- ANSI 837 5010 X223
- 999 5010
- 277 5010 X214
- 277 5010 X212
- 270 4010 X092
- 271 4010 X092

The layouts for these standards are specified in XML mapping files contained in the mapping directory. The structure of the Loop object will match the structure specified in the mapping files for
the X12 standard you are processing.

## Download

Java 8 is the minimum version required to use the library.

Download [the latest JAR][1] or grab via Maven:

```xml

<dependency>
    <groupId>com.imsweb</groupId>
    <artifactId>x12-parser</artifactId>
    <version>1.16</version>
</dependency>
```

or via Gradle:

```groovy
compile 'com.imsweb:x12-parser:1.16'
```

[1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.imsweb&a=x12-parser&v=LATEST

An example of how to process an X12 file is shown below

## Processing a file

```java
X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File("/path/file.txt"));
```

Each supported X12 standard has a FileType option that must be passed as the first argument to the X12Reader. In this example, an 837 5010 X222 file is being processed. If there are errors in the
document structure you can review them as follows:

```java
List<String> errors = reader.getErrors();
```

There may be errors in the structure that are severe enough that they prevent proper processing of the file. You can access these as follows:

```java
List<String> errors = reader.getFatalErrors();
```

## Accessing Data

You can access the data from the file using:

```java
List<Loop> loops = reader.getLoops();
```

Each individual ISA-IEA transaction is one element in the list. If a file contains only a single ISA-IEA transaction then the length of the list is 1. You can access data further down in the X12
structure
as follows:

```java
String data = loop.getLoop("ISA_LOOP")
        .getLoop("GS_LOOP")
        .getLoop("ST_LOOP")
        .getLoop("1000A")
        .getSegment("NM1")
        .getElement("NM101")
        .getSubElement(1);
```

In this example, GS_LOOP is a subloop of ISA_LOOP, ST_LOOP is a subloop of GS_LOOP and so on. NM1 is a segment of loop 1000A. NM101 is the first element of segment NM1, as indicated by the 01 appened
to NM1. The fourth element of NM1 would be NM104. This code is grabbing the first sub-element in that element. The loop and segment names are all specified in the mapping files. If an element does not
have sub-elements, you can access the element value using:

```java
String data = loop.getLoop("ISA_LOOP")
        .getLoop("GS_LOOP")
        .getLoop("ST_LOOP")
        .getLoop("1000A")
        .getSegment("NM1")
        .getElementValue("NM101");
```

It's possible for loops and segments to repeat multiple times. Here is an example of how to access a particular repeated loop or segment

```java
Loop loop = loop.getLoop("1000A", 1);
Segment segment = loop.getSegment("NM1", 2);
```

This gets the first iteration of the 1000A subloop an the second instance of the NM1 segment within the 1000A loop. If no iteration index is specified in getLoop() or getSegment() then the first
iteration is grabbed.

You can also search for all loops with a particular ID that is either a subloop of the current loop object or a subloop of on the current loop's subloops.

```java
Loop loop = loop.getLoop("1000A");
List<Loop> loops = loop.findLoop("1000B");
```

All loops with ID of 1000B that are contained with the 1000A loop structure are returned. The 1000B loops will either be a direct subloop of 1000A or a subloop of one 1000A's subloops. A 1000B will be
returned event if it's contained deep within the loop structure. The same can be done for segments.

```java
List<Segment> segments = loop.findSegment("NM1");
```

## Creating and Writing an X12 File

It is also possible to create a loop object and then write the contents to a file. Here is an example of creating a loop with a segment.

```java
Loop isaLoop = new Loop("ISA_LOOP");

Segment segment = new Segment("ISA");
segment.addElement("01", "00");
segment.addElement("02", "          ");
segment.addElement("03", "01");
segment.addElement("04", "SECRET    ");
segment.addElement("05", "ZZ");
segment.addElement("06", "SUBMITTERS.ID  ");
segment.addElement("07", "ZZ");
segment.addElement("08", "RECEIVERS.ID   ");
segment.addElement("09", "030101");
segment.addElement("10", "1253");
segment.addElement("11", "U");
segment.addElement("12", "00501");
segment.addElement("13", "000000905");
segment.addElement("14", "1");
segment.addElement("15", "T");
segment.addElement("16", ":");
isaLoop.addSegment(segment);

segment = new Segment("IEA");
segment.addElement("01", "1");
segment.addElement("02", "000000905");
isaLoop.addSegment(segment);
```

Subsequent segments and subloops could then be appended to the ISA loop. This data can then be written to a string as follows:

```java
X12Writer writer = new X12Writer(FileType.ANSI837_5010_X222,Collections.singletonList(isaLoop), separators);

String writerResult = writer.toX12String(lineBreak).trim();
```

The string can also be written to a file if needed.
