#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== SUREFIRE REPORTS ===\n"

for i in  hawtjni-runtime hawtjni-generator maven-hawtjni-plugin hawtjni-example hawtjni-website 
	do for F in ${i}/target/surefire-reports/*.txt
		do echo $F
    		cat $F
    		echo
	done
done
