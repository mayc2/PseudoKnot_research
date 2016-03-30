#!/bin/bash

. ./set_env.sh
if [ $? -ne 0 ]
then
exit 1
fi
python3 ht_sampling.py output/sample_1000.out sequence.fasta
if [ $? -ne 0 ]
then
exit 1
fi
