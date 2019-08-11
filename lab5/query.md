# lab 5

### Verifying the Data

1. The oldest movie

   ```sqlite
   sqlite> SELECT primaryTitle, originalTitle, startYear 
   FROM title_basics 
   ORDER BY startYear 
   LIMIT 5;
   Passage de Venus	Passage de Venus	1874
   Sallie Gardner at a Gallop	Sallie Gardner at a Gallop	1878
   Athlete Swinging a Pick	Athlete Swinging a Pick	1881
   Buffalo Running	Buffalo Running	1883
   L'homme machine	L'homme machine	1885
   ```

   The oldest movie is Passage de Venus in 1874.

2. The  longest movie in 2009

   ```sqlite
   sqlite> SELECT primaryTitle, originalTitle, runtimeMinutes
   FROM title_basics 
   WHERE startYear = 2009 AND runtimeMinutes != "\N"
   ORDER BY runtimeMinutes DESC
   LIMIT 5;
   The Perfect Sleep	The Perfect Sleep	99
   Lynch Mob	Lynch Mob	99
   Everybody's Fine	Everybody's Fine	99
   Not Easily Broken	Not Easily Broken	99
   The Good Heart	The Good Heart	99
   ```

   There are many movies...

3. The year with the most movies

   ```sqlite
   sqlite> SELECT startYear, COUNT(*) AS num
   FROM title_basics 
   WHERE startYear != "\N"
   GROUP BY startYear
   ORDER BY num DESC
   LIMIT 5;
   2018	230915
   2017	227759
   2016	218583
   2015	202092
   2012	195530
   ```

   The year with the most movies is 2018.

4. The name of the person who contains in the most movies

   ```sqlite
   sqlite> SELECT primaryName FROM name
   WHERE nconst IN (
   SELECT nconst FROM (
   SELECT nconst, COUNT(*) AS num 
   FROM principals 
   GROUP BY nconst 
   ORDER BY num DESC))
   LIMIT 5;
   Fred Astaire
   Lauren Bacall
   Brigitte Bardot
   John Belushi
   Ingmar Bergman
   /* 
   nm0438471	13603 
   nm0914844	12849
   nm0251041	12195
   nm0438506	11565
   nm1916590	10574
   */
   
   /* OR use: */
   sqlite>  SELECT primaryName FROM name
   WHERE nconst IN
   (SELECT nconst, MAX(num) FROM
   (SELECT nconst, COUNT(*) AS num 
   FROM principals 
   GROUP BY nconst));
   ```

   The person is Fred Astaire.

5. The principal crew of the movie with highest average ratings and more than 500 votes

   ```sqlite
   sqlite>  SELECT * FROM crew 
   WHERE tconst IN 
   (SELECT tconst FROM ratings
   WHERE numVotes > 500
   ORDER BY averageRating DESC
   LIMIT 5);
   tt0394122	nm0532530	nm0229004,nm0532530
   tt0514388	nm0003941	nm0532530,nm0696400
   tt9566030	nm5579304	nm5579304
   tt9906260	\N	\N
   sqlite>  SELECT primaryName FROM name WHERE nconst IN ("nm0532530", "nm0229004"564);
   D.J. MacHale
   Will Dixon
   ```

   The crew has ID tt0394122, with director D.J. MacHale and writers Will Dixon and D.J. MacHale

6. The count of each Pair<BirthYear, DeathYear> of the people

   ```sqlite
   sqlite>  SELECT birthYear, deathYear, COUNT(*) AS ct FROM name
   WHERE birthYear!="\N" AND deathYear!="\N"
   GROUP BY birthYear, deathYear
   ORDER BY ct DESC;
   1928	2012	135
   1930	2013	135
   1928	2010	132
   1928	2013	131
   1930	2018	130
   1924	2009	129
   1927	2015	129
   1929	2015	129
   1926	2007	127
   1921	2010	126
   ...
   ```



### Advanced Analysis with the new Tables

1. The top 3 most common professions among these people and also the average life span of these three professions 

   First, create a new table.

   ```sqlite
   sqlite>  CREATE TABLE prof
   (
   nconst varchar(10) not null,
   profession text not null,
   span int not null,
   foreign key(nconst) references name(nconst)
   );
   ```

   Using python to write table.

   ```sqlite
   sqlite>  SELECT profession, COUNT (*) as num, AVG(span)
   FROM prof 
   GROUP BY profession
   ORDER BY num DESC
   LIMIT 3;
   actor	62136	69.3966138792326
   writer	31966	71.3838453356692
   actress	26926	72.7062690336478
   ```

2. The top 3 most popular (received most votes) genres

   Create a table for genres:

   ```sqlite
   sqlite>  CREATE TABLE genres
   (
   tconst varchar(10) not null,
   genre text not null,
   span int not null
   );
   ```

   Then query on tables `ratings` and `genres`:

   ```sqlite
   sqlite>  SELECT genre, AVG(ratings.numVotes)AS avgs
   FROM ratings, genres
   WHERE ratings.tconst=genres.tconst
   GROUP BY genres.genre
   ORDER BY avgs DESC
   LIMIT 5;
   Mystery	8304.24814264487
   Action	7204.44046844502
   Horror	6328.89453125
   Sci-Fi	5910.4081237911
   Crime	5896.27493074792
   ```

   The most popular genres are Mystery, Action and Horror.

3. The average time span (endYear - startYear) of the titles for each person

   Splite titles each person has.

   ```sqlite
   sqlite>  CREATE TABLE titles
   (
   nconst varchar(10) not null,
   primaryName text not null,
   title text not null
   );
   ```

   Query on tables `titles` and `genres`:

   ```sqlite
   sqlite>  SELECT primaryName, AVG(span) as sp FROM titles, genres
   WHERE title=tconst
   GROUP BY nconst
   /* ORDER BY sp DESC*/
   LIMIT 5;
   Dog Ear Films	75.0
   Allen Ditto	75.0
   Ian Botham	68.0
   Mike Brearley	68.0
   Jackie Fullerton	68.0
   ```

   We can limit (and in descending order) our querries to 5 results.

   

### Reference

1. [IMDb data](<https://www.imdb.com/interfaces/>)
2. SQLite [tutorial](<http://www.sqlitetutorial.net/>)
3. Python API [sqlite3](<https://docs.python.org/3/library/sqlite3.html>)

