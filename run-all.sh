#!/bin/bash

if [ -z "$JARS" ]
then
	echo "Must specify the jars folder with env var JARS"
	exit 1
fi

# List latest compilable versions of each project into latest_project_versions_with_tests.txt
./select-project-versions.sh

# I decided on this compiler version manually
COMPILER="${COMPILER:-openjdk-11.0.19}"

# Find the subset of jars that actually exist (not all projects have buildable test jars)
for f in `cat latest_project_versions_with_tests.txt`; do if [ -e "$JARS/$COMPILER/$f" ]; then echo $f; fi; done > actually_existing_latest_project_versions.txt

# Extract selected project versions to local dir
mkdir -p "jars/EQ/$COMPILER"
for f in `cat actually_existing_latest_project_versions.txt`; do echo "$f"; d="jars/EQ/$COMPILER/${f%.jar}"; echo "$d"; mkdir -p "$d"; ( cd "$d" && unzip "$JARS/$COMPILER/$f" ); done

# Generate mutated classes
mvn package
mkdir -p "jars/NEQ2/$COMPILER"
time for d in jars/EQ/$COMPILER/*; do echo $d; m=${d/EQ/NEQ2}; echo $m; mkdir -p $m && java -jar target/jmutator.jar -b $d -m $m -p '$n-$i.class' -j '$n-$i.json' -v >$m.stdout 2>$m.stderr; done

# Convert JSON results to TSV
time ./convert-json-to-tsv-faster.sh > NEQ2.tsv

# Jar up classes
time for d in jars/NEQ2/$COMPILER/*/; do echo $d; b=`basename ${d%/}`; echo $b; ( cd `dirname $d` && find $b -name '*.class' > $b.filelist && jar --create --file $b.jar @$b.filelist ) ; done
