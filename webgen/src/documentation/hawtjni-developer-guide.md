# HawtJNI Developer Guide

* Table of contents
{:toc}

## Features {#features}

* Automatic mapping from Java to native functions, with simple mappings for all primitive data types
* Automatic conversion between C and Java strings
* Structure and Union arguments/return values
* Function Pointers, (callbacks from native code to Java) as arguments and/or members of a struct
* Nested structures
* Native long support (32 or 64 bit as appropriate) 
* Customizable mapping from Java method to native function name
* Maven integration:
  * Generates an autoconf and msbuild projects for the the native library so it can be built on other platforms using the native toolchain.
  * Native library is built before the test phase, so you can unit tests your JNI classes.
  * Built native library is packaged as a jar resource and deployed to the maven repo for easy distribution of native code.

## Getting Started with HawtJNI  {#getting-started}

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
    
  private static final Library LIBRARY = new Library("simple", Simple.class);    
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
automatically unpack the native library from a jar resource and use that so that
you don't have to worry installing it to the java library path.

The HawtJNI build process will take care of implementing your `simple` jni library
by using some maven tooling which we will cover in the next example.

## Building with Maven 

If you are not familiar with Maven, please checkout 
[Maven by Example](http://www.sonatype.com/books/mvnex-book/reference/public-book.html).

The easiest way to get started with HawtJNI is copy and use 
[the example maven project](http://github.com/chirino/hawtjni/tree/master/hawtjni-example/) as a template for your module.

At the root of the maven project run:
{pygmentize:: text}
mvn install
{pygmentize}

The maven build will produce the following artifacts:

* `target/${artifactId}-${version}.jar`: is the standard jar which maven produces that contains all your java classes. 
* `target/${artifactId}-${version}-native-src.zip`: is a source archive of
a autoconf and msbuild based build project for the native library.  You can
easily build this on other platforms using the platforms native toolchain.
* `target/${artifactId}-${version}-${platform}.jar` is the platform specific
jar which contains the your build JNI library as a resource.

These artifacts will be deployed to the maven repository so that other users can
easily use them as dependencies in their builds.

You may also be interesting in 
[How to Add HawtJNI to an Existing Maven Build](#adding-to-maven-build).

## Native Method Mapping {#method-mapping}

HawtJNI looks for all classes annotated with `@JniClass`. For every static
native method found, it will generate the corresponding JNI function which calls
a the platform function of the same name as the java method.

The JNI method mapping can be customized by applying the `@JniMethod` annotation
to the method and the `@JniArg` to each method argument.

### Default Type Mappings {#default-type-mappings}

Without additional configuration native methods automatically convert the
method arguments and return types to the corresponding type of the
same size on the platform.

| Java Type   | Native Type   | Description            | Windows Types   |
|-------------|---------------|------------------------|-----------------|
| `byte`      | `char`        | 8-bit integer          | `BYTE`, `TCHAR` |
| `short`     | `short`       | 16-bit integer         | `WORD`          |
| `char`      | `wchar_t`     | 16 or 32-bit character | `TCHAR`         |
| `int`       | `int`         | 32-bit integer         | `DWORD`         |
| `long`      | `long long`   | 64-bit integer         | `LONG`          |
| `boolean`   | `int`         | boolean value          | `BOOL`          |
| `float`     | `float`       | 32-bit FP              |                 |
| `double`    | `double`      | 64-bit FP              |                 |

If a primitive array type or String is used, it gets converted to the corresponding
native array/pointer type.

| Java Type   | Native Type   | Description            | Windows Types   |
|-------------|---------------|------------------------|-----------------|
| `byte[]`    | `char*`       | 8-bit array            | `BYTE`, `TCHAR` |
| `short[]`   | `short*`      | 16-bit array           | `WORD`          |
| `char[]`    | `wchar_t*`    | 16 or 32-bit array     | `TCHAR`         |
| `int[]`     | `int*`        | 32-bit array           | `DWORD`         |
| `long[]`    | `long long*`  | 64-bit array           | `LONG`          |
| `float[]`   | `float*`      | 32-bit FP array        |                 |
| `double[]`  | `double*`     | 64-bit FP array        |                 |
| `String`    | `char*`       | 8-bit array            | `LPTCSTR`       |

It's important to note that when dealing with arrays and structures, HawtJNI must
copy the contents of the java object to the native type since the JVM can any
time move java objects in memory. It will then call the native function and then
copy back the native array back over the original java array so that the original
java array picks up any changes.

## Mapping Native Structures {#mapping-native-structures}

You define a Java class for each native structure that you want map and replicate all the fields that you will need to access as regular java fields.

For example, say you had a C structure and function that was defined as follows:
{pygmentize:: c}
struct COORD {
  int x;
  int y;
};
void display_coord(struct COORD* position);
{pygmentize}

Then the the corresponding Java class for the structure would look like:

{pygmentize:: java}
@JniClass(flags={ClassFlag.STRUCT})
public static class COORD {
  public short x;
  public short y;
}
{pygmentize}

The native method definition can then just take COORD java object as an argument.
{pygmentize:: java}
public static native void display_coord(COORD position);
{pygmentize}

Nested native structures are also easy.  For example:
{pygmentize:: c}
struct RECT {
  struct COORD top_left;
  struct COORD bottom_right;
};
{pygmentize}

Would be mapped as:
{pygmentize:: java}
@JniClass(flags={ClassFlag.STRUCT})
public static class RECT {
  public COORD top_left = new COORD();
  public COORD bottom_right = new COORD();
}
{pygmentize}

## Loading Platform Constants {#platform-constants}

Many times you need to access the value of platform constants.  To load
platform constants you will need to:

1.  Define a constant initializer method which when called will sets all the
    static fields annotated as constant fields with the constant value.  Example:
    {pygmentize:: java}
      @JniMethod(flags={CONSTANT_INITIALIZER})
      private static final native void init();
    {pygmentize}

2. For each constant you want to load define a static field for it with the 
   `@JniField(flags={CONSTANT})` annotation.  Example:
    {pygmentize:: java}
      @JniField(flags={CONSTANT})
      public static short FOREGROUND_BLUE;
    {pygmentize}

3. Call the constant initializer method in the class initializer, after the 
   library is loaded.  For example:
    {pygmentize:: java}
      private static final Library LIBRARY = new Library("simple", Simple.class);    
      static {
        LIBRARY.load();
        init();
      }
    {pygmentize}


## Accessors: Changing the Function or Field Accessed {#accessors}

If you want to call your java method something different from the native method
name, you can set the `accessor` attribute on a `@JniMethod` or `@JniField` annotation.

For example, to call the native `printf` function from a Java `print` method,
you would set it up as follows:
{pygmentize:: java}
  @JniMethod(accessor="printf")
  public static native int print(String message);
{pygmentize}

In the case of field on a structure this can be used to do simple renames or
to map field to the field of a nested structure or union:
{pygmentize:: java}

  // Just a simple rename..
  @JniField(accessor="dwSize")
  public int size;

  // Mapping to a nested field.
  @JniField(accessor="u.pos")
  public int deep;
  
{pygmentize}

The accessor also comes in handy when you want to load constants which 
are not a simple symbol, for example the size of a structure:
{pygmentize:: java}
  @JniField(accessor="sizeof(struct COORD)", flags={CONSTANT})
  public static int SIZEOF_COORD;
{pygmentize}

## Working with Pointers {#pointers}

If you want the same java class to be able to work with 32 bit and 64 bit
pointers, you should map pointers to Java `long`. If your only targeting 32 bit
platforms you can map pointers to `int`.

When HawtJNI is on a 32 bit platform but is mapping pointers to Java's 64 bit
longs, it needs to know that the value it's working with is a pointer so that it
properly up and down casts the pointer value.

This is typically done by setting the `cast` attribute on the the `@JniMethod`,
`@JniArg` and `@JniField` annotations to the pointer type. In general it's good
practice to set the cast attribute to the defined type of value.

For example, to let HawtJNI know that the `malloc` function returns a void
pointer you would define it as follows:
{pygmentize:: java}
  @JniMethod(cast="void *")
  public static final native long malloc(
    @JniArg(cast="size_t") long size);
{pygmentize}

If the cast end with `*` and it's being mapped to a Java `long` then HawtJNI knows that it's dealing with a pointer type.  But if your using a typedef to pointer, then you need to flag the condition with one of the following:

* `@JniMethod(flag={POINTER_RETURN})`
* `@JniArg(flags={POINTER_ARG})`
* `@JniField(flags={POINTER_FIELD})`

This is very common on the Windows platform where tend to typedef pointer types like `LPTCSTR`. 

## Allocating Arrays and Structures on the Native Heap {#heap-structures}

The memory associated with a passed structure or array is reclaimed at the end of every native method call.  Therefore, a method expects a passed array or structure reference to remain valid after the method call then that array or structure needs to be allocated on the native heap instead.

This is accomplished using the standard native `malloc` function call.
For example to create a char array 80 bytes big:

{pygmentize:: java}
  long buffer = malloc(80);
{pygmentize}

To create a structure on the the heap, your going to need a couple of helper methods.

1.  You need to know the size of the structure.
2.  You need define a versions of memmove which copy to and from the your Java structure object and memory buffer (`void *`).

I recommend keeping those defined in the structure class itself.  For example:

{pygmentize:: java}
@JniClass(flags={ClassFlag.STRUCT})
public static class COORD {

  public short x;
  public short y;

  // To hand loading the SIZE_OF constant
  @JniMethod(flags={CONSTANT_INITIALIZER})
  private static final native void init();
  @JniField(flags={CONSTANT}, accessor="sizeof(struct COORD)")
  public static short SIZE_OF;
  static {
    init();
  }

  public static final native void memmove (
    @JniArg(cast="void *", flags={NO_IN, CRITICAL})) COORD dest, 
    @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) long src, 
    @JniArg(cast="size_t") long size);

  public static final native void memmove (
    @JniArg(cast="void *", flags={NO_IN, CRITICAL})) long dest, 
    @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) COORD src, 
    @JniArg(cast="size_t") long size);
}
{pygmentize}

