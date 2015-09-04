$!/bin/bash

# fetch 2014 regular season
mkdir wnba-2014-season
for ((i=400539461;i<=400539662;i++))
do
   curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-2014-season/$i.html
done
curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-2014-season/400539664.html


# fetch 2014 playoffs
mkdir wnba-2014-playoffs
for i in 400580067 400580068 400580069 400580070 400580071 400578287 400578288 400578290 400578291 400581627 400581628 400581629 400581821 400581822 400581823 400582773 400582774 400582775
do
   curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-2014-playoffs/$i.html
done


# fetch 2013 regular season
mkdir wnba-2013-season
for ((i=400445705;i<=400445900;i++))
do
   curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-2013-season/$i.html
done


# fetch 2013 playoffs
mkdir wnba-2013-playoffs
for i in 400496774 400496775 400496776 400496777 400496778 400496779 400496780 400496781 400496783 400496784 400499743 400499744 40049976 400499747 400505694 400505695 400505696
do
   curl http://scores.espn.go.com/wnba/playbyplay?gameId=$i > wnba-2013-playoffs/$i.html
done

