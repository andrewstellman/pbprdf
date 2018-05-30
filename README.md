# pbprdf
Generate RDF for NBA or WNBA basketball play-by-play data by reading a folder full of ESPN-style play-by-play HTML pages (eg. [Pacers vs. Cavaliers, April 15, 2018](http://tv5.espn.com/nba/playbyplay?gameId=401029417) or [Mystics vs. Sun, June 5, 2015](http://scores.espn.go.com/wnba/playbyplay?gameId=400610636)), processing each play in each game file, and generating a Turtle file that contains all of the plays from each game.

Install and run
===============

__Prerequisite: [sbt](http://www.scala-sbt.org/) 1.x and Java 8 or later must be in your path__
* [Installing SBT](https://www.scala-sbt.org/1.x/docs/Setup.html)
* [Install sbt 1.x on Mac](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Mac.html)
* [Install sbt 1.x on Unix](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html)
* [Install sbt 1.x on Windows](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html)

Mac or Unix: 
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

Step 4: Import the Turtle file into RDF4J Server
```
$ console -s http://localhost:8080/rdf4j-server PbpRdfDatabase
Type 'help' for help.
PbpRdfDatabase> load wnba-2014-playoffs.ttl into http://stellman-greene.com/pbprdf/wnba-2014-playoffs
Loading data...
Data has been added to the repository (20410 ms)
```

If your file is large, you can use zip or gzip to compress it. Make sure it has the extension `.ttl.zip`:

```
$ ./fetch-nba-play-by-plays.sh
$ ./pbprdf data/nba-2017-2018-season/ nba-2017-2018-season.ttl
$ zip nba-2017-2018-season.ttl.zip nba-2017-2018-season.ttl
$ console -s http://localhost:8080/rdf4j-server PbpRdfDatabase
Type 'help' for help.
PbpRdfDatabase> load nba-2017-2018-season.ttl.zip into http://stellman-greene.com/pbprdf/nba-2017-2018
Loading data...
Data has been added to the repository (427100 ms)
```

__See 'Setting up RDF4J Server' below for details on setting up RDF4J server__

Step 5: Run SPARQL queries
```
PbpRdfDatabase> SPARQL
enter multi-line SPARQL query (terminate with line containing single '.')
BASE <http://stellman-greene.com/>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
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

Example: Load the ontology into RDF4J Server
--------------------------------------------

Step 1: Generate the ontology
```
$ ./pbprdf --ontology ontology.ttl
```

Step 2: Load the ontology into its own context
```
PbpRdfDatabase> load ontology.ttl into http://stellman-greene.com/pbprdf/Ontology
Loading data...
Data has been added to the repository (18 ms)
```

Step 3: Execute a query that retrieves only the data in the ontology
```
PbpRdfDatabase> SPARQL
enter multi-line SPARQL query (terminate with line containing single '.')
BASE <http://stellman-greene.com/>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
SELECT *
FROM NAMED <http://stellman-greene.com/pbprdf/Ontology>
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
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Block                        |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Enters                       |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Event                        |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Foul                         |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Game                         |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:JumpBall                     |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Play                         |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Player                       |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Rebound                      |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Roster                       |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Shot                         |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Team                         |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:TechnicalFoul                |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Timeout                      |
| <http://stellman-greene.com/pbprdf/Ontology>| pbprdf:Turnover                     |
+-------------------------------------+-------------------------------------+
15 result(s) (60 ms)
```

Other Useful Queries
====================

Clutch Shots
------------
```
BASE <http://stellman-greene.com/>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
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
BASE <http://stellman-greene.com/>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
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

Setting up RDF4J Server
=======================

One effective way to execute SPARQL queries against these files is to use [RDF4J Server, Workbench, and Console](http://docs.rdf4j.org/server-workbench-console/). RDF4J Server and its GUI, RDF Workbench, are both web applications that run in an application server like Tomcat.

__Step 1: [Download RDF4J](http://rdf4j.org/download/)__
Download and extract the latest RDF4J SDK. It will contain a `bin` folder with the `console` binary, and a `war` folder with the `rdf4j-server.war` and `rdf4j-workbench.war` web applications.

__Step 2: [Install Apache Tomcat](https://tomcat.apache.org/tomcat-7.0-doc/appdev/installation.html)__
Make sure you edit libexec/conf/tomcat-users.xml to add a user with `tomcat` and `manager-gui` permissions.

__Step 3: Open the Apache Tomcat App Manager (http://localhost:8080/manager/html) and deploy the web applications
Use the app manager GUI to deploy the `rdf4j-server.war` and `rdf4j-workbench.war` web applications to your Tomcat installations.

__Step 4: Use the RDF4J console to create a database__
Create a Native database with `spoc`,`sopc`,`opsc`,`ospc`,`posc`, and `psoc` indexes. This will take disk space for the indexes, but will make your queries run much faster.

```
$ ./console.sh -s http://localhost:8080/rdf4j-server 
Connected to http://localhost:8080/rdf4j-server

> create native
Please specify values for the following variables:
Repository ID [native]: PbpRdfDatabase
Repository title [Native store]: PBPRDF Database
Query Iteration Cache size [10000]: 
Triple indexes [spoc,posc]: spoc,sopc,opsc,ospc,posc,psoc
EvaluationStrategyFactory [org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategyFactory]: 
Repository created
```

__Step 5: Import your Turtle file__
You can use the instructions above to import your `*.ttl` or `*.ttl.zip` files into your newly created database. You can either use the RDF4J console or RDF4J workbench GUI to execute SPARQL queries.
