dnl ---------------------------------------------------------------------------
dnl  Copyright (C) 2009-2011 FuseSource Corp.
dnl  http://fusesource.com
dnl  
dnl  Licensed under the Apache License, Version 2.0 (the "License");
dnl  you may not use this file except in compliance with the License.
dnl  You may obtain a copy of the License at
dnl  
dnl     http://www.apache.org/licenses/LICENSE-2.0
dnl  
dnl  Unless required by applicable law or agreed to in writing, software
dnl  distributed under the License is distributed on an "AS IS" BASIS,
dnl  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
dnl  See the License for the specific language governing permissions and
dnl  limitations under the License.
dnl ---------------------------------------------------------------------------
dnl ---------------------------------------------------------------------------
dnl SYNOPSIS:
dnl
dnl   WITH_OSX_UNIVERSAL()
dnl
dnl   Allows creating universal binaries on the 
dnl
dnl   Adds the --with-universal=ARCH option.  This will will 
dnl   set sysroot option to /Developer/SDKs/MacOSX${OSX_VERSION}.sdk.
dnl   if OSX_VERSION is not defined, it will set it to 10.5 (the first
dnl   release where intel universal binaries were supported)
dnl
dnl   You must use the no-dependencies option when automake is initialized.  
dnl   for example: AM_INIT_AUTOMAKE([no-dependencies]) 
dnl
dnl      This macro calls:
dnl        AC_SUBST(CFLAGS)
dnl        AC_SUBST(CXXFLAGS)
dnl        AC_SUBST(LDFLAGS)
dnl        AC_SUBST(OSX_VERSION)
dnl
dnl AUTHOR: <a href="http://hiramchirino.com">Hiram Chrino</a>
dnl ---------------------------------------------------------------------------

AC_DEFUN([WITH_OSX_UNIVERSAL],
[
  AC_PREREQ([2.61])
  case "$host_os" in
  darwin*)
    
    AC_MSG_CHECKING(OS X SDK version)
    AC_ARG_WITH([osxsdk],
    [AS_HELP_STRING([--with-osxsdk@<:@=VERSION@:>@],
      [OS X SDK version to build against. Example: --with-osxsdk=10.6])],
    [ 
      OSX_UNIVERSAL="$withval"
    ],[
      OSX_VERSION=""
      for v in 10.0 10.1 10.2 10.3 10.4 10.5 10.6 10.7 10.8 10.9 10.10 ; do
        if test -z "${OSX_VERSION}" && test -d "/Developer/SDKs/MacOSX${v}.sdk" ; then 
          OSX_VERSION="${v}"
        fi
      done
    ])
    AC_MSG_RESULT([$OSX_VERSION])
    AC_SUBST(OSX_VERSION)
        
    AC_MSG_CHECKING(whether to build universal binaries)
    AC_ARG_WITH([universal],
    [AS_HELP_STRING([--with-universal@<:@=ARCH@:>@],
      [Build a universal binary.  Set to a space separated architecture list. Pick from: i386, x86_64, ppc, and/or ppc64. @<:@default="i386 x86_64"@:>@])],
    [ 
      AS_IF(test "$withval" = "no", [
        OSX_UNIVERSAL=""
        AC_MSG_RESULT([no])
      ], test "$withval" = "yes", [
        OSX_UNIVERSAL="i386 x86_64"
        AC_MSG_RESULT([yes, archs: $OSX_UNIVERSAL])
      ],[
        OSX_UNIVERSAL="$withval"
        AC_MSG_RESULT([yes, archs: $OSX_UNIVERSAL])
      ])
    ],[
      OSX_UNIVERSAL=""
      AC_MSG_RESULT([no])
    ])
    
    AS_IF(test -n "$OSX_UNIVERSAL", [
      for i in $OSX_UNIVERSAL ; do
        CFLAGS="-arch $i $CFLAGS"
        CXXFLAGS="-arch $i $CXXFLAGS"
        LDFLAGS="-arch $i $LDFLAGS"
      done 
      
      CFLAGS="-isysroot /Developer/SDKs/MacOSX${OSX_VERSION}.sdk $CFLAGS"
      CXXFLAGS="-isysroot /Developer/SDKs/MacOSX${OSX_VERSION}.sdk $CXXFLAGS"
      LDFLAGS="-syslibroot,/Developer/SDKs/MacOSX${OSX_VERSION}.sdk $LDFLAGS"
      AC_SUBST(CFLAGS)
      AC_SUBST(CXXFLAGS)
      AC_SUBST(LDFLAGS)
    ])
    ;;
  esac
])


