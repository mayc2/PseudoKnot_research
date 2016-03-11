SHELL=/bin/bash
CONFIG_FILE=config.make
-include $(CONFIG_FILE)
###################### Common Settings #######################################
# These variables are common among several operating systems and compilers, but
# they can also be modified in the OS-specific section later on.
#
# CXX - The c++ compiler. Examples: g++ (GNU C++ compiler), icc (Intel C compiler),
#     CC (SGI and Sun compiler), xlC (xlc compiler, e.g. on AIX)
CXX = g++

# SHARED_CXXFLAGS - c++ compilation flags for c++ compilation to object files.
#     This variable should be used for flags that are shared across all
#     platforms. 
#     Use 'CXXFLAGS' to define OS-specific flags (which should include SHARED_CXXFLAGS)
#     GCC Options can be found here:  https://gcc.gnu.org/onlinedocs/gcc-4.9.2/gcc/Invoking-GCC.html#Invoking-GCC
#     The various components of SHARED_CXXFLAGS (e.g. CXXDEFINES) are explained below.
  SHARED_CXXFLAGS = $(CXXDEFINES) -O3
# Example of CXXFLAGS used for Linux
  CXXFLAGS= $(SHARED_CXXFLAGS) -Wno-write-strings -fsched-spec-load -fPIC

# CXXDEFINES = List miscellaneous C++ defines here. (This variable may be temporarily modified for some targets and is included in the CXXFLAGS variable.)
  CXXDEFINES = -D NDEBUG 

# COMPILE_CPP  - The default recipe to compile a .cpp source file into 
#                an .o object file. (Do not include $< so that this be the base for other recipes.
  COMPILE_CPP = ${CXX} ${CXXFLAGS} -c -o $@

# LINK       - recipe for linking object files into an executable.
  LINK = ${CXX} ${ARCHITECTURE} ${CXXFLAGS} $(if $(LINK_STATIC),${STATIC_FLAGS}) -o $@

# LINK_STATIC - Set to blank to do dynamic linking. Set to 1 (or any non-blank value) for static linking.
  LINK_STATIC=
# Flags for compiling with static libraries. (Will only be used if LINK_STATIC is non-empty).
  STATIC_FLAGS=-static-libgcc -static-libstdc++ -Wl,-Bstatic

# LIBFLAGS   - compiler flags when producing a shared library (.dll, .so, etc)
  LIBFLAGS =-shared 

# CUDA_COMPILER - NVIDIA CUDA compiler, for GPU calculations
  CUDA_COMPILER = nvcc

# DEBUG_FLAGS - flag(s) to add debugging symbols. By default these are not used unless
#   make is invoked with the flag debug=yes ('true', '1', and 'on' are also acceptable).
  DEBUG_FLAGS=-g
  ifeq ($(and $(filter yes on true 1,$(debug)), $(origin debug)),command line)
    $(info Building with debugging symbols.)
    SHARED_CXXFLAGS+=$(DEBUG_FLAGS)
  endif

############## Operating-System and Processor Architecture Detection #########
# This section attempts to auto-detect the OS. This is used only for convenience
# and allows the same Makefile to be used on multiple operating systems
# without modification.
# You can circumvent auto-detection by setting these environment variables:
#   RNA_MAKE_OS   -- the operating system name (e.g. Linux, Windows, Mac)
#   RNA_MAKE_ARCH -- the target architecture (e.g. x86 or x86_64)
# (These can be set as environment variables or on the MAKE command-line. 
#    e.g. `make all RNA_MAKE_OS=Linux`)
    ifneq (${RNA_MAKE_OS},) 
      #if RNA_MAKE_OS is NOT blank, use it as the OS
      OPSYSTEM=$(RNA_MAKE_OS)
    else ifeq (${OPSYSTEM},)
      # IF both RNA_MAKE_OS and OPSYSTEM are blank, use the `uname` command 
      #   (if available) to determine the OS.
      #   Replace 'UNKNOWN' with default OS if desired. 
      OPSYSTEM_RAW:=$(shell uname -s 2>/dev/null || echo UNKNOWN) 
      # Perform some replacements to normalize the output of uname on various systems.
      # OS_REPLACEMENTS= CYGWIN%=Windows MSYS%=Windows Darwin=Mac GNU%=Linux
      OPSYSTEM := $(OPSYSTEM_RAW:CYGWIN%=Windows)
      OPSYSTEM := $(OPSYSTEM:MSYS%=Windows)
      OPSYSTEM := $(OPSYSTEM:Darwin=Mac)
      OPSYSTEM := $(OPSYSTEM:GNU%=Linux)
      $(if $(DEBUG), $(info Make: Operating System: $(OPSYSTEM)))
      export OPSYSTEM #make it available for recursive calls to make, so auto-detection is performed only once.
    endif
