HawtJNI
==========

Description
-----------

[HawtJNI][1] is a code generator that produces the JNI code needed to implement java native methods.  It is based on the [jnigen][2] code generator that is part of the SWT Tools 
project which is used to generate all the JNI code which powers the eclipse platform.

[![Maven Central](https://img.shields.io/maven-central/v/org.fusesource.hawtjni/hawtjni-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.fusesource.hawtjni%22%20a%3A%22hawtjni-runtime%22)

Features
--------

* jni code generated from annotations on your java code
* maven integration

Synopsis
--------

There are many open source JNI code generators available, but if you're performance sensitive,
the code generator used by the eclipse SWT project is by far the best option.  The biggest 
problem is that it was not developed to be reused by other projects.  It was tightly coupled
to producing the SWT jni libraries and it could only be run within the eclipse platform.

HawtJNI takes that code generator and makes it more generally accessible to any project.

Example Usage
-------------

Your JNI methods must be defined as static native methods in a class annotated with `@JniClass`.  The following example will expose the C `open` function as a java 
method:

    @JniClass
    public class Platform {
        public static native long open (String file, int flags, int mode);
    }
    
You will also need to tell the JVM to load the native library when your class is loaded.  You can do this using the standard `System.loadLibrary` method:

    @JniClass
    public class Platform {
        static {
            System.loadLibrary("hawtjni-example");
        }
        public static native long open (String file, int flags, int mode);
    }
    
If you want to bundle the native library in as a resource of your jar, so that 
it can be automatically unpacked if it cannot be be found in your java 
library path.  Then a better option is to use the `Library` helper class 
that HawtJNI provides:

    @JniClass
    public class Platform {
        private static Library library = new Library("hawtjni-example", 1, 0, 0);
      	static {
      	    library.load();
      	}
        public static native long open (String file, int flags, int mode);
    }
    
To generate the JNI code, first compile your annotated class, then use the 
`hawtjni-generate.jar` runnable jar as follows:

    java -jar hawtjni-generate.jar -o target/native target/classes
    
The above example expects your compiled java classes to be in the `target/classes`
directory.  The generated JNI classes will be placed in the `target/native` directory.

More Docs:
-----------

[http://fusesource.github.io/hawtjni/documentation/developer-guide.html](http://fusesource.github.io/hawtjni/documentation/developer-guide.html)

[1]: http://fusesource.github.io/hawtjni "HawtJNI"
[2]: http://www.eclipse.org/swt/jnigen.php
