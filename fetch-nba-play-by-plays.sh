#!/bin/bash 

FOLDER=data/retrieved_`date +%s`
echo "Fetching play-by-play files into $FOLDER"
mkdir -p $FOLDER/

# fetch 2017-18 regular season (up to 12-25-2017)
mkdir $FOLDER/nba-2017-2018-season
#for ((i=400974437;i<=400974449;i++))
#do
#   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-2018-season/$i.html
#   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-2018-season/$i-gameinfo.html
#done

for ((i=400974700;i<=400974705;i++))
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-2018-season/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-2018-season/$i-gameinfo.html
done

for ((i=400974766;i<=400975243;i++))
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-2018-season/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-2018-season/$i-gameinfo.html
done

exit

# fetch 2017-18 regular season (12-25-2017 to 2018)
for ((i=400975244;i<=400975976;i++))
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2017-2018-season/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2017-2018-season/$i-gameinfo.html
done

# fetch 2018 playoffs
mkdir $FOLDER/nba-2018-playoffs
for i in 401029441 401029410 401029439 401029459 401029429 401029417 401029438 401029411 401029442 401029446 401029412 401029430 401029460 401029421 401029440 401029414 401029443 401029461 401029453 401029424 401029413 401029432 401029444 401029462 401029418 401029445 401029434 401029455 401029415 401029427 401029422 401029450 401029435 401029447 401029456 401029416 401029428 401029423 401029451 401029436 401029419 401029431 401029452 401029437 401031412 401029433 401031590 401031671 401031713 401031645 401031639 401031714 401031672 401031646 401031640 401031673 401031715 401031647 401031641 401031674 401031716 401031642 401031648 401031675 401032840 401032761 401032841 401032762 401032842 401032763 401032843 401032764 401032844 401032765 401032845 401032766
do
   curl http://www.espn.com/nba/playbyplay?gameId=$i > $FOLDER/nba-2018-playoffs/$i.html
   curl http://www.espn.com/nba/game?gameId=$i > $FOLDER/nba-2018-playoffs/$i-gameinfo.html
done

echo "Fetched play-by-play files into $FOLDER"

