#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== SUREFIRE REPORTS ===\n"

for F in c5db/target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done
