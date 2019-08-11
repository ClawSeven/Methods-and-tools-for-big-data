#!/bin/bash

BASE=1000000000					# base for ID
RANGE=8999999999				# range space of ID
RECORD=$2						# number of record to generate
NUMFILE=$1						# number of files generated
AWKSCRIPT="gen_name.awk"		# awk script
LASTNAME="l2-names/lastnames.txt"
FIRSTNAME="l2-names/firstnames.txt"

if [ $# -eq 0 ]
then
	echo "not enough args"
	echo "./gen.sh [num_files] [num_entries]"
fi

# awk -v record=$RECORD -v base=$BASE -v range=$RANGE \
# 	-v firstname=$FIRSTNAME -v lastname=$LASTNAME \
# 	-f $AWKSCRIPT > $OFILE

mkdir gen_csv

for ((c=1; c<=$NUMFILE; c++))
do
	awk -v record=$RECORD -v base=$BASE -v range=$RANGE \
	-v firstname=$FIRSTNAME -v lastname=$LASTNAME \
	-f $AWKSCRIPT > "gen_csv/gradebook_$c.csv"
   # echo "file $c done"
done

exit 0
