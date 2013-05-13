#![HawtJNI](http://hawtjni.fusesource.org/images/project-logo.png)

## [HawtJNI 1.8](http://hawtjni.fusesource.org/blog/releases/release-1-7.html), released 2013-05-13

* Improved shared lib extraction logic.

## [HawtJNI 1.7](http://hawtjni.fusesource.org/blog/releases/release-1-7.html), released 2013-03-20

* Support explicitly configuring which build tool to use on windows.
* Fix for automake 1.11

## [HawtJNI 1.6](http://hawtjni.fusesource.org/blog/releases/release-1-6.html), released 2012-08-09

* Updating hawtjni generate projects so that they work on OS X Lion.
* Fixes issue #2 : Support passing the JNIEnv pointer to native methods.

## [HawtJNI 1.5](http://hawtjni.fusesource.org/blog/releases/release-1-5.html), released 2011-09-21

* Only include config.h if it's available.

## [HawtJNI 1.4](http://hawtjni.fusesource.org/blog/releases/release-1-4.html), released 2011-08-18

* Add more options to the maven hawtjni plugin so that you can build jars containing
  native libs in a different module from the one which generates the native package for 
  the jar.

## [HawtJNI 1.3](http://hawtjni.fusesource.org/blog/releases/release-1-3.html), released 2011-08-08

* Add hawtjni_attach_thread and hawtjni_dettach_thread helper methods
* Fully support binding against C++ source code / classes.
* Support using private fields in struct bound classes.
* Avoid "jump to label from here crosses initialization" compiler error message.
* Provide better error messages when a user does not properly setup a C++ method binding.
* Support mapping a class to a differently named structure name.
* Support picking the OS X SDK version via a configure option.
* Added pointer math support class to be able to do pointer math in java land without going into a JNI layer.

## [HawtJNI 1.2](http://hawtjni.fusesource.org/blog/releases/release-1-2.html), released 2011-06-11

* Adding bit model to the name of the extracted library to support hosts running both 32 and 64 bits JVM.
* Converted website to a scalate based static website

## [HawtJNI 1.1](http://hawtjni.fusesource.org/blog/releases/release-1-1.html), released 2010-11-04
----
* Generate a .vcxproj for for compatibility with the new Windows 7.1 SDK
* Fixed callback failures on 32 bit platforms

## [HawtJNI 1.0](http://hawtjni.fusesource.org/blog/releases/2010/04/release-1-0.html), released 2010-02-24

* Initial release
