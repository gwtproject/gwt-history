![GWT3/J2CL compatible](https://img.shields.io/badge/GWT3/J2CL-compatible-brightgreen.svg)

# GWT History

A future-proof port of the `com.google.user.history.History` GWT module,
with no dependency on `gwt-user` (besides the Java Runtime Emulation),
to prepare for GWT 3 / J2Cl.


##  Migrating from `com.google.gwt.user.history.History`

1. Add the dependency to your build.

   For Maven:

   ```xml
   <dependency>
     <groupId>org.gwtproject.user.history</groupId>
     <artifactId>gwt-history</artifactId>
     <version>HEAD-SNAPSHOT</version>
   </dependency>
   ```

   For Gradle:

   ```gradle
   implementation("org.gwtproject.user.history:gwt-historyr:HEAD-SNAPSHOT")
   ```

2. Update your GWT module to use

   ```xml
   <inherits name="org.gwtproject.user.history.History" />
   ```

3. Change your `import`s in your Java source files:

   ```java
   import org.gwtproject.user.history.client.History;
   ```

## Instructions

To build gwt-history:

* run `mvn clean install`

on the parent directory.

To run the j2cl tests:

* switch to the 'gwt-history-j2cl-tests' directory
* run `mvn j2cl:clean` & `mvn j2cl:test`


## System Requirements

**GWT Timer requires GWT 2.9.0 or newer!**


