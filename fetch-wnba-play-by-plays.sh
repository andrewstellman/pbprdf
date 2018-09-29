#!/bin/bash 

FOLDER=data/retrieved_`date +%s`
echo "Fetching play-by-play files into $FOLDER"
mkdir -p $FOLDER/

# fetch 2018 playoffs
mkdir $FOLDER/wnba-2018-playoffs
for i in 401074453 401074454 401074455 401074456 401074459 401074457 401074460 401074458 401074473 401074461 401074462 401074463 401074465 401074464 401079693 401079694 401079695
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2018-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2018-playoffs/$i-gameinfo.html
done

# fetch 2018 regular season
mkdir $FOLDER/wnba-2018-season
for ((i=401018781;i<=401018984;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2018-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2018-season/$i-gameinfo.html
done


# fetch 2017 playoffs
mkdir $FOLDER/wnba-2017-playoffs
for i in 400981090 400981091 400981092 400981093 400981094 400981099 400981095 400981100 400981096 400981101 400981428 400981429 400981430 401018784 400981432
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2017-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2017-playoffs/$i-gameinfo.html
done

# fetch 2017 regular season
mkdir $FOLDER/wnba-2017-season
for ((i=400927392;i<=400927595;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2017-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2017-season/$i-gameinfo.html
done


# fetch 2016 playoffs
mkdir $FOLDER/wnba-2016-playoffs
for i in 400910430 400910431 400910450 400910451 400910452 400910457 400910453 400910458 400910459 400910454 400910460 400920400 400920461 400920462 400920463 400920464 
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2016-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2016-playoffs/$i-gameinfo.html
done

# fetch 2016 regular season
mkdir $FOLDER/wnba-2016-season
for ((i=400864357;i<=400864495;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2016-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2016-season/$i-gameinfo.html
done


# fetch 2015 regular season
mkdir $FOLDER/wnba-2015-season
for ((i=400610636;i<=400610839;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2015-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2015-season/$i-gameinfo.html
done

# fetch 2015 playoffs
mkdir $FOLDER/wnba-2015-playoffs
for i in 400839633 400839118 400839630 400838810 400839634 400839119 400839631 400838811 400839635 400839632 400844305 400844348 400844306 400844349 400844307 400847195 400847196 400847197 400847198 400847199
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2015-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2015-playoffs/$i-gameinfo.html
done


# fetch 2014 regular season
mkdir $FOLDER/wnba-2014-season
for ((i=400539461;i<=400539662;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2014-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2014-season/$i-gameinfo.html
done
curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2014-season/400539664.html
curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2014-season/400539664-gameinfo.html


# fetch 2014 playoffs
mkdir $FOLDER/wnba-2014-playoffs
for i in 400580067 400580068 400580069 400580070 400580071 400578287 400578288 400578290 400578291 400581627 400581628 400581629 400581821 400581822 400581823 400582773 400582774 400582775
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2014-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2014-playoffs/$i-gameinfo.html
done


# fetch 2013 regular season
mkdir $FOLDER/wnba-2013-season
for ((i=400445705;i<=400445900;i++))
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2013-season/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2013-season/$i-gameinfo.html
done


# fetch 2013 playoffs
mkdir $FOLDER/wnba-2013-playoffs
for i in 400496774 400496775 400496776 400496777 400496778 400496779 400496780 400496781 400496783 400496784 400499743 400499744 400499746 400499747 400505694 400505695 400505696
do
   curl http://www.espn.com/wnba/playbyplay?gameId=$i > $FOLDER/wnba-2013-playoffs/$i.html
   curl http://www.espn.com/wnba/game?gameId=$i > $FOLDER/wnba-2013-playoffs/$i-gameinfo.html
done

echo "Fetched play-by-play files into $FOLDER"

