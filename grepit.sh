#!/bin/bash

FILE=$1

if [[ ! -f $FILE ]]; then
  echo "Provide file as argument"
  exit 1
fi

CURRENT=current-grepit.txt
BACKUP=backup-grepit.txt

cp $FILE $CURRENT
cp $FILE $BACKUP

head -n 50 $CURRENT

while read LINE; do
  if [[ -z $LINE ]]; then
    cp $BACKUP $CURRENT
  else
    cp $CURRENT $BACKUP
    egrep -v "$LINE" $BACKUP  > $CURRENT
  fi
  head -n 50 $CURRENT
done
