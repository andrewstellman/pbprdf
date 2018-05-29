# pbprdf
Generate RDF for NBA or WNBA basketball play-by-play data by reading a folder full of ESPN-style play-by-play HTML pages (eg. [Pacers vs. Cavaliers, April 15, 2018](http://tv5.espn.com/nba/playbyplay?gameId=401029417) or [Mystics vs. Sun, June 5, 2015](http://scores.espn.go.com/wnba/playbyplay?gameId=400610636)), processing each play in each game file, and generating a Turtle file that contains all of the plays from each game.

Install and run
===============

__Prerequisite: [sbt](http://www.scala-sbt.org/) 1.x and Java 8 or later must be in your path__
* [Installing SBT](https://www.scala-sbt.org/1.x/docs/Setup.html)
* [Install sbt 1.x on Unix](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html)
* [Install sbt 1.x on Mac](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Mac.html)
* [Install sbt 1.x on Windows](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html)

Unix: 
```
$ git clone https://github.com/andrewstellman/pbprdf.git
$ cd pbprdf
$ sbt assembly
$ ./pbprdf
```

Windows:
```
C:\Users\Public\src>git clone https://github.com/andrewstellman/pbprdf.git
C:\Users\Public\src>cd pbprdf
C:\Users\Public\src\pbprdf>sbt assembly
C:\Users\Public\src\pbprdf>pbprdf
```

Other useful build commands
---------------------------

Compile the code and the unit tests:
```
$ sbt compile test
```

Generate an Eclipse project:
```
$ sbt eclipse
```

Generate sample Turtle from the unit test data and print it to the console

using the script:
```
$ ./pbprdf src/test/resources/com/stellmangreene/pbprdf/test/htmldata/
```

via SBT:
```
$ sbt "run src/test/resources/com/stellmangreene/pbprdf/test/htmldata/"
```

Examples
========

Example: Analyze a set of games
-------------------------------

Step 1: Clone the pbprdf repository
```
$ git clone https://github.com/andrewstellman/pbprdf.git
$ cd pbprdf
```

Step 2: Download a set of play-by-play HTML files
```
$ ./fetch-wnba-play-by-plays.sh
```

Step 3: Run pbprdf and generate the Turtle file for the 2014 WNBA playoffs
```
$ ./pbprdf data/wnba-2014-playoffs/ wnba-2014-playoffs.ttl
```

Step 4: Import the Turtle file into Sesame
```
$ console -s http://localhost:8080/openrdf-sesame MyRdfDatabase
Type 'help' for help.
MyRdfDatabase> load wnba-2014-playoffs.ttl into http://www.stellman-greene.com/pbprdf/wnba-2014-playoffs
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

Example: Load the ontology into Sesame
--------------------------------------

Step 1: Generate the ontology
```
$ ./pbprdf --ontology ontology.ttl
```

Step 2: Load the ontology into its own context
```
MyRdfDatabase> load ontology.ttl into http://www.stellman-greene.com/pbprdf/Ontology
Loading data...
Data has been added to the repository (18 ms)
```

Step 3: Execute a query that retrieves only the data in the ontology
```
MyRdfDatabase> SPARQL
enter multi-line SPARQL query (terminate with line containing single '.')
BASE <http://www.stellman-greene.com/>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
SELECT *
FROM NAMED <http://www.stellman-greene.com/pbprdf/Ontology>
WHERE {
  GRAPH ?graph {
    ?class a owl:Class
  }
}
.
Evaluating SPARQL query...
+-------------------------------------+-------------------------------------+
| graph                               | class                               |
+-------------------------------------+-------------------------------------+
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Block                        |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Enters                       |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Event                        |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Foul                         |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Game                         |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:JumpBall                     |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Play                         |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Player                       |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Rebound                      |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Roster                       |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Shot                         |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Team                         |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:TechnicalFoul                |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Timeout                      |
| <http://www.stellman-greene.com/pbprdf/Ontology>| pbprdf:Turnover                     |
+-------------------------------------+-------------------------------------+
15 result(s) (60 ms)
```

Other Useful Queries
====================

Clutch Shots
------------
```
BASE <http://www.stellman-greene.com/>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?playerName ?shotsTaken ?shotsMade ?shotPercentage
WHERE 
{ 
  ?player a pbprdf:Player .
  ?player rdfs:label ?playerName .
  
  # Find the number of shots taken
  {
    SELECT ?player (COUNT(?shot) AS ?shotsTaken)
    WHERE 
    {
      ?shot a pbprdf:Shot .
      ?shot pbprdf:shotBy ?player .
      ?shot pbprdf:secondsLeftInPeriod ?secondsLeftInPeriod .
      FILTER (?secondsLeftInPeriod < 5)
    }
    GROUP BY ?player 
  }

  # Find the number of shots made
  {
    SELECT ?player (COUNT(?shot) AS ?shotsMade)
    WHERE 
    {
      ?shot a pbprdf:Shot .
      ?shot pbprdf:shotBy ?player .
      ?shot pbprdf:shotMade "true"^^xsd:boolean .
      ?shot pbprdf:secondsLeftInPeriod ?secondsLeftInPeriod .
      FILTER (?secondsLeftInPeriod < 5)
    }
    GROUP BY ?player 
  }
  
  # Calculate the shot percentage
  BIND ( (round((?shotsMade / ?shotsTaken) * 10000)) / 100 AS ?shotPercentage ) .
  
  # Only match players who took more than 10 shots just before the end of the period
  FILTER (?shotsTaken >= 15) .
}
ORDER BY DESC(?shotPercentage)
```

Shots made and missed at Target Center in the first five minutes
----------------------------------------------------------------
```
BASE <http://www.stellman-greene.com/>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?game ?gameTime ?shotsTaken ?shotsMade ?shotsMadePercentage ?shotsMissed ?shotsMissedPercentage
WHERE 
{ 
  ?game a pbprdf:Game .
  ?game pbprdf:gameTime ?gameTime .
  ?game pbprdf:gameLocation "Target Center, Minneapolis, MN" .

  # Find the number of shots made per game
  {
    SELECT ?game (COUNT(?madeShot) AS ?shotsMade) {
      ?madeShot a pbprdf:Shot .
      ?madeShot pbprdf:inGame ?game .
      ?madeShot pbprdf:shotMade ?made .
      ?madeShot pbprdf:shotMade "true"^^xsd:boolean .
      ?madeShot pbprdf:secondsIntoGame ?secondsIntoGame .
      FILTER (?secondsIntoGame < 300)
    }
    GROUP BY ?game
  }
  
  # Find the number of shots missed per game
  {
    SELECT ?game (COUNT(?missedShot) AS ?shotsMissed) {
      ?missedShot a pbprdf:Shot .
      ?missedShot pbprdf:inGame ?game .
      ?missedShot pbprdf:shotMade ?made .
      ?missedShot pbprdf:shotMade "false"^^xsd:boolean .
      ?missedShot pbprdf:secondsIntoGame ?secondsIntoGame .
      FILTER (?secondsIntoGame < 300)
    }
    GROUP BY ?game
  }
  
  BIND ((?shotsMade + ?shotsMissed) AS ?shotsTaken) .
  BIND ( (round((?shotsMade / ?shotsTaken) * 10000)) / 100 AS ?shotsMadePercentage ) .
  BIND ( (round((?shotsMissed / ?shotsTaken) * 10000)) / 100 AS ?shotsMissedPercentage ) .
}
LIMIT 100
```