#
# The target architecture (i.e. x86 or x86_64) is probably that of the current OS,
# however the RNA_MAKE_ARCH variable can be used to cross-compile for a different 
# architecture if supported by the compiler etc.
    ifneq (${RNA_MAKE_ARCH},) #processor arch. 
      TARGET_ARCH=$(RNA_MAKE_ARCH)
    else ifeq (${TARGET_ARCH},) 
      # Determine processor architecture of the the OS.
      # If desired, you may replace the term 'UNKNOWN' with the default 
      # architecture (i.e. x86 or x86_64)
      TARGET_ARCH := $(shell uname -m 2>/dev/null || echo UNKNOWN)   
      $(if $(DEBUG), $(info Make: Architecture: $(TARGET_ARCH)))
      export TARGET_ARCH #make it available for recursive calls to make, so auto-detection is performed only onces
    endif
# Determine the number of bits (32 or 64) of the target architecture. This is used to name some files (see library_defines.h for details.)
  ARCH_BITS = $(if $(findstring 64,$(TARGET_ARCH)),64,32)


##############################################################################
############## Operating-System Dependent Configuration ######################
##############################################################################
#
    ifeq (${OPSYSTEM},Linux)
      ############ LINUX ##########################################################
      MULTIFINDFLAG = -DMULTIFIND #### this flag does not seem to be used anywhere
      CXXFLAGS= $(SHARED_CXXFLAGS) -Wno-write-strings -fPIC
    else ifeq (${OPSYSTEM},Mac)
      ############ MAC ############################################################
      CXXFLAGS= $(SHARED_CXXFLAGS) -Wno-write-strings -fPIC -arch $(TARGET_ARCH)  -Wno-unused-value -Wno-c++11-compat-deprecated-writable-strings -Wno-logical-op-parentheses
      LIBFLAGS = -dynamiclib -arch $(TARGET_ARCH)
      LINK_STATIC=## Do not use static linking on mac. Apple strongly discourages it.
    else ifeq (${OPSYSTEM},Windows)
      ############ WINDOWS ########################################################
      #  The following settings are for compilation on Windows using the MinGW-w64 
      #  compiler from within the Cygwin POSIX environment. Other choices of shell
      #  or compiler may work as well. 
      #  Compiler executable names may be dependent on the flavor of MinGW 
      #  you have installed. The names below correspond to the "MinGW-w64" build 
      #  obtained directly from the Cygwin repository. Note that 32-bit and 64-bit 
      #  targeted versions of the compiler can be installed side by side.
      CXX=$(if $(findstring 64,${TARGET_ARCH}),x86_64-w64-mingw32-g++.exe,i686-w64-mingw32-g++.exe)
      CXXFLAGS= $(SHARED_CXXFLAGS) -Wno-write-strings -fsched-spec-load -D__USE_MINGW_ANSI_STDIO=1 -pthread ## -fPIC is not supported on windows
      #On Windows, statically link libgcc, libstdc++, winpthreads, libgomp (OpenMP). 
      LINK_STATIC=1
      STATIC_FLAGS= -static-libgcc -static-libstdc++ -Wl,-Bstatic -lstdc++ -lpthread -lgomp 
      # on Windows, there are many variants of the root drive, depending on the OS/shell (e.g. cygwin, MSys2)
      # SYSROOT will be something like  "/c" or "/cygdrive/c"  
      SYSROOT:=$(shell cd '$(SYSTEMROOT)/..' && pwd)
      # SYSROOT_W will be something like  "c:/" (it must be used for windows programs that do not understand /c or /cygdrive/c )
      SYSROOT_W:=$(subst \,/,$(dir $(SYSTEMROOT)))
      # INCLUDE_GAMMA : When compiling on windows with certain 
      #   compilers (Intel, VS etc), we need to include a file to redefine the 
      #   'tgamma' function that is missing from cmath. 
      #   The variable INCLUDE_GAMMA is used to indicate this.
      INCLUDE_GAMMA = $(if $(findstring g++,$(CXX)),,${ROOTPATH}/src/gamma.o)

      # DYNALIGN_BIG_STACK
      # Compiling with MinGW-w64 on Windows seems to cause dynalign to crash (stack overflow)
      # unless the stack size is increased (with a linking option)
      DYNALIGN_BIG_STACK= -Wl,--stack,8388608
    else ifeq (,${OPSYSTEM})
      ############ NO OS ########################################################
	  $(error No Operating system defined!!!)
    else
	  ############ UNKNOWN OS ###################################################
      $(error Unknown Operating system defined: $(OPSYSTEM))
    endif

