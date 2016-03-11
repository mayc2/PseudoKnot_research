##########################################################################################
## Describe which targets the Makefile can use.
##########################################################################################
help:
	$(info \
		Type "make <goal> [<goals...>]" to perform one of the actions listed below.$n \
		$n  swig      -- Generate SWIG glue code for JNI. \
		$n  native    -- Compile the native class library. \
		$n  javaGui   -- Compile the Java user interface only (RNAstructure.jar)  \
		$n  tester    -- Compile GUI testing platform (GUITester.jar).  \
		$n  gui (GUI) -- Compile the Java user interface (RNAstructure.jar) and the native (c++) class library.  \
		$n               (but NOT the SWIG code or GUI testing platform). \
		$n  all       -- Perform all actions listed above.  \
		$n  help      -- Show this list of goals.  \
		$n \
	)

instructions: help
##########################################################################################
## Define the relative path to the RNAstructure root directory.
## Include all macro, dependency, and variable definitions.
##########################################################################################
ROOTPATH=..
JAVA=yes
include ${ROOTPATH}/compiler.h
include ${ROOTPATH}/library_defines.h
include ${ROOTPATH}/dependencies.h
include compiler-java.h

# ifneq ($(OPSYSTEM),Mac)
#   # When compiling the RNAstructure_GUI library, use OpenMP (SMP) and pThreads, but only if using Linux or Windows (Not Mac)
#   JAVA_LIBRARY_FILES=$(JAVA_LIBRARY_FILES_SMP)
# endif

#Names of the different java modules.  MOD_UTIL is a utility library used by the others.
JAVA_MODULES=UTIL GUI TESTER
MOD_UTIL = Utilities
MOD_GUI = RNAstructureUI
MOD_TESTER = GUITester

#root java package name for all modules.
BASE_PACKAGE=ur_rna

#Will be expanded during building to match one of the module names above.
# (MOD_NAME will be set to UTIL, GUI, or TESTER)
JAVA_MOD=$(MOD_$(MOD_NAME))

#The full package for each module    e.g. ur_rna.RNAstructureUI
#  The JAVA_MOD variable will change depending on the build target,
#  (as will the value of the MODULE_PACKAGE variable).
MODULE_PACKAGE=$(BASE_PACKAGE).$(JAVA_MOD)

#The fullly qualified output path for classes in each module. 
# e.g.  ur_rna/RNAstructureUI
# The JAVA_MOD variable will change depending on the build target, 
# as will the value of this variable.

PACKAGE_SUBDIR=$(subst .,/,$(MODULE_PACKAGE))

#The root directory for all java-related stuff, including SWIG etc.
JAVA_ROOT_DIR  = ${ROOTPATH}/java_interface
#The root directory for source code in each module. (The JAVA_MOD variable will change depending on the build target)
JAVA_SRC_ROOT		=$(JAVA_ROOT_DIR)/src
JAVA_PKG_SRC_DIR	=$(JAVA_SRC_ROOT)/$(PACKAGE_SUBDIR)## e.g. ../java_interface/src/ur_rna/RNAstructureUI

#BIN_DIR is defined in compiler.make  (by default it is set to ${ROOTPATH}/exe/$(OPSYSTEM)_$(TARGET_ARCH)
BIN_OUTPUT_DIR      =${ROOTPATH}/exe
JAVA_OUT_ROOT		=${BIN_OUTPUT_DIR}/java
#output folder for compiled *.class files.
JAVA_PKG_OUT_DIR	=$(JAVA_OUT_ROOT)/$(PACKAGE_SUBDIR)

JAVA_RES_SRC_DIR=$(JAVA_PKG_SRC_DIR)/resources
JAVA_RES_OUT_DIR=$(JAVA_PKG_OUT_DIR)/resources
SWIG_SRC = $(JAVA_ROOT_DIR)/SWIG

#ALL_OUTPUT_DIRS=$(BIN_OUTPUT_DIR) $(JAVA_PKG_OUT_DIR) $(JAVA_OUT_MOD) $(JAVA_RES_OUT) $(JAVA_RES_OUT)/images $(JAVA_RES_OUT)/sounds $(SWIG_OUT) $(SWIG_OUT)/drawing

