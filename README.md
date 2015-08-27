# pbprdf
Generate RDF for basketball play-by-play data

Installation
============

__Prerequisite: must have [sbt](http://www.scala-sbt.org/) in your path.__

Download or clone the source, then run:
```
$ sbt run
```

Run the unit tests:
```
$ sbt compile test
```

Generate an Eclipse project:
```
$ sbt eclipse
```



Usage
=====




```
$ for ((i=400610636;i<=400610798;i++))
> do
> curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > $i.html
> done
```
