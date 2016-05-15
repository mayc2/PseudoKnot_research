#!/bin/bash

. ./set_env.sh

OUTPUTDIR=$RESEARCH/output_files
INPUTDIR=$RESEARCH/input_files

sampling_num=20

OPTIND=1
directory=0
file=0
echo $2
while getopts "df" opts; do
    case "${opts}" in
        d)
            directory=1
            ;;
        f) 
            file=1
            d=$2
            ;;
        *) 
            :
            ;;
    esac
done
shift $((OPTIND-1))


if [ $directory -eq 1 ]; then
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
fi 

if [ $file -eq 1 ]; then
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
fi