#!/bin/bash

RESEARCH=`pwd`
cmd='export RESEARCH'
cmd2='export NUPACKHOME='$RESEARCH'/nupack3.0.5'
cmd3='export HOTKNOTS='$RESEARCH'/HotKnots_v2.0'

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
$cmd3
if [ $? -ne 0 ]
then
exit 1
fi

