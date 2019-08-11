#!/bin/bash

BASE=1000000000					# base for ID
RANGE=8999999999				# range space of ID
RECORD=1000000					# number of record to generate
AWKSCRIPT="gen_name.awk"		# awk script
LASTNAME="l2-names/lastnames.txt"
FIRSTNAME="l2-names/firstnames.txt"
OFILE="gradebook.csv"			# output file

awk -v record=$RECORD -v base=$BASE -v range=$RANGE \
	-v firstname=$FIRSTNAME -v lastname=$LASTNAME \
	-f $AWKSCRIPT > $OFILE

exit 0
