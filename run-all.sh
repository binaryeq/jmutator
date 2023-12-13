#!/bin/bash

if [ -z "$JARS" ]
then
	echo "Must specify the jars folder with env var JARS"
	exit 1
fi

# List latest compilable versions of each project into latest_project_versions.txt
./select-project-versions.sh

# I decided on this compiler version manually
COMPILER="${COMPILER:-openjdk-11.0.19}"

# Copy selected project versions to local dir
mkdir -p "data/original-jars/$COMPILER"
for f in `cat latest_project_versions.txt`; do echo $f; cp "$JARS/$COMPILER/$f" "data/original-jars/$COMPILER"; done

# Generate mutated classes
time for d in data/original-jars/$COMPILER/*; do echo $d; m=${d/original-/mutated-}; echo $m; mkdir -p $m && java -jar target/jmutator.jar -b $d -m $m -p '$n-$i.class' -j '$n-$i.json' -v >$m.stdout 2>$m.stderr; done

# Convert JSON results to TSV
time ./convert-json-to-tsv-faster.sh > NEQ2.tsv

# Jar up classes
time for d in jars/NEQ2/$COMPILER/*/; do echo $d; b=`basename ${d%/}`; echo $b; ( cd `dirname $d` && find $b -name '*.class' > $b.filelist && jar --create --file $b.jar @$b.filelist ) ; done