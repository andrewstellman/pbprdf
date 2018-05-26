#!/bin/bash 

FOLDER=data/retrieved_`date +%s`
echo "Fetching play-by-play files into $FOLDER"
mkdir -p $FOLDER/

mkdir $FOLDER/nba-2017-season

# fetch 2017-18 regular season (up to 12-25-2017)
for ((i=400974437;i<=400974449;i++))
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-season/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-season/$i-gameinfo.html
done

# fetch 2017-18 regular season (12-25-2017 to 2018)
for ((i=400975244;i<=400975976;i++))
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-season/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-season/$i-gameinfo.html
done

