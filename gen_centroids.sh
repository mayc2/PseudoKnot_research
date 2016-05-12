#!/bin/bash

. ./set_env.sh

OUTPUTDIR=$RESEARCH/centroid_structures

usage() { 
    echo "Usage: $0 [-df]" 1>&2
    echo "          -f *input_sequence_file(located in research parent directory) must be provided"
    echo "          *-d directory of input sequences"    
    exit 1; } 


directory=0
file=0
while getopts "df" opts; do
	case "${opts}" in
	   d) 
		INPUTDIR=$2
		directory=1
		;;
	   f)
		INPUTFILE=$2
		file=1
		;;
	   *)
		usage
		;;
	esac
done
shift $((OPTIND-1))

if [ $file -eq 1 ]; then
    name=${INPUTFILE%.*}
    name=${name##*/}
    OUTFILE=$OUTPUTDIR/$name

    $RESEARCH/sfold-2.2/bin/sfold $INPUTFILE
    i=0
    for cluster in $RESEARCH/output/clusters/c*.ccentroid.bp; do
	if [ $i -eq 0 ]; then
	   echo Cluster $[$i+1] > $OUTFILE
           python3 Centroid.py $cluster $INPUTFILE >> $OUTFILE
	else
	   echo Cluster $[$i+1] >> $OUTFILE
	   python3 Centroid.py $cluster $INPUTFILE >> $OUTFILE
	fi
	i=$[$i+1]
    done
fi

if [ $directory -eq 1 ]; then
for INPUTFILE in $INPUTDIR/*.fa; do
    temp=0
    name=${INPUTFILE%.*}
    name=${name##*/}
    OUTFILE=$OUTPUTDIR/$name    
    
    $RESEARCH/sfold-2.2/bin/sfold $INPUTFILE
    i=0
    for cluster in $RESEARCH/output/clusters/c*.ccentroid.bp; do
        if [ $i -eq 0 ]; then
           echo Cluster $[$i+1] > $OUTFILE
           python3 Centroid.py $cluster $INPUTFILE >> $OUTFILE
        else
           echo Cluster $[$i+1] >> $OUTFILE
           python3 Centroid.py $cluster $INPUTFILE >> $OUTFILE
        fi
        i=$[$i+1]
    done
done
fi
