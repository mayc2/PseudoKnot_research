#!/bin/bash

cmd='export NUPACKHOME=/home/chris/devtree/pseudoKnot_research/nupack3.0.5'
cmd2='export RESEARCH=/home/chris/devtree/pseudoKnot_research'

$cmd
if [ $? -ne 0 ]
then
exit 1
fi
$cmd2
if [ $? -ne 0 ]
then
exit 1
fi

