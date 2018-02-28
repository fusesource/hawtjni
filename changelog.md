# ![HawtJNI](http://fusesource.github.io/hawtjni/images/project-logo.png)

## [HawtJNI 1.15](http://fusesource.github.io/hawtjni/blog/releases/release-1-15.html), released 2017-05-04

* [`7537b9d`](https://github.com/fusesource/hawtjni/commit/7537b9d19be9806b210674ccad4b96d90a11d50b) Update changelog
* [`906cedb`](https://github.com/fusesource/hawtjni/commit/906cedb80b9661d0ea08f524fb464243610653a9) Default to extract in the users' home folder in case the temp directory is not writable
* [`ed95784`](https://github.com/fusesource/hawtjni/commit/ed95784f9a4d3ed1afb0a14bd3dccc815d3e3cbe) search in library.$name.path like in META-INF/native resources
* [`477c8cc`](https://github.com/fusesource/hawtjni/commit/477c8ccac78c3695ebcf6299d8b201adb3394d34) Fix some other problems with platform, especially on windows when compiling for the non native platform
* [`58834e8`](https://github.com/fusesource/hawtjni/commit/58834e835c6f196f6188c6f35aa9c349db610d84) Upgrade some plugins
* [`992ee3f`](https://github.com/fusesource/hawtjni/commit/992ee3fa28f30823913fe95a790fe3a08d19bdf3) Fix bad naming for the extracted file when the version contains a dot
* [`6b58328`](https://github.com/fusesource/hawtjni/commit/6b58328635bd181c18048387aa7d83fda51d5be8) Do not include the extension in the windows project name, [#23](http://github.com/fusesource/hawtjni/issues/23)
* [`9165154`](https://github.com/fusesource/hawtjni/commit/916515413152d2b25268d0f813c1f0f411388b3a) Merge pull request [#30](http://github.com/fusesource/hawtjni/issues/30) from felixvf/fix_lib64_bug
* [`1cb6770`](https://github.com/fusesource/hawtjni/commit/1cb6770dc7348958d96b38d8d0b1f4b065f43da5) Merge pull request [#34](http://github.com/fusesource/hawtjni/issues/34) from hboutemy/master
* [`4c430c6`](https://github.com/fusesource/hawtjni/commit/4c430c6d4454b37e035c1fb7ae284b8d3ac99c03) Merge pull request [#20](http://github.com/fusesource/hawtjni/issues/20) from felixvf/fix_bug_18
* [`f99972b`](https://github.com/fusesource/hawtjni/commit/f99972b7892fd621dca1442b8c8f3234edd4b02f) Better exception reporting when unable to load a library, fixes [#27](http://github.com/fusesource/hawtjni/issues/27)
* [`1c5b81f`](https://github.com/fusesource/hawtjni/commit/1c5b81fb386f74e47e776f3ba2775d15003f2ae9) Allow the windows project name to be specified, fixes [#23](http://github.com/fusesource/hawtjni/issues/23)
* [`ef3437c`](https://github.com/fusesource/hawtjni/commit/ef3437cde117c04793d773b25bd0627e5e260e66) Allow the -Dplatform=xxx setting to be used when doing the actual native build
* [`0072848`](https://github.com/fusesource/hawtjni/commit/0072848253e100c98745725bdf5224e63103fad7) Remove explicit array creation when using var args
* [`c6fb914`](https://github.com/fusesource/hawtjni/commit/c6fb9149b43292564bbc854d9942d4898a7f728d) Remove unused imports
* [`145f3ee`](https://github.com/fusesource/hawtjni/commit/145f3ee50204c8b8f8ae728cc91533dd19424d7d) Fix typos in method names
* [`81a35e1`](https://github.com/fusesource/hawtjni/commit/81a35e1a923bb1c7b0e6ffbdd66a08c83e119324) prepare gh-pages publication with scm-publish plugin
* [`b3982d5`](https://github.com/fusesource/hawtjni/commit/b3982d573b04878918aebe5435a5f64af6a4401f) Use latest version of maven javadoc plugin
* [`cb2ad85`](https://github.com/fusesource/hawtjni/commit/cb2ad85bc551e1628be25181acd6f9e97e04afab) Merge branch 'hboutemy-hawtjni-31'
* [`cd20329`](https://github.com/fusesource/hawtjni/commit/cd20329a801e5d904d7a43c46d3cb150b4767b66) [#31](http://github.com/fusesource/hawtjni/issues/31) fixed API doc generation and misc other Maven-related conf
* [`784a50f`](https://github.com/fusesource/hawtjni/commit/784a50f22d0abd1d4fa05f1fb720e70623092e63) Fix libdir to "/lib". Prevent any variation such as "/lib64".
* [`401ce1c`](https://github.com/fusesource/hawtjni/commit/401ce1cc6f053fccae386977b695ae7a5948ef4d) Update readme.md
* [`a73fc16`](https://github.com/fusesource/hawtjni/commit/a73fc165306a139e8cbb82f9dc28002c05d6d206) Merge pull request [#11](http://github.com/fusesource/hawtjni/issues/11) from OhmData/travis
* [`098c501`](https://github.com/fusesource/hawtjni/commit/098c501c90feb20749105840eaca1f51fbae2559) Simplify the fallback case a bit
* [`40f9f23`](https://github.com/fusesource/hawtjni/commit/40f9f23b4839941e217a8415eb9799aa539e0e36) Merge pull request [#22](http://github.com/fusesource/hawtjni/issues/22) from slaunay/use-java7-chmod-with-unix-chmod-fallback

## [HawtJNI 1.14](http://fusesource.github.io/hawtjni/blog/releases/release-1.14.html), released 2016-06-20

* [`e2522b0`](https://github.com/fusesource/hawtjni/commit/e2522b0ddd9f8975dc3a1cc99534ea458b807ddd) Merge pull request [#26](http://github.com/fusesource/hawtjni/issues/26) from michael-o/freebsd
* [`6dc93fe`](https://github.com/fusesource/hawtjni/commit/6dc93fe4c3b67e68d9805b6f0cc7f2b7c36d5b06) Improve FreeBSD support
* [`2d49307`](https://github.com/fusesource/hawtjni/commit/2d493076d264f6d8e2ac81ada4da4fcd78b2dabf) Deploy to sonatype.

## [HawtJNI 1.12](http://fusesource.github.io/hawtjni/blog/releases/release-1.12.html), released 2016-04-26

* [`70f24ba`](https://github.com/fusesource/hawtjni/commit/70f24ba7438a698d8e1e0de599b304774e01f5d4) Don't build the website by default.
* [`ef93152`](https://github.com/fusesource/hawtjni/commit/ef931527b4ca915a53c59eb6f6ef0222f8cf3c12) Better JDK detection on OS X.
* [`61ac652`](https://github.com/fusesource/hawtjni/commit/61ac6525a42117f0ea8820417d00616ef7f27452) Use Files.setPosixFilePermissions for chmod
* [`57e5b32`](https://github.com/fusesource/hawtjni/commit/57e5b3262a86ac0541585f3b3a40bf3b8933561b) Define JNI64 not only in case of \_\_x86\_64\_\_ but in general for any \_LP64 platform.

## [HawtJNI 1.11](http://fusesource.github.io/hawtjni/blog/releases/release-1.11.html), released 2015-04-21

* [`e1da91a`](https://github.com/fusesource/hawtjni/commit/e1da91aec68eda9f40350b062c4fed4e75fb4cb1) Update xbean version used.
* [`354e277`](https://github.com/fusesource/hawtjni/commit/354e2773cfb60008fd7500eef52ea7de8e9bb74a) Disable deployment of website since web host is not there anymore.
* [`08cfdd0`](https://github.com/fusesource/hawtjni/commit/08cfdd0995bb298d88e87d559d2ce39018e6b509) Update parent pom.
* [`86e97d1`](https://github.com/fusesource/hawtjni/commit/86e97d161d956009bbc92f2913dd570ece2ec3da) Merge pull request [#19](http://github.com/fusesource/hawtjni/issues/19) from jerrydlamme/master
* [`1e2ee63`](https://github.com/fusesource/hawtjni/commit/1e2ee6330f6832a374e29b78a1fff2df62d4a52c) Added architecture specific native library loading path
* [`d10c4b0`](https://github.com/fusesource/hawtjni/commit/d10c4b0914301810297f0f917ce3dba3e8868ff1) Merge pull request [#16](http://github.com/fusesource/hawtjni/issues/16) from NJAldwin/use-absolute-path
* [`3d3aa0b`](https://github.com/fusesource/hawtjni/commit/3d3aa0be17cc8d35e251ea3594b1e684ce919d0d) Ensure absolute path is used for library
* [`8c28532`](https://github.com/fusesource/hawtjni/commit/8c2853238e31b6e92f61fbdeda84314e5a529254) Merge pull request [#13](http://github.com/fusesource/hawtjni/issues/13) from batterseapower/master
* [`c10adf5`](https://github.com/fusesource/hawtjni/commit/c10adf5139969f1bfa6cb6e8dd6af204d64280a9) Version bumps and markup fixes necessary for building on JDK8
* [`aed6cbd`](https://github.com/fusesource/hawtjni/commit/aed6cbd06b4579170617dae7146ec9c61b70d82c) Build a stock travis
* [`efa684c`](https://github.com/fusesource/hawtjni/commit/efa684c0a87136f16b0bca67bc518ee9bf698f85) Ignore IDEA project files.
* [`18cb7e5`](https://github.com/fusesource/hawtjni/commit/18cb7e5d98e0edf687ba2d02c724c36d631e9f65) prepare for next development iteration
* [`f3bd38e`](https://github.com/fusesource/hawtjni/commit/f3bd38e1d83a5563c63b1bbebadf0c77c1fb54b8) Upgrade parent pom version.
* [`175faf0`](https://github.com/fusesource/hawtjni/commit/175faf07fbc2ec1c42582d0b935bb05fd46fc33f) Merge pull request [#8](http://github.com/fusesource/hawtjni/issues/8) from normanmaurer/netty\_needs
* [`b3f8609`](https://github.com/fusesource/hawtjni/commit/b3f8609c6682bda6d6c112c2e19c0c6cdc6dcfc6) Allow to also use generate mojo with existing native src files
* [`c27b5a0`](https://github.com/fusesource/hawtjni/commit/c27b5a0c4640bce9437488275b0d8c360c45c1e6) Avoid warning.
* [`c1980ef`](https://github.com/fusesource/hawtjni/commit/c1980ef32387547b0a5bba408abb00cbceaf6705) Add support for building against the Oracle JDK on OS X.

## [HawtJNI 1.10](http://fusesource.github.io/hawtjni/blog/releases/release-1.10.html), released 2014-02-12

* `efa684c` Ignore IDEA project files.
* `18cb7e5` prepare for next development iteration
* `f3bd38e` Upgrade parent pom version.
* `175faf0` Merge pull request [#8](http://github.com/fusesource/hawtjni/issues/8) from normanmaurer/netty\_needs
* `b3f8609` Allow to also use generate mojo with existing native src files
* `c27b5a0` Avoid warning.
* `c1980ef` Add support for building against the Oracle JDK on OS X.

## [HawtJNI 1.9](http://fusesource.github.io/hawtjni/blog/releases/release-1-9.html), released 2013-09-09

* [`1d27b2f`](https://github.com/fusesource/hawtjni/commit/1d27b2f1396920be7fce0be8b1995ac0459c69ef) Improve the generated build settings.
* [`d9cd0ab`](https://github.com/fusesource/hawtjni/commit/d9cd0ab660ac5acbdc5f84c806ba14b77e197385) Should fix issue [#7](http://github.com/fusesource/hawtjni/issues/7).  We now do a write barrier before setting the 'cached' field to 1 so that reader don't see this get re-ordered before all the fields are readable.

## [HawtJNI 1.8](http://fusesource.github.io/hawtjni/blog/releases/release-1-8.html), released 2013-05-13

* [`92c2661`](https://github.com/fusesource/hawtjni/commit/92c266170ce98edc200c656bd034a237098b8aa5) Simplify shared lib extraction.

## [HawtJNI 1.7](http://fusesource.github.io/hawtjni/blog/releases/release-1-7.html), released 2013-03-20

* [`3567b1d`](https://github.com/fusesource/hawtjni/commit/3567b1d89d458bddb651df252f3bb275c9076e1a) Support explicitly configuring which build tool to use on windows.
* [`d566bf7`](https://github.com/fusesource/hawtjni/commit/d566bf7de5d6a67fa7c7b3e04352ca2630fb55fe) Fix for automake 1.11

## [HawtJNI 1.6](http://fusesource.github.io/hawtjni/blog/releases/release-1-6.html), released 2012-08-09

* [`11df668`](https://github.com/fusesource/hawtjni/commit/11df668cb0d1269c0f98d9c09d80c56cf0770421) Updating hawtjni generate projects so that they work on OS X Lion.
* [`f0e3ace`](https://github.com/fusesource/hawtjni/commit/f0e3ace6422e5c5413445229ac79d27f68b1485b) Fixes [#2](http://github.com/fusesource/hawtjni/issues/2) : Support passing the JNIEnv pointer to native methods.

## [HawtJNI 1.5](http://fusesource.github.io/hawtjni/blog/releases/release-1-5.html), released 2011-09-21

* [`15d5b1a`](https://github.com/fusesource/hawtjni/commit/15d5b1a4c928fb8c39eee0705316478af30704b5) Only include config.h if it's available.

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
