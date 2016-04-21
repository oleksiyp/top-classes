#!/bin/bash

egrep -hR "^import" src/main/java |\
 sed 's!import\( static\)\? !!' |\
 sort |\
 uniq -c |\
 sort -nr |\
 tee result.txt |\
 head -n 50
