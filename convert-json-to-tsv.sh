#!/bin/sh

/bin/echo -e "container_1\tcontainer_2\tclass_1\tclass_2\tclass_name\tdescription\tline\tmethod_descriptor\tmethod_name\tmutator"

for d in jars/NEQ2/*/*/
do
	>&2 echo "Processing $d"
	b="${d%/}"
	toJar="$b.jar"
	fromJar="jars/EQ/${toJar#jars/NEQ2/}"
	(
		cd $d
		for json in `find . -name '*.json'`
		do
			classWithNoDot="${json#./}"
			toClass="${classWithNoDot%.json}.class"
			fromClass="${toClass%-*.class}.class"
			/bin/echo -e "$fromJar\t$toJar\t$fromClass\t$toClass\t$(jq -r '[.location.class, .description, .location.line, .location."method-descriptor", .location."method-name", .mutator] | join("\t")' "$json")"
			#/bin/echo -e "$fromJar\t$toJar\t$fromClass\t$toClass\tTODO"
		done
	)
done | sort
