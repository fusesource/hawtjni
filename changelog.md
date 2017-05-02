#![HawtJNI](http://fusesource.github.io/hawtjni/images/project-logo.png)

## [HawtJNI 1.15](http://fusesource.github.io/hawtjni/blog/releases/release-1-15.html), released 2017-05-xx

906cedb Default to extract in the users' home folder in case the temp directory is not writable
ed95784 search in library.$name.path like in META-INF/native resources
477c8cc Fix some other problems with platform, especially on windows when compiling for the non native platform
58834e8 Upgrade some plugins
992ee3f Fix bad naming for the extracted file when the version contains a dot
6b58328 Do not include the extension in the windows project name, #23
9165154 Merge pull request #30 from felixvf/fix_lib64_bug
1cb6770 Merge pull request #34 from hboutemy/master
4c430c6 Merge pull request #20 from felixvf/fix_bug_18
f99972b Better exception reporting when unable to load a library, fixes #27
1c5b81f Allow the windows project name to be specified, fixes #23
ef3437c Allow the -Dplatform=xxx setting to be used when doing the actual native build
0072848 Remove explicit array creation when using var args
c6fb914 Remove unused imports
145f3ee Fix typos in method names
81a35e1 prepare gh-pages publication with scm-publish plugin
b3982d5 Use latest version of maven javadoc plugin
cb2ad85 Merge branch 'hboutemy-hawtjni-31'
cd20329 #31 fixed API doc generation and misc other Maven-related conf
784a50f Fix libdir to "/lib". Prevent any variation such as "/lib64".
401ce1c Update readme.md
a73fc16 Merge pull request #11 from OhmData/travis
098c501 Simplify the fallback case a bit
40f9f23 Merge pull request #22 from slaunay/use-java7-chmod-with-unix-chmod-fallback

## [HawtJNI 1.14](http://fusesource.github.io/hawtjni/blog/releases/release-1.14.html), released 2016-06-20

e2522b0 Merge pull request #26 from michael-o/freebsd
6dc93fe Improve FreeBSD support

## [HawtJNI 1.12](http://fusesource.github.io/hawtjni/blog/releases/release-1.12.html), released 2016-04-26

70f24ba Don't build the website by default.
ef93152 Better JDK detection on OS X.
61ac652 Use Files.setPosixFilePermissions for chmod
57e5b32 Define JNI64 not only in case of \__x86_64__ but in general for any _LP64 platform.

## [HawtJNI 1.11](http://fusesource.github.io/hawtjni/blog/releases/release-1.11.html), released 2015-04-21

e1da91a Update xbean version used.
354e277 Disable deployment of website since web host is not there anymore.
08cfdd0 Update parent pom.
86e97d1 Merge pull request #19 from jerrydlamme/master
1e2ee63 Added architecture specific native library loading path
d10c4b0 Merge pull request #16 from NJAldwin/use-absolute-path
3d3aa0b Ensure absolute path is used for library
8c28532 Merge pull request #13 from batterseapower/master
c10adf5 Version bumps and markup fixes necessary for building on JDK8
aed6cbd Build a stock travis

## [HawtJNI 1.10](http://fusesource.github.io/hawtjni/blog/releases/release-1.10.html), released 2014-02-12

efa684c Ignore IDEA project files.
18cb7e5 prepare for next development iteration
f3bd38e Upgrade parent pom version.
175faf0 Merge pull request #8 from normanmaurer/netty_needs
b3f8609 Allow to also use generate mojo with existing native src files
c27b5a0 Avoid warning.
c1980ef Add support for building against the Oracle JDK on OS X.

## [HawtJNI 1.9](http://fusesource.github.io/hawtjni/blog/releases/release-1-9.html), released 2013-09-09

* Fix issue #7. We now do a write barrier before setting the 'cached' field to 1 so that reader don't see this get re-ordered before all the fields are readable.
* Improve the auto generated build systems for windows/OS X

## [HawtJNI 1.8](http://fusesource.github.io/hawtjni/blog/releases/release-1-8.html), released 2013-05-13

* Improved shared lib extraction logic.

## [HawtJNI 1.7](http://fusesource.github.io/hawtjni/blog/releases/release-1-7.html), released 2013-03-20

* Support explicitly configuring which build tool to use on windows.
* Fix for automake 1.11

## [HawtJNI 1.6](http://fusesource.github.io/hawtjni/blog/releases/release-1-6.html), released 2012-08-09

* Updating hawtjni generate projects so that they work on OS X Lion.
* Fixes issue #2 : Support passing the JNIEnv pointer to native methods.

## [HawtJNI 1.5](http://fusesource.github.io/hawtjni/blog/releases/release-1-5.html), released 2011-09-21

* Only include config.h if it's available.

## [HawtJNI 1.4](http://fusesource.github.io/hawtjni/blog/releases/release-1-4.html), released 2011-08-18

* Add more options to the maven hawtjni plugin so that you can build jars containing
  native libs in a different module from the one which generates the native package for 
  the jar.

## [HawtJNI 1.3](http://fusesource.github.io/hawtjni/blog/releases/release-1-3.html), released 2011-08-08

* Add hawtjni_attach_thread and hawtjni_dettach_thread helper methods
* Fully support binding against C++ source code / classes.
* Support using private fields in struct bound classes.
* Avoid "jump to label from here crosses initialization" compiler error message.
* Provide better error messages when a user does not properly setup a C++ method binding.
* Support mapping a class to a differently named structure name.
* Support picking the OS X SDK version via a configure option.
* Added pointer math support class to be able to do pointer math in java land without going into a JNI layer.

## [HawtJNI 1.2](http://fusesource.github.io/hawtjni/blog/releases/release-1-2.html), released 2011-06-11

* Adding bit model to the name of the extracted library to support hosts running both 32 and 64 bits JVM.
* Converted website to a scalate based static website

## [HawtJNI 1.1](http://fusesource.github.io/hawtjni/blog/releases/release-1-1.html), released 2010-11-04
----
* Generate a .vcxproj for for compatibility with the new Windows 7.1 SDK
* Fixed callback failures on 32 bit platforms

## [HawtJNI 1.0](http://fusesource.github.io/hawtjni/blog/releases/2010/04/release-1-0.html), released 2010-02-24

* Initial release
