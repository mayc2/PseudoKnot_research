#!/bin/bash

input_file=$1
output_file=$2

if [ -f $inputfile ]
then
  echo The input file is: $input_file
else
  echo $input_file does not exist
  exit 1
fi

echo The output file is: $output_file

cwd=`pwd`
RESEARCH_DIR=/home/chris/devtree/research
RNAStructure_DIR=/home/chris/devtree/research/RNAstructure_Source
EXE_DIR=$RNAStructure_DIR/exe
DATAPATH=$RNAStructure_DIR/data_tables
export DATAPATH

cd $RNAStructure_DIR
make stochastic
if [ $? -ne 0 ]
  then
  exit 1
fi

$EXE_DIR/stochastic --sequence $cwd/$input_file $cwd/$output_file
if [ $? -ne 0 ]
  then
  exit 1
fi

exit 0