Now you can allocate and initialize a structure on the heap:
{pygmentize:: java}
  long buffer = malloc(COORD.SIZE_OF);
  COORD tmp = new COORD()
  tmp.x = 3;
  tmp.y = 4;
  COORD.memmove(buffer, tmp, COORD.SIZE_OF);
{pygmentize}

<!-- TODO: talk about pointer arithmetic to index into an array of structs -->

## Callbacks: Calling Java methods from native functions.

Some native functions require a callback function pointer as an argument.  You can
create one using a `Callback` object.  For example, lets say needed to call the following
native function:

{pygmentize:: java}
long foo(void (*fp)(char *buffer, int n));
{pygmentize}

It you could map it in Java as:
{pygmentize:: java}
  public static final native void foo (
    @JniArg(cast="void *") long fp);
{pygmentize}

Next you need to create a java method that can accept the callback.  The method MUST
return long and can have any number of arguments, but they must also all be longs.
The method can be a static or instance method.

For example:
{pygmentize:: java}
class MyObject {
  public long mymethod(long buffer, long n) {
    System.out.println("Was given a buffer "+n+" byte big at "+buffer);
  }
}
{pygmentize}

Then to create a function pointer which points back to a Java method, you create
a Callback object with a reference to the Java object, method name, and number of arguments
on the method takes.