##############################################################################
# The rest of this file is taken up by common compilation and linking commands.
# Nothing below this point needs to be changed, unless certain 
#  standard compiler flags need to be modified for a given compiler (e.g. -c or -o )
# It may also be useful to look at the commands below for reference.
#
# Note that for some of these commands to work, the ROOTPATH variable, which
# should hold the path to the RNAstructure root directory, must be defined.
#
# As a general rule of thumb, ROOTPATH should always be defined in a Makefile
# before compiler.h is included. ROOTPATH may be defined as an absolute path
# or relative to the current Makefile. All the Makefiles contained within
# RNAstructure follow this rule, and define ROOTPATH with a relative path.
##############################################################################

######################### GPU/CUDA ############################
# Determine if compilation should be done for GPU
ifneq (${CUDA},)
  CXX=${CUDA_COMPILER}
  CXXFLAGS := -O3 -D NDEBUG -D _CUDA_CALC_ -D FLOAT -D SHORT $(patsubst %,-Xcompiler %,$(CXXFLAGS))
  $(info CUDA environment variable detected: compiling for GPU)
endif

# For compilation of programs using CUDA, the cuda flags need to be set
    CUDAFLAGS = -DFLOAT -DSHORT -O3 -use_fast_math

########################### SMP/OpenMP ##########################################
# For compilation of SMP programs using OpenMP, the OpenMP flags need to be set. 
    CXXOPENMPFLAGS = -fopenmp -D SMP ## g++ compiler
    #CXXOPENMPFLAGS = -openmp -D SMP ## intel compiler

########################## INCLUDESVM #########################################
#  If making Multifind, INCLUDESVM must indicate the location of svm.o,
#  from the libsvm package. 
    INCLUDESVM = /usr/local/libsvm-3.17

######################## Default Compilation Rule ############################
# Define generic rules for compiling c++ object files (.o) from source files (cpp)
# These rules will be overridden if a more specific rule is defined 
# (see dependencies.h)
# TODO: make this more strict by prepending $(ROOTPATH) (but this means that other Makefils have to specify the full sub-path to each .o instead of a relative path from the current directory. e.g. see PARTS/Makefile )
# Add a rule for cpp files with corresponding header files:
%.o: %.cpp %.h
	$(MAKE_OUTDIR)
	${COMPILE_CPP} $<

# If no header file is found, this second rule will apply:
%.o: %.cpp
	$(MAKE_OUTDIR)
	${COMPILE_CPP} $<

##############################################################################
######## Program-Specific Compilation/Linking Rules ##########################
##############################################################################
  # Define a Dynalign II compiling rule.
  COMPILE_DYNALIGN_II = ${COMPILE_CPP} -DDYNALIGN_II

  # Define a Dynalign II SMP compiling rule.
  COMPILE_DYNALIGN_II_SMP = ${COMPILE_CPP} -DDYNALIGN_II -DCOMPILE_SMP

  # Define an MULTIFIND compiling rule.
  COMPILE_MULTIFIND = ${COMPILE_CPP} -DMULTIFIND

  # Define an SMP compiling rule.
  COMPILE_SMP = ${COMPILE_CPP}  -DCOMPILE_SMP

  # Define a CUDA compiling rule.
  COMPILE_CUDA = nvcc ${CUDAFLAGS} -c -o $@

  # Define an SVM compiling rule.
  COMPILE_SVM = ${COMPILE_CPP} -I${INCLUDESVM}

  # Define an SVM/SMP compiling rule.
  COMPILE_SVM_SMP = ${COMPILE_CPP} -I${INCLUDESVM} -DCOMPILE_SMP

  # Define an INSTRUMENTED compiling rule.
  COMPILE_INSTRUMENTED = ${COMPILE_CPP} -DINSTRUMENTED

  # Define the SMP linking rule, for use with OpenMP programs.
  LINK_SMP = ${LINK} ${CXXOPENMPFLAGS}

  # Define the CUDA linking rule, for use with GPU programs.
  LINK_CUDA = nvcc ${CUDAFLAGS} -o $@

##############################################################################
########## Debugging and Utility Macros/Variables Defined Below ##############
##############################################################################
# Some useful definitions for special characters
#newline (linefeed) character
define newline


endef

comma:= ,
empty:=
space:= $(empty) $(empty)
tab:= $(empty)	$(empty)
dollar:=$$
hash:=\#
bkslash:=\\
t:=$(tab)
n:=$(newline)
_:=$(empty)
s:=$(space)
k:=$(bkslash)
h:=$(hash)

# MKDIR: A function to make a directory.
#      Usage: $(call MKDIR,PATH_TO_DIR)
MKDIR = @[[ -e $(1) ]] || mkdir -p $(1) &> /dev/null

# MAKE_OUTDIR: A macro to use in recipes. 
#    It simply makes the parent directory of the target file.
#    Usage: $(MAKE_OUTDIR)
MAKE_OUTDIR = $(call MKDIR,$(@D))

############# Debugging ######################################################
# Show information about the operating system and configured variables if
# debug output has been enabled.
#############################################################################
fShowVar=$(1)$(2)Source:$(3)$(origin $(1))$(2)Define:$(3)$(value $(1))$(2)Value :$(3)$($(1))
#fShowVarList=$(foreach var,$(1),HZ@)#$(call fShowVar,$(var),M,X)
#fShowVarList=$(foreach var,$(1),$(call fShowVar,$(var),$(newline)      ,$(tab)))
fShowVarList=$(foreach var,$(1),$n  $(call fShowVar,$(var),$n    ,$s))
fSpacesToLines=$(subst $s,$n,$(1))
#fShowVarListOld=$(foreach var,$(1),\#    $(call fShowVar,$(var),$(newline)\#       ,$(tab))$(newline))\#

CPP_MAKE_VARS=COMPILE_CPP CXX CXXDEFINES CXXFLAGS LIBFLAGS LINK LINK_STATIC STATIC_FLAGS OPSYSTEM TARGET_ARCH 
APP_MAKE_VARS=COMPILE_CUDA COMPILE_DYNALIGN_II COMPILE_DYNALIGN_II_SMP COMPILE_INSTRUMENTED  COMPILE_MULTIFIND COMPILE_SMP COMPILE_SVM COMPILE_SVM_SMP CUDA_COMPILER CUDAFLAGS CXXOPENMPFLAGS INCLUDE_GAMMA INCLUDESVM LINK_CUDA LINK_SMP MULTIFINDFLAG 
JAVA_MAKE_VARS=COMPILE_JNI JNI_CXXFLAGS JNI_DIR JNI_INCLUDE_FLAGS JNI_INCLUDE JXX JXXFLAGS LINK_JNI_LIB

showvars:
	$(info \
$n---------------------------- Makefile Configuration ----------------------------\
$n  Building on OS: $(OPSYSTEM) (uname: $(OPSYSTEM_RAW))\
$n  Target Architecture: $(TARGET_ARCH) ($(ARCH_BITS)-bits)\
$n$n-----------C++ Configuration------------$(call fShowVarList,$(CPP_MAKE_VARS))\
$n$n-----------Java Configuration-----------$(call fShowVarList,$(JAVA_MAKE_VARS))\
$n$n-----------App-specific Configuration-----------$(call fShowVarList,$(APP_MAKE_VARS))\
$n$n       (For a list of ALL variables, use the target 'showallvars')\
$n--------------------------------------------------------------------------------)
showallvars:
	$s  $(foreach var,$(sort $(.VARIABLES)),$(info   $(call fShowVar,$(var),$n   ,$s)$n))

############# Configuration ##################################################
# Run some tests and save some settings.
config.make:

export CP_UPD?=cp # cp command

autoconfig:
	@echo 'Makefile Auto-Config'
	@echo '## This is an auto-generated file. It may not be wise to edit it.' > $(CONFIG_FILE)
	@echo 'RNA_MAKE_OS=$(RNA_MAKE_OS)' >> $(CONFIG_FILE)
	@echo 'RNA_MAKE_ARCH=$(RNA_MAKE_ARCH)' >> $(CONFIG_FILE)
	@# Test for cp -u 
	@$(shell touch '#1.tmp')
	@CP_UPD=cp$(shell cp -u '#1.tmp' '#2.tmp' &> /dev/null && echo ' -u')
	@echo 'CP_UPD=$($CP_UPD)' >> $(CONFIG_FILE)
	@$(shell rm -f '#?.tmp') 
	@echo 'Auto-Config Complete. Values written to $(abspath $(CONFIG_FILE)).'

#Clear the default goal if it was set to one of the targets in this file. (It should be set in the Makefile itself.)
ifeq ($(.DEFAULT_GOAL),showvars)
   .DEFAULT_GOAL=
endif

##############################################################################
# Disable built-in rules. All the rules we need are specified explicitly.
MAKEFLAGS += --no-builtin-rules --no-builtin-variables
.SUFFIXES:
####### Define default message/action for files with no matching rule. #######
.DEFAULT:
	$(error Unknown target "$@".  For a list of goals, type "make help".)
##############################################################################