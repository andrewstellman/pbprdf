#!/bin/sh

JAVA_OPT=-mx512m

lib="$(dirname "${0}")/target/scala-2.11"
java $JAVA_OPT -cp "$lib/$(ls "$lib"|xargs |sed "s; ;:$lib/;g")" com.stellmangreene.pbprdf.PbpRdfApp $*
