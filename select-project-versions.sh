#!/bin/bash

if [ -z "$JARS" ]
then
	echo "Must specify the jars folder with env var JARS"
	exit 1
fi

if [ -z "$DATASET" ]
then
	echo "Must specify the path to the dataset.json file with env var DATASET"
	exit 1
fi

#for f in $JARS/*/*.jar; do echo ${f##*/}; done|sort|uniq > project_names.txt
jq -r '.[] | .name + "\t" + .jar' < "$DATASET" > project_names.txt
perl -le 'sub svcmp($$) { my ($x, $y) = @_; return 0 if !@$x && !@$y; return -1 if !@$x; return 1 if !@$y; return ($x->[0] <=> $y->[0]) || svcmp([@$x[1..$#{$x}]], [@$y[1..$#{$y}]]); } sub getsv($) { my ($before, $sv, $after) = $_[0] =~ m|^(\S+)\t.+-([\d.]+)((?:-tests)?\.jar)$| or die "Cannot find semver in $_[0]"; return ([ split /\./, $sv ], "$after$before"); } my @lines = <>; chomp @lines; @lines = sort { my ($asv, $arest) = getsv($a); my ($bsv, $brest) = getsv($b); return ($arest cmp $brest) || svcmp($asv, $bsv); } @lines; for (my $i = 0; $i < @lines; ++$i) { print $lines[$i] if ($i == @lines - 1 || (getsv($lines[$i]))[1] ne (getsv($lines[$i + 1]))[1]); }' < project_names.txt > latest_project_versions.txt
perl -lane 'print $F[1]; $F[1] =~ s|\.jar$|-tests.jar|; print $F[1]' < latest_project_versions.txt > latest_project_versions_with_tests.txt
for dd in $JARS/*/; do d=${dd%/}; echo `grep -F -f <(ls $d|grep '\.jar$') latest_project_versions.txt |wc -l`"   $d"; done | sort -nr > project_versions_successful_builds.txt
