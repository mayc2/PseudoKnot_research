#!/bin/bash
usage() { 
    echo "Usage: $0 [-r] input_sequence_file" 1>&2
    echo "          *input_sequence_file(located in research parent directory) must be provided"
    echo "          *-r option re-runs sfold first"    
    exit 1; } 
rerun=0
OPTIND=1
while getopts "rh" opts; do
    case "${opts}" in
        h)
            usage
            ;;
        r) 
            rerun=1
            ;;
        *) 
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ $# -ne 1 ]; then
usage
fi

#initialize env variables
echo "setting environment up"
. ./set_env.sh
if [ $? -ne 0 ]
then
exit 1
fi

sequence=$RESEARCH/$1

#run sfold
if [ $rerun -eq 1 ]; then
echo "running sfold binary"
$RESEARCH/sfold-2.2/bin/sfold $sequence
if [ $? -ne 0 ]
then
exit 1
fi
fi

#run sampling algorithm on sfold output
echo "running sampling algorithm"
python3 ht_sampling.py output/sample_1000.out $sequence
if [ $? -ne 0 ]
then
exit 1
fi
