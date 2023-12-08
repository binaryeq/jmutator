#!/usr/bin/perl

use strict;
use warnings;

foreach my $fn (`find jars/NEQ2 -name "*.json"`) {
	chomp $fn;
	my ($jarPath, $json) = $fn =~ m|^(jars/NEQ2/[^/]+/[^/]+)(/.*\.json)$| or die;
	my $outJar = "$jarPath.jar";
	my $inJar = $outJar;
	$inJar =~ s|^jars/NEQ2/|jars/EQ/| or die;
	my $outClass = $json;
	$outClass =~ s/\.json$/.class/;
	my $inClass = $outClass;
	$inClass =~ s/-\d+\.class$/.class/ or die;

	print qq({"inJar":"$inJar","outJar":"$outJar","inClass":"$inClass","outClass":"$outClass","d":);
	open my $jsonF, "<", $fn or die "Could not open '$fn': $!";
	while (<$jsonF>) {
		print;
	}
	close $jsonF;
	print "}\n";
}
