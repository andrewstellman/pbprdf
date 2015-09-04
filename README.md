# pbprdf
Generate RDF for basketball play-by-play data by reading a folder full of ESPN-style play-by-play HTML pages (eg. [Mystics vs. Sun on 7:00 PM ET, June 5, 2015](http://scores.espn.go.com/wnba/playbyplay?gameId=400610636)), processing each play in each game file, and generating a Turtle file that contains all of the plays from each game.

Install and run
===============

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


Example: Generate Turtle from the unit test data
------------------------------------------------
```
$ sbt "run src/test/resources/com/stellmangreene/pbprdf/test/htmldata/"
```

Example: Analyze a set of games
-------------------------------

Step 1: Clone the pbprdf repository
```
$ git clone https://github.com/andrewstellman/pbprdf.git
$ cd pbprdf
```

Step 2: Download a set of play-by-play HTML files
```
$ mkdir wnba-games
$ for ((i=400610636;i<=400610811;i++))
> do
> curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-games/$i.html
> done
```

Step 3: Run pbprdf and generate the Turtle file
```
$ sbt "run wnba-games wnba-rdf.ttl"
```

Step 4: Import the Turtle file into Sesame
```
$ console -s http://localhost:8080/openrdf-sesame MyRdfDatabase
Type 'help' for help.
MyRdfDatabase> load wnba-rdf.ttl
Loading data...
Data has been added to the repository (20410 ms)
```

Step 5: Run SPARQL queries
```
MyRdfDatabase> SPARQL
enter multi-line SPARQL query (terminate with line containing single '.')
BASE <http://www.stellman-greene.com/>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?teamName (COUNT(*) AS ?foulsDrawn) WHERE { 
  ?fouledPlayer pbprdf:foulDrawnBy ?player .
  ?roster pbprdf:hasPlayer ?player .
  ?roster rdfs:label ?teamName .
}
GROUP BY ?teamName
ORDER BY ?foulsDrawn
.
Evaluating SPARQL query...
+-------------------------------------+-------------------------------------+
| teamName                            | foulsDrawn                          |
+-------------------------------------+-------------------------------------+
| "Sparks"                            | "10136"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Sun"                               | "12101"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Mystics"                           | "12882"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Lynx"                              | "13129"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Storm"                             | "13452"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Dream"                             | "13457"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Stars"                             | "13932"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Liberty"                           | "13954"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Mercury"                           | "13992"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Fever"                             | "13997"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Shock"                             | "14329"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Sky"                               | "14909"^^<http://www.w3.org/2001/XMLSchema#integer>|
+-------------------------------------+-------------------------------------+
12 result(s) (1033 ms)
```