## Use the Java TProgressDialog and ProgressMonitor if libraries are to be integrated with Java.
OBSERVINGBAR =
PROGRESSMONITOR = $(SWIG_OUT)/ProgressMonitor.o
TPROGRESSDIR = $(SWIG_SRC)

##########################################################################################
JAVA_LIB_DIR=$(JAVA_ROOT_DIR)/lib
#specify the required dependency libraries for java compilation
JAVA_CLASSPATH:=$(sort $(wildcard $(JAVA_LIB_DIR)/abbot.jar $(JAVA_LIB_DIR)/gnu-regexp-*.jar $(JAVA_LIB_DIR)/jdom-*.jar))
ifeq ($(OPSYSTEM),Windows)
  PATH_SEP=;
else
  PATH_SEP=:
endif
JAVA_CLASSPATH:=$(subst $(space),$(PATH_SEP),$(JAVA_CLASSPATH))#replace : with ; on Windows.
##########################################################################################
## List files and targets
##########################################################################################
#List all directories that have java source files.
#FYI, javac will compile dependent files as needed, so not all source files will be compiled individually.
#Note: JAVA_PKG_SRC_DIR variable depends on the JAVA_MOD variable, which changes for each target.
MOD_GUI_DIRS= \
	menus \
	windows \
	drawing/dialogs \
	drawing/menus \
	drawing/proxy \
	drawing/runners \
	utilities
#	core \
#	core/menus \
#	core/windows \
	
MOD_UTIL_DIRS= \
	annotation

MOD_TESTER_DIRS= \
	GuiTools \
	ScriptParser

