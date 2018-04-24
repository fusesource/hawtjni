Building on Unix/Linux/OS X
---------------------------

The configure script will customize the way the software is built and
installed into your system along with detecting the available libraries
that have been installed.  To use the default configuration just run:

    ./configure

For more help on how to customize the build configuration, run:

    ./configure --help

Once the configure script has run successfully, you are ready to build.
Run:

    make

This will build all of the core ActiveMQ CPP source code.  To build and
install the code into the system directories, run:

    make install

You will have to become the superuser in order to be able to install the
JNI libraries.


### OS X Lion requirements

Install brew.  It will depends on XCode being installed along with 
the command line tools.  Then use brew to install autoconf, automake,
and libtool.

    brew install autoconf automake libtool

Building on Windows
-------------------

Download and install the free [Microsoft Windows SDK][1].  The SDK includes
all the headers, libraries, and build tools needed to compile the JNI library.

Set the `JAVA_HOME` environment variable to the location where your JDK is 
installed.  

Use the installed command window and change to the directory that this file is located in and then run: 

For recent SDK Versions:

		msbuild vs2010.vcxproj (or )

For legacy SDK Versions:

		vcbuild vs2008.vcproj

The dll files will be located under the target directory.
    
[1]: https://developer.microsoft.com/en-us/windows/downloads