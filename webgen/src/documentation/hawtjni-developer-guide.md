# HawtJNI Developer Guide

* Table of contents
{:toc}

## Features

* Automatic mapping from Java to native functions, with simple mappings for all primitive data types
* Automatic conversion between C and Java strings
* Structure and Union arguments/return values
* Function Pointers, (callbacks from native code to Java) as arguments and/or members of a struct
* Nested structures
* Native long support (32 or 64 bit as appropriate) 
* Customizable mapping from Java method to native function name
* Maven integration:
  * Generates an autoconf and msbuild projects for the the native library so it can be built on other platforms using the native tool chain.
  * Native library is built before the test phase, so you can unit tests your JNI classes.
  * Built native library is packaged as a jar resource and deployed to the maven repo for easy distribution of native code.

## Getting Started with HawtJNI

Implementing JNI libraries is a piece of cake when you use HawtJNI.  It will code generate 
the JNI code needed to access the native methods defined in your Java classes.  Lets say you 
wanted to access to the c library's classic `printf` function:

{pygmentize:: c}
int printf(const char *format, ...);
{pygmentize}

To do that, you would only need to define a simple Java class like:

{pygmentize:: java}
import org.fusesource.hawtjni.runtime.*

@JniClass
public class Simple {
    
  private static final Library LIBRARY = new Library("simple", Example.class);    
 static {
    LIBRARY.load();
 }

 public static native int printf(String message);
}
{pygmentize}


That's it.  No JNI coding required.  It's composed of a static class initializer
and a native method interface definition for the `printf` function.  For folks
who have done JNI before this looks familiar except for the way the library is loaded.
You could have also loaded the library the traditional textbook way:

{pygmentize:: java}
 static {
    System.loadLibrary("simple");
 }
{pygmentize}

The benefit of using the Library class to load the native library is that it can
automatically unpack the native library from a jar resource and use that so 
that you don't have to worry installing it to the java library path.

The HawtJNI build process will take care of implementing your `simple` jni library
by using some maven tooling which we will cover in the next example.

## Building Your Library with Maven

If you are not familiar with Maven, please checkout [Maven by Example](http://www.sonatype.com/books/mvnex-book/reference/public-book.html).

The HawtJNI provides a maven plugin which makes it easy code generate and build
the native library for your current platform.  

Once you have a working maven build for a java module, you can then update
it to use HawtJNI to generate your JNI libraries.  With the following steps:

1.  Add the hawtjni repositories to the `pom.xml` configuration.
    {pygmentize:: xml}
    <pom>
      <repositories>
        ...
        <repository>
          <id>hawtjni.snapshot.fusesource.org</id>
          <name>HawtJNI Snapshot Repo</name>
          <url>http://hawtjni.fusesource.org/repo/snapshot</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>    
        ...
      </repositories>
      <pluginRepositories>
        ...
        <pluginRepository>
          <id>hawtjni.snapshot.fusesource.org</id>
          <name>HawtJNI Snapshot Repo</name>
          <url>http://hawtjni.fusesource.org/repo/snapshot</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </pluginRepository>
        ...
      </pluginRepositories>
    <pom>
    {pygmentize}

2.  Add the `hawtjni-runtime` dependency to pom. This small 19k jar file contains the HawtJNI annotations and a few Helper classes that
    you will be developing against.
    {pygmentize:: xml}
    <pom>
      <dependencies>
        ...
        <dependency>
          <groupId>org.fusesource.hawtjni</groupId>
          <artifactId>hawtjni-runtime</artifactId>
          <version>{project_version:}</version>
        </dependency>
        ...
      </dependencies>
    <pom>
    {pygmentize}
    
3.  Add the HawtJNI maven plugin to the pom.
    {pygmentize:: xml}
    <pom>
      <build>
        <plugins>
          ...
          <plugin>
            <groupId>org.fusesource.hawtjni</groupId>
            <artifactId>maven-hawtjni-plugin</artifactId>
            <version>{project_version:}</version>
            <executions>
              <execution>
                <goals>
                  <goal>generate</goal>
                  <goal>build</goal>
                  <goal>package-jar</goal>
                  <goal>package-source</goal>
                </goals>
              </execution>
            </executions>        
          </plugin>
          ...
        </plugins>
      </build>
    <pom>
    {pygmentize}
    
4.  If you run into problems with `mvn clean` no deleting native files, make sure you are using at least version 2.3 of the maven clean plugin.  Previous
    versions would run into problems with symlinks.
    {pygmentize:: xml}
    <pom>
      <build>
        <plugins>
          ...
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-clean-plugin</artifactId>
            <version>2.3</version>
          </plugin>
          ...
        </plugins>
      </build>
    <pom>
    {pygmentize}
    

You can now run `mvn install` to build the HawtJNI native code and even test that JNI integration.  Once it's done, in addition to the normally
produced `target/${artifactId}-${version}.jar` file that maven generates of a java module, you will also get `target/${artifactId}-${version}-native-src.zip`
and a `target/${artifactId}-${version}-${platform}.jar`.

The `target/${artifactId}-${version}-native-src.zip` file is a source archive of a autoconf and msbuild build project for the native library so that
you can easily built on other platforms using the native build tools (maven not required).

The `target/${artifactId}-${version}-${platform}.jar` is the platform specific jar which contains the your build JNI library as a resource.

Both these files get deployed to the maven repository so that other users can easily use them as dependencies in their builds.

### Generation Details

You may have noticed that HawtJNI is generating a slew of code in different directories.  Here is a breakdown of what gets generated where
and during which maven build phase:

1. __process-classes__: Processes you annotated java classes looking for HawtJNI annotations, and:
  1. generates JNI .c and .h files to `target/generated-sources/hawtjni/native-src`
  2. generates an autoconf and msbuild build project in the `target/generated-sources/hawtjni/native-package` directory
2. __generate-test-resources__: Builds the native library using the generated build project.
  1. Copies and builds the proxy in `target/native-build`
  2. Built libraries are installed to `target/native-dist` 
  3. The libraries are also copied to `target/generated-sources/hawtjni/lib` which gets added as a test resource path.  This way your unit tests can
     run against the native library.
3. __package-jar__: The contents of `target/generated-sources/hawtjni/lib` get jarred and attached to the maven build with a platform specific classifier.
4. __package-source__: The contents of `target/generated-sources/hawtjni/native-package` get zipped up into a platform agnostic source package for building the native library.

## Native Method Mapping

HawtJNI looks for all classes annotated with `@JniClass`.  For every static native method found it will generate the corresponding JNI function which calls
a the platform function of the same name as the java method.  Without additional configuration it will automatically convert the java method
arguments and return type to the natural native equivalents.  Customizations are performed by using the `@JniMethod` and `@JniArg` annotations.

### Default Type Mappings

The java primitive types are mapped by default to the corresponding type of the same size on the platform.

| Java Type   | Native Type   | Description            | Windows Types               |
|-------------|---------------|------------------------|-----------------------------|
| `byte`      | `char`        | 8-bit integer          | `BYTE`, `TCHAR`             |
| `short`     | `short`       | 16-bit integer         | `WORD`                      |
| `char`      | `wchar_t`     | 16 or 32-bit character | `TCHAR`                     |
| `int`       | `int`         | 32-bit integer         | `DWORD`                     |
| `long`      | `long long`   | 64-bit integer         | `LONG`                      |
| `boolean`   | `int`         | boolean value          | `BOOL`                      |
| `float`     | `float`       | 32-bit FP              |                             |
| `double`    | `double`      | 64-bit FP              |                             |

If primitive array type is used, it gets converted to the corresponding native array/pointer 
type.

| Java Type   | Native Type   | Description            | Windows Types               |
|-------------|---------------|------------------------|-----------------------------|
| `byte[]`    | `char*`       | 8-bit array            | `BYTE`, `TCHAR`             |
| `short[]`   | `short*`      | 16-bit array           | `WORD`                      |
| `char[]`    | `wchar_t*`    | 16 or 32-bit array     | `TCHAR`                     |
| `int[]`     | `int*`        | 32-bit array           | `DWORD`                     |
| `long[]`    | `long long*`  | 64-bit array           | `LONG`                      |
| `float[]`   | `float*`      | 32-bit FP array        |                             |
| `double[]`  | `double*`     | 64-bit FP array        |                             |
| `String`    | `char*`       | 8-bit array            | `LPTCSTR`                   |

### Improving Performance of methods passing Arrays or Structures

It's important to note that when dealing with arrays and structurs, HawtJNI must copy 
the contents of the java object to the native type since the JVM can any time move java objects
in memory.  It will then call the native function and then copy back
the native array back over the original java array so that the original java array picks up
any changes. 

If you have performance sensitive method, you might want to some of the following annotations
to the method argument:

* __`@JniArg(flags={NO_IN})`__:Indicate that a native method parameter is an out only variable. 
  This only makes sense if the parameter is a structure or an array 
  of primitives. It is an optimization to avoid copying the java 
  memory to C memory on the way in.
  
* __`@JniArg(flags={NO_OUT})`__: Indicate that a native method parameter is an in only variable. 
  This only makes sense if the parameter is a structure or an array 
  of primitives. It is an optimization to avoid copying the C memory 
  from java memory on the way out.
  
* __`@JniArg(flags={CRITICAL})`__: Uses a special JVM call which locks the java array in memory
  and disable garbage collection for the duration of the native call.  This is an optimization 
  to avoid copying memory and must only be used with low latency functions.  This only makes sense 
  if the parameter is an array
  



### Working with Pointers




<!--
| `long`      | `long long`   | 64-bit integer         | `__int64`                   |
-->
                                                
### Argument Options



## Using Platform Constants

## Native Structure Mapping

### Field Options

## Conditional Compilation

### Using Autoconf to Detect Platform Features

## Native Library Loading