For example:
{pygmentize:: java}
  MyObject object = new MyObject();
  Callback callback = new Callback(object, "mymethod", 2);
  long fp = callback.getAddress();
  foo(fp);
{pygmentize}
  
Warning: you can only create up to 128 Callbacks concurrently.  If you exceed this number
`callback.getAddress()` returns zero.  You should use the `callback.dispose()` method
to release a callback once it's not being used anymore.

## Optimizations {#optimizations}

If you have performance sensitive method that works on an Array or Structure,
setting on of the following flags may help your performance.

* __`@JniArg(flags={NO_IN})`__:Indicate that a native method parameter is an out
  only variable. This only makes sense if the parameter is a structure or an
  array of primitives. It is an optimization to avoid copying the java memory to
  C memory on the way in.

* __`@JniArg(flags={NO_OUT})`__: Indicate that a native method parameter is an in
  only variable. This only makes sense if the parameter is a structure or an
  array of primitives. It is an optimization to avoid copying the C memory from
  java memory on the way out.

* __`@JniArg(flags={CRITICAL})`__: Uses a special JVM call which locks the java
  array in memory and disable garbage collection for the duration of the native
  call. This is an optimization to avoid copying memory and must only be used
  with low latency functions. This only makes sense if the parameter is an array

These optimization flags are typically used for the `memmove` C library function.
for example, to copy the the contents of a `byte[]` into a java `int[]`, you would
define the `memmove` method as follows:

{pygmentize:: java}
  public static final native void memmove (
    @JniArg(cast="void *", flags={NO_IN, CRITICAL})) int[] dest, 
    @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) byte[] src, 
    @JniArg(cast="size_t") long size);
{pygmentize}


## Conditionals {#conditionals}

You can use the `conditional` attribute on the `@JniClass`, `@JniMethod` and `@JniField` annotations to control if JNI code associated
with the class, method, or field gets conditionally compiled out.

This is very useful if your mapping to a native function, structure, or field
that may not be available on all the platforms which your Java class is going to
be made available to.

Example:
{pygmentize:: java}
  @JniMethod(conditional="defined(_WIN32)")
  public static native int printf(String message);
{pygmentize}

Would produce a JNI `printf` method implementation which is surrounded by the
following pre-processor directives:

{pygmentize:: c}
  #if defined(_WIN32)
  // ... the JNI method implementation would be here
  #endif
{pygmentize}

The `conditional` on a `@JniMethod` or `@JniField` defaults to value configured on
the enclosing `@JniClass`. So if most of the fields or methods in a class need to
have have the same conditional applied, just set it on the `@JniClass` annotation.

## How to Add HawtJNI to an Existing Maven Build {#adding-to-maven-build}

The HawtJNI provides a maven plugin which makes it easy code generate and build
the native library for your current platform.  

Once you have a working maven build for a java module, you can then update it to
use HawtJNI to generate your JNI libraries. With the following steps:

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

### Generation Details

You may have noticed that HawtJNI is generating a slew of code in different
directories. Here is a breakdown of what gets generated where and during which
maven build phase:

1. __process-classes__: Processes the annotated java classes:

    1. generates JNI .c and .h files to
    `target/generated-sources/hawtjni/native-src`

    2. generates an autoconf and msbuild build project in the
    `target/generated-sources/hawtjni/native-package` directory

2. __generate-test-resources__: Compiles the native library:

    1. The project is built in `target/native-build`

    2. The project installed the libraries to `target/native-dist` 

    3. The libraries are copied to `target/generated-sources/hawtjni/lib` which gets
    added as a test resource path.

3. __package-jar__: The contents of `target/generated-sources/hawtjni/lib` get
jarred and attached to the maven build with a platform specific classifier.

4. __package-source__: The contents of
`target/generated-sources/hawtjni/native-package` get zipped up into a platform
agnostic source package for building the native library.

<!-- TODO:
### Using Autoconf to Detect Platform Features

### custom header files ... linking
-->

