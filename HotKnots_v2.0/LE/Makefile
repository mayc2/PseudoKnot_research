# makefile for the energy model library

CC = g++
#CFLAGS = -g -DDOUBLEPARAMS -Wno-deprecated
CFLAGS = -g -Wno-deprecated
LIB = -lm -L/usr/lib -L../LE -lsimfold
#LIB = -lm -L/usr/lib -L../LE
INC = -I./H -I../simfold/include -I../simfold/src/common -I../simfold/src/simfold 

#SRC_C = ../simfold/src/common
#SRC_S = ../simfold/src/simfold

P_SRCS = Loop.cpp Stack.cpp Input.cpp Bands.cpp LoopList.cpp commonPK.cpp paramsPK.cpp initPK.cpp
#SC_SRCS = $(SRC_C)/params.cpp $(SRC_C)/common.cpp $(SRC_C)/init.cpp $(SRC_C)/timer.cpp
#SS_SRCS = $(SRC_S)/s_specific_functions.cpp $(SRC_S)/s_min_folding.cpp $(SRC_S)/s_hairpin_loop.cpp $(SRC_S)/s_stacked_pair.cpp $(SRC_S)/s_energy_matrix.cpp $(SRC_S)/s_internal_loop.cpp $(SRC_S)/s_multi_loop.cpp $(SRC_S)/s_multi_loop_sub.cpp $(SRC_S)/s_sub_folding.cpp $(SRC_S)/s_partition_function.cpp $(SRC_S)/s_partition_function_complex.cpp

P_OBJS = $(P_SRCS:.cpp=.o)
#SC_OBJS = $(SC_SRCS:.cpp=.o)
#SS_OBJS = $(SS_SRCS:.cpp=.o)

#LIBOBJ = Loop.o Stack.o Input.o Bands.o LoopList.o commonPK.o paramsPK.o initPK.o

default: libLEModel.a

#$(PROG): $(P_OBJS) $(SC_OBJS) $(SS_OBJS)
#	$(CC) -o $@ $(P_OBJS) $(SC_OBJS) $(SS_OBJS) $(LIB)

#libLEModel.a: $(P_OBJS) libsimfold.a
libLEModel.a: $(P_OBJS)
	ar -rv libLEModel.a $(P_OBJS)
	ranlib libLEModel.a

.SUFFIXES: .cpp
.cpp.o:
	$(CC) -c $(INC) $*.cpp $(CFLAGS) -o $*.o

#libsimfold.a: $(SC_OBJS) $(SS_OBJS)
#	ar -rv libsimfold.a $(SC_OBJS) $(SS_OBJS)
#	ranlib libsimfold.a

clean:
#	rm -f $(P_OBJS) $(SC_OBJS) $(SS_OBJS) libLEModel.a libsimfold.a *~ core
	rm -f $(P_OBJS) libLEModel.a *~ core


depend:
	makedepend -- $(CFLAGS) -- $(P_SRCS)
#	makedepend -- $(CFLAGS) -- $(P_SRCS) $(SC_SRCS) $(SS_SRCS)

# end of makefile
# DO NOT DELETE THIS LINE -- make depend depends on it.
