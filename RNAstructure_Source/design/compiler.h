# Many different C/C++ compilers can be used, defined as the following:
# Option 1: g++ (GNU C++ compiler)
# Option 2: CC (SGI and Sun compiler)
# Option 3: icc (Intel C compiler)
# Option 4: xlC (xlc compiler, e.g. on AIX)
CXX = g++
#CXX = CC
#CXX = icc
#CXX = xlC

# The Java compiler, defined below, must be active for the Java interface of
# RNAstucture to be built successfully from its Makefile. For building
# RNAstructure text interfaces only, these flags don't need to be defined.
JXX = javac 
JXXFLAGS = -source 1.6

# For compilation of the C++ component of RNAstructure, the exact flags depend
# on the operating system. There are flags for compiling into object files and
# for linking into a shared library, defined below for the three major systems.

# Linux
CXXFLAGS = -O3 -fsched-spec-load -fPIC -D NDEBUG
LIBFLAGS = -shared
LIBEXT = .so
OPSYSTEM = Linux

# Mac
#CXXFLAGS = -O3 -fsched-spec-load -fPIC -arch x86_64
#LIBFLAGS = -dynamiclib -arch x86_64 -framework JavaVM
#LIBEXT = .dylib
#OPSYSTEM = Mac

# Cygwin (running on Windows)
#CXXFLAGS = -O3 -fsched-spec-load -mno-cygwin
#LIBFLAGS = -Wl,--add-stdcall-alias -mno-cygwin
#LIBEXT = .dll
#OPSYSTEM = Windows

# The INCLUDEPATH flags are required to find proper linking headers for the
# RNAstructure Java interface. On all systems, these paths are the required
# native headers needed to interface Java with C++ code for the Java interface.
# These flags must be changed manually to coincide with the current machine
# before the Java interface of RNAstructure can run successfully.
#
# The INCLUDEPATH flags are used in the java_interface Makefile.
#
# INCLUDEPATH1 must hold the directory which contains "jni.h".
# INCLUDEPATH2 must hold the directory which contains "jni_md.h".
# Absolute paths must be used for both.
#
# For Fedora Linux, these two paths are normally similar to:
#     INCLUDEPATH1 = /usr/java/jdk1.x.x_x/include
#     INCLUDEPATH2 = /usr/java/jdk1.x.x_x/include/linux
# For Ubuntu Linux, these two paths are normally similar to:
#     INCLUDEPATH1 = /usr/lib/jvm/java-6-sun-1.x.x.x/include
#     INCLUDEPATH2 = /usr/lib/jvm/java-6-sun-1.x.x.x/include/linux
# For Mac, these two paths are usually the same, and similar to:
#     INCLUDEPATH1 = /System/Library/Frameworks/JavaVM.framework/Headers
#     For Mac, INCLUDEPATH2 should be defined as "", since it's not used.
# For Windows (Cygwin build), these two paths are normally similar to:
#     INCLUDEPATH1 = "C:\Program Files\Java\jdk1.x.x_x\include"
#     INCLUDEPATH2 = "C:\Program Files\Java\jdk1.x.x_x\include\win32"
#     For Windows, quotes may be needed if paths contain spaces.
#
# Note that the example paths given above are only meant to be a general guide
# to the paths' forms on different systems; the actual paths on any given
# system may differ from these.

INCLUDEPATH1 = "/usr/java/jdk1.6.0_13/include"
INCLUDEPATH2 = "/usr/java/jdk1.6.0_13/include/linux"

###############################################################################
## The rest of this file is taken up by common compilation commands. Nothing
## below this point needs to be, or should be, changed.
## It may, however, be useful to look at the commands below for reference.
###############################################################################

# Define suffixes for compilation.
.SUFFIXES:      .cpp
.cpp.o:
	${CXX} ${CXXFLAGS} -c -o $@ $<

# Define linking rule. By default, the ARCHITECTURE variable is empty, so it is
# not defined in this file. Changes to the architecture, where applicable. can
# be found in file library_defines.h. (library_defines.h should always be
# included alongside compiler.h.)
LINK = ${CXX} ${ARCHITECTURE} ${CXXFLAGS} -o $@
