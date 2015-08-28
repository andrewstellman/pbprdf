# pbprdf
Generate RDF for basketball play-by-play data. 
o Read a folder full of ESPN-style play-by-play HTML pages (eg. [Mystics vs. Sun on 7:00 PM ET, June 5, 2015](http://scores.espn.go.com/wnba/playbyplay?gameId=400610636))
o Generate a Turtle file that contains all of the plays from each game

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
$ for ((i=400610636;i<=400610798;i++))
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
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>
SELECT ?player (COUNT(*) as ?count) {
   ?game pbprdf:foulDrawnBy ?player .
}
GROUP BY ?player
ORDER BY DESC(?count)
LIMIT 10
.
Evaluating SPARQL query...
+-------------------------------------+-------------------------------------+
| player                              | count                               |
+-------------------------------------+-------------------------------------+
| "Elena Delle Donne"                 | "156"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Angel McCoughtry"                  | "151"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Maya Moore"                        | "143"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "DeWanna Bonner"                    | "106"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Riquna Williams"                   | "96"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Sophia Young-Malcolm"              | "95"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Karima Christmas"                  | "94"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Nneka Ogwumike"                    | "90"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Tina Charles"                      | "89"^^<http://www.w3.org/2001/XMLSchema#integer>|
| "Plenette Pierson"                  | "88"^^<http://www.w3.org/2001/XMLSchema#integer>|
+-------------------------------------+-------------------------------------+
10 result(s) (85 ms)
```
