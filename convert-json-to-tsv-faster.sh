#!/bin/sh

/bin/echo -e "container_1\tcontainer_2\tclass_1\tclass_2\tclass_name\tdescription\tline\tmethod_descriptor\tmethod_name\tmutator"

./convert-json-to-tsv-faster.pl | jq -r '[.inJar, .outJar, .inClass, .outClass, .d.location.class, .d.description, .d.location.line, .d.location."method-descriptor", .d.location."method-name", .d.mutator] | join("\t")' | sort