JAVA_DIR_LIST=$(MOD_$(MOD_NAME)_DIRS)## e.g. $(MOD_UTIL_DIRS)
# use pattern substitution and wildcard expansion to get all java source files as well as desired output classes
###  Expand *.java in each folder listed above, rooted in the java source folder.
JAVA_SRC_FILES=$(wildcard $(JAVA_PKG_SRC_DIR)/*.java $(patsubst %,$(JAVA_PKG_SRC_DIR)/%/*.java,$(JAVA_DIR_LIST)))
###  Convert each source file path into a class file, rooted in the output directory.
TARGET_CLASS_FILES=$(patsubst $(JAVA_SRC_ROOT)/%.java,$(JAVA_OUT_ROOT)/%.class,$(JAVA_SRC_FILES))
###  Expand *.class in each folder listed above, rooted in the output directory. This will contain all the files listed in TARGET_CLASS_FILES as well as some other misc. ones produced by the java compiler
#ACTUAL_CLASS_FILES=$(subst $$,\$$,$(wildcard $(patsubst %,$(JAVA_OUT_DIR)/%/*.class,$(JAVA_DIR_LIST))))

#Evaluate target classes
MOD_NAME=GUI
GUI_CLASSES:=$(TARGET_CLASS_FILES)
MOD_NAME=UTIL
UTIL_CLASSES:=$(TARGET_CLASS_FILES)
MOD_NAME=TESTER
TESTER_CLASSES:=$(TARGET_CLASS_FILES)

##########################################################################################
## Define PHONY targetrs (i.e. Top-level goals).
##########################################################################################
#make sure to run these targets, even if a file of the same name already exists.
.PHONY: instructions help all swig native java gui GUI clean realclean debug tester

# Rule to build the C++ part of the RNAstructure Java interface only.
native: $(BIN_OUTPUT_DIR)/${RNASTRUCTURE_LIBRARY}
# Rule to build the Java interface, C++ and Java code.
GUI gui: javaGui native
tester: javaTester
all: swig gui tester
##########################################################################################
## Compilation Rules
##########################################################################################

#generic rule for compiling class files from java source files
$(JAVA_OUT_ROOT)/%.class: $(JAVA_SRC_ROOT)/%.java
	$(MAKE_OUTDIR) # make the output directory for the class file
#	$(call MKDIR, $(JAVA_OUT_ROOT)) #also make this java output folder, because javac will NOT do it and will abort compilation.
#	Note: the sourcepath flag is usually not required if the classpath (-cp) includes the source directory. However if it is NOT specified, and javac finds source files (*.java) inside a jar file in the CP, then it may try to recompile them, leading to compilation errors related to third-party pre-compiled code
	"${JXX}" ${JXXFLAGS} -cp "$(JAVA_SRC_ROOT)$(PATH_SEP)$(JAVA_CLASSPATH)" -d "$(JAVA_OUT_ROOT)" -sourcepath $(JAVA_SRC_ROOT)  $<
#:${JAVA_PKG_SRC_DIR}:$(JAVA_OUT_TARGET)

#rule to compile the native library (RNAstructure.dll, libRNAstructure.so, etc)
$(BIN_OUTPUT_DIR)/${RNASTRUCTURE_LIBRARY}: $(JAVA_LIBRARY_FILES)
	$(MAKE_OUTDIR)
	$(info LINK_JNI_LIB ${LINK_JNI_LIB} )
	${LINK_JNI_LIB} ${JAVA_LIBRARY_FILES}

# General rule to compile all SWIG-generated c++ files. (COMPILE_CPP is defined in compiler.h)
$(SWIG_OUT)/%.o: $(SWIG_SRC)/%.cpp $(SWIG_SRC)/%.h
	$(MAKE_OUTDIR)
	${COMPILE_JNI} $<

# General rule to compile all SWIG-generated cxx files. (COMPILE_JNI is defined in compiler.h)
$(SWIG_OUT)/%.o: $(SWIG_SRC)/%.cxx
	$(MAKE_OUTDIR)
	${COMPILE_JNI} $<

# Rule to build the Java part of the RNAstructure Java interface only.
# Build the jar file. prerequisites are all java class files. Specify these in reverse order of dependency (main depends on drawing and util and drawing depends on util)
javaGUI javaGui: $(BIN_OUTPUT_DIR)/RNAstructure.jar
$(BIN_OUTPUT_DIR)/RNAstructure.jar: $(UTIL_CLASSES) $(GUI_CLASSES)
# 	The MOD_NAME variable affects the expansion of JAVA_RES_SRC_DIR among others.
	$(eval MOD_NAME=GUI)
	$(MAKE_OUTDIR) #create the output directory
#   CP_UPD is cp -u (on platforms that support it)
	$(CP_UPD) -r $(JAVA_RES_SRC_DIR)/ $(JAVA_RES_OUT_DIR)/  
#   Delete Source Control Folders
	find $(JAVA_OUT_ROOT) -name 'CVS' -type d -exec rm -r {} +
	$(JAR) cvmf $(JAVA_ROOT_DIR)/Manifest.txt $@ \
		-C $(JAVA_OUT_ROOT)  $(BASE_PACKAGE)/$(MOD_UTIL) \
		-C $(JAVA_OUT_ROOT)  $(BASE_PACKAGE)/$(MOD_GUI) \
		 > $@.log

javaTester: javaGui $(BIN_OUTPUT_DIR)/GUITester.jar
$(BIN_OUTPUT_DIR)/GUITester.jar: $(UTIL_CLASSES) $(GUI_CLASSES) $(TESTER_CLASSES)
# 	The MOD_NAME variable affects the expansion of JAVA_RES_SRC_DIR among others.
	$(eval MOD_NAME=TESTER)
	$(MAKE_OUTDIR) #create the output directory
#   CP_UPD is cp -u (on platforms that support it)
	[[ ! -d $(JAVA_RES_SRC_DIR)/ ]] || $(CP_UPD) -r $(JAVA_RES_SRC_DIR)/ $(JAVA_RES_OUT_DIR)/
	find $(JAVA_OUT_ROOT) -name 'CVS' -type d -exec rm -r {} +
	$(JAR) cvmf $(JAVA_ROOT_DIR)/GUITester_Manifest.txt $@ \
		-C $(JAVA_OUT_ROOT)  $(BASE_PACKAGE)/$(MOD_UTIL) \
		-C $(JAVA_OUT_ROOT)  $(BASE_PACKAGE)/$(MOD_TESTER) \
		 > $@.log

# $(BIN_OUTPUT_DIR)/%.jar:
# 	$(MAKE_OUTDIR)
# 	-[ -e "$(JAVA_RES)/" ] && cp -u -r $(JAVA_RES)/ $(JAVA_RES_OUT)/
# 	$(JAR) cvf $@ \
# 		$(JAVA_OUT_TARGET)
# 		> $@.log
#		$(ALL_CLASS_FILES)

#Specify that the JAR file should be deleted if there is an error making it. (Otherwise the jar is created but may not have all required files inside.)
.DELETE_ON_ERROR: $(BIN_OUTPUT_DIR)/RNAstructure.jar $(BIN_OUTPUT_DIR)/Utilities.jar $(BIN_OUTPUT_DIR)/GUITester.jar

#ALL_POSSIBLE_OUTPUT_DIRS = $(sort $(foreach MOD_NAME,$(JAVA_MODULES, $(ALL_OUTPUT_DIRS))))
#$(call expandWith,UTIL,ALL_OUTPUT_DIRS) $(ALL_OUTPUT_DIRS) $(ALL_OUTPUT_DIRS):

# Rule to build SWIG files.
# This rule is almost never necessary for the end user.
swig: 
	$(eval MOD_NAME=GUI)#affects the expansion of  MODULE_PACKAGE, JAVA_PKG_SRC_DIR, among others
	swig -java -c++ -package $(MODULE_PACKAGE) -outdir $(JAVA_PKG_SRC_DIR) ${SWIG_SRC}/RNAstructureBackendCalculator.i
	swig -java -c++ -package $(MODULE_PACKAGE).drawing.proxy -outdir $(JAVA_PKG_SRC_DIR)/drawing/proxy ${SWIG_SRC}/drawing/StructureBackend.i
	swig -java -c++ -package $(MODULE_PACKAGE).drawing.proxy -outdir $(JAVA_PKG_SRC_DIR)/drawing/proxy ${SWIG_SRC}/drawing/DotPlotBackend.i
	@rm -f $(JAVA_PKG_SRC_DIR)/*Proxy.java
	@rm -f $(JAVA_PKG_SRC_DIR)/drawing/proxy/*Proxy.java


DEBUG_VARS=JAVA_MOD MODULE_PACKAGE PACKAGE_SUBDIR BIN_OUTPUT_DIR JAVA_PKG_SRC_DIR JAVA_PKG_OUT_DIR
#SHOW debugging information
debug:
	$(foreach MOD_NAME,$(JAVA_MODULES), \
	  $(info ------- DEBUG INFO for Module: $(MOD_NAME) -------) \
	  $(foreach var,$(DEBUG_VARS),$(info $(var) = $($(var)))) \
	  $(info $n---$(MOD_NAME) JAVA_DIR_LIST---$n$(call fSpacesToLines,$(JAVA_DIR_LIST))) \
	  $(info $n---$(MOD_NAME) JAVA_SRC_FILES---$n$(call fSpacesToLines,$(JAVA_SRC_FILES))) \
	  $(info $n---$(MOD_NAME) TARGET_CLASS_FILES---$n$(call fSpacesToLines,$(TARGET_CLASS_FILES))) \
	  $(info ------- END of INFO for Module: $(MOD_NAME) -------$n) \
	)

##########################################################################################
## Cleanup.
## Object cleanup removes all temporary build objects.
## Executable cleanup removes all possible executables.
##########################################################################################

# Rule to clean all possible object files and compiled Java class files.
clean: clean-java clean-native

cleanJava cleanjava clean-Java clean-java:
	[[ ! -d $(JAVA_OUT_ROOT) ]] || find $(JAVA_OUT_ROOT) -name '*.class' -delete || echo 'No files to clean.'

cleannative cleanNative clean-Native clean-native:
	rm -f $(sort ${JAVA_LIBRARY_FILES} $(JAVA_LIBRARY_FILES_SMP))

# Rule to clean all possible objects and executables.
realclean: clean
	rm -f $(BIN_OUTPUT_DIR)/${RNASTRUCTURE_LIBRARY} $(BIN_OUTPUT_DIR)/RNAstructure.jar $(BIN_OUTPUT_DIR)/Utilities.jar $(BIN_OUTPUT_DIR)/GUITester.jar
	rm -rf $(JAVA_OUT_ROOT)
