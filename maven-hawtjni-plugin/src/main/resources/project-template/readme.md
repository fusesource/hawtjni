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

Building on Windows
-------------------

Download and install the free 'Visual C++ 2008 Express Edition' IDE from:

* [http://www.microsoft.com/express/download/](http://www.microsoft.com/express/download/)

Set the `JAVA_HOME` environment variable to the location where your JDK is 
installed.  

You can then use the 'Visual C++ 2008 Express Edition' IDE to open the 
`vs2008.vcproj` file.  Pressing F7 in the IDE will build the JNI DLL. 
Alternatively, if you want to build it from the command line, you
can run:

    vcexpress vs2008.vcproj /build release