#!/bin/bash

. ./set_env.sh

OUTPUTDIR=$RESEARCH/output_files
INPUTDIR=$RESEARCH/input_files

sampling_num=20

for d in $INPUTDIR/*.fa; do
    temp=0
    name=${d%.*}
    name=${name##*/}
    FILENAME=$OUTPUTDIR/$name    
    while [ $temp -lt $sampling_num ]; do
        if [ $temp -eq 0 ]; then
            echo Iteration $temp : $name 
            echo Iteration $temp > $FILENAME
            echo '' >> $FILENAME
            `bash $RESEARCH/run_ht.sh -r $d >> $FILENAME`
        else
            echo Iteration $temp : $name
            echo '' >> $FILENAME
            echo '' >> $FILENAME
            echo Iteration $temp >> $FILENAME
            echo '' >> $FILENAME
            echo '' >> $FILENAME
            `bash $RESEARCH/run_ht.sh $d >> $FILENAME`
        fi
        temp=$[$temp+1]
    done
done