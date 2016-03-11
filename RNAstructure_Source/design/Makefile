###############################################################################
## Included definitions of outside variables.
###############################################################################

ROOTPATH=..
include ${ROOTPATH}/compiler.h
include ${ROOTPATH}/library_defines.h
include ${ROOTPATH}/dependencies.h

###############################################################################
## General Build Actions
###############################################################################

# Build the design executable.
#Note: design.o is included in the RNA library.
design: design_interface.o ${CMD_LINE_PARSER} ${RNA_FILES}
	${LINK} design_interface.o ${CMD_LINE_PARSER} ${RNA_FILES}

###############################################################################
## Object and Executable Cleanup
###############################################################################

# Remove object files.
clean:
	rm -f design_interface.o ${CMD_LINE_PARSER} ${RNA_FILES}

# Remove object files and executables.
realclean: clean
	rm -f design
