#### Ex. 1 — Reminders on database

##### 1. `join`

##### 2. aggregate function

##### 3. drill queries

By the way, at first, I wanted to use pydrill because I thought it would be more user-friendly than sql. However I found out that it was simply using sql in python, so I gave up on it.

First connect drill to local file path and set up the configuration. For simplicity, we connect it to hdfs with the configuration:

```json
{
  "type": "file",
  "connection": "hdfs://localhost:9000/",
  "config": null,
  "workspaces": {
    "root": {
      "location": "/Users/kelly/drill",
      "writable": true,
      "defaultInputFormat": null,
      "allowAccessOutsideWorkspace": false
    }
  },
  "formats": {
    "json": {
      "type": "json",
      "extensions": [
        "json"
      ]
    },
    "csv": {
      "type": "text",
      "extensions": [
        "csv"
      ],
      "skipFirstLine": true,
      "delimiter": ","
    }
  },
  "enabled": true
}
```

Next, upload all the files we need to hadoop.

```sh
~/hadoop/bin/hdfs dfs -put * hdfs://localhost:9000/
```

Then use `show file` in apache drill, we can get

![](/Users/kelly/Desktop/2019_Summer/Ve572/hw/hw4/1.3.1.png)

Since `2017.csv` is too large, we select a small piece from it to `s17.csv`. The csv files are without header, and we really want to know what each column represents.

From the [format file](ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/by_year/ghcn-daily-by_year-format.rtf) provided on the official website, 

```text
ID = 11 character station identification code
YEAR/MONTH/DAY = 8 character date YYYYMMDD
ELEMENT = 4 character indicator of element type
DATA VALUE = 5 character data value for ELEMENT
```

The file `ghcnd-stations.txt` contains all stations and their metadata.

```tcl
------------------------------
Variable   Columns   Type
------------------------------
ID            1-11   Character
LATITUDE     13-20   Real
LONGITUDE    22-30   Real
ELEVATION    32-37   Real
STATE        39-40   Character
NAME         42-71   Character
GSN FLAG     73-75   Character
HCN/CRN FLAG 77-79   Character
WMO ID       81-85   Character
------------------------------
```

The weather info are contained in `2017.csv`.

```tcl
ELEMENT   the element type. 
```

 The elements required for investigation are: 

```text
PRCP = Precipitation (tenths of mm)

MNPN = Daily minimum temperature of water in an evaporation pan (tenths of degrees C)
MXPN = Daily maximum temperature of water in an evaporation pan (tenths of degrees C)

TAVG = Average temperature (tenths of degrees C) [Note that TAVG from source 'S' corresponds 				to an average for the period ending at 2400 UTC rather than local midnight]
```



```sql
select * from (select columns[0] as id, columns[1] as dates, columns[2] as x2, columns[3] as x3, columns[4] as x4, columns[5] as x5, columns[6] as x6, columns[7] as x7 from hdfs.`2017\.csv`) where soundex(id)=soundex('USS0021B60S');
```

Since we want to get the output, we can record the query result to file for storage. Using `!set` to set file format and `!record` to record.

```cassandra
!set outputformat 'csv'
```

Sadly this also changes the output format to screen, which is a much more ugly than the table format. Start and stop recording by

```
!record 'file-name.csv'
!record
```

No args means close recording.



#### Ex. 2 — Holidays!

##### First get all records about average temperature.

Check distinct days of the year: all 365 days are included.

```sql
apache drill (hdfs)> SELECT DISTINCT day AS anyemp FROM (select columns[1] as day from hdfs.`2017\.csv`);
... omit records
365 rows selected (2.609 seconds)
```

First, choose a week of sepcial interest. Suppose the week is August 11-17.

```sql
SELECT * FROM (select columns[0] as id, columns[1] as day, columns[2] as x2, columns[3] as x3, columns[4] as x4, columns[5] as x5, columns[6] as x6, columns[7] as x7 from hdfs.`2017\.csv`) where day in ('20170811','20170812','20170813','20170814','20170815','20170816','20170817');
```

Stores all the info within the week into `week.csv` for future use. However, this will make add quotes to original data, making `20170101` become `'20170101'`. Thus a better choice is to create table. Tables can only be created in writable schemes, such as `df.tmp`.

 ```sql
CREATE TABLE dfs.tmp.`week` AS SELECT * FROM (select columns[0] as id, columns[1] as day, columns[2] as x2, columns[3] as x3, columns[4] as x4, columns[5] as x5, columns[6] as x6, columns[7] as x7 from hdfs.`2017\.csv`) where day in ('20170811','20170812','20170813','20170814','20170815','20170816','20170817');
 ```

Suppose temperature is the most important element we consider. First, try to filter out all records regarding average temperature.

```sql
CREATE TABLE dfs.tmp.`tavg` AS SELECT * FROM dfs.tmp.`week` where x2 in ('TAVG');
```

Accoridng to the data description, the temperautre is tenths of $^{\circ} \mathrm{C}$. The average temperature in the week can be queried by

```sql
SELECT id, AVG(CAST(x3 AS decimal(28, 2)))/10 FROM dfs.tmp.`tavg` where x2 in ('TAVG') group by id;
```

Next, get the total precipitation (tenths of mm) of the week.

```sql
SELECT id, SUM(CAST(x3 AS decimal(28, 2)))/10 FROM dfs.tmp.`week` where x2 in ('PRCP') group by id;
```

Next, min and max temperatures, first create tables.

```sql
CREATE TABLE dfs.tmp.`tmin` AS SELECT id, AVG(CAST(x3 AS decimal(28, 2)))/10 FROM dfs.tmp.`week` where x2 in ('MNPN') group by id;

CREATE TABLE dfs.tmp.`tmax` AS SELECT id, AVG(CAST(x3 AS decimal(28, 2)))/10 FROM dfs.tmp.`week` where x2 in ('MXPN') group by id;

CREATE TABLE dfs.tmp.`range` AS select tbl1.id, tbl1.EXPR$1 as tmin, tbl2.EXPR$1 as tmax from dfs.tmp.`tmin` as tbl1
join
dfs.tmp.`tmax` as tbl2
on tbl1.id=tbl2.id;
```

Combine all possible records  together,

```sql
create TABLE dfs.tmp.`tmp` AS select tbl1.id, tbl1.tmin, tbl1.tmax, tbl2.EXPR$1 as prcp from dfs.tmp.`range` as tbl1
join
(SELECT id, SUM(CAST(x3 AS decimal(28, 2)))/10 FROM dfs.tmp.`week` where x2 in ('PRCP') group by id) as tbl2
on tbl1.id=tbl2.id;
```

We can select places. According to [rain intensity](<https://en.wikipedia.org/wiki/Rain#Intensity>) explanation and temperature we want, 

```sql
select * from dfs.tmp.`tmp` where prcp < 20 and tmin > 15 and tmax < 30;
```

And the result is:

```text
apache drill (hdfs)> select * from dfs.tmp.`tmp` where prcp < 20 and tmin > 15 and tmax < 30;
+-------------+-----------+-----------+-------+
|     id      |   tmin    |   tmax    | prcp  |
+-------------+-----------+-----------+-------+
| USC00450587 | 15.242857 | 27.400000 | 0.50  |
| USC00111497 | 15.871429 | 29.900000 | 15.20 |
| USC00049026 | 16.200000 | 16.200000 | 0.00  |
| USC00457941 | 15.057143 | 27.871429 | 0.50  |
| USC00356550 | 15.328571 | 28.657143 | 1.50  |
| USC00193276 | 17.614286 | 25.557143 | 1.80  |
| USC00146333 | 26.100000 | 26.100000 | 0.00  |
+-------------+-----------+-----------+-------+
7 rows selected (0.151 seconds)
```

Now we want to see the average temperature of these places in Augest. Sadly the option is not provided. There are 0 mathed rows for the following query.

```sql
SELECT * FROM (select columns[0] as id, columns[1] as day, columns[2] as x2, columns[3] as x3, columns[4] as x4, columns[5] as x5, columns[6] as x6, columns[7] as x7 from hdfs.`2017\.csv`) where id in ('USC00450587','USC00111497','USC00049026','USC00457941','USC00356550','USC00193276','USC00146333') and x2 in ('TAVG');
```

Anyway, let's check where are the places in.

```text
+-------------+-----------+-----------+-------+
|     id      |   lati    |   long    | name  |
+-------------+-----------+-----------+-------+
| USC00450587 | 48.7178   | -122.5114 | 4.6 WA BELLINGHAM 3 SSW                   HCN
| USC00111497 | 42.1397   | -87.7853  | 192.0 IL CHICAGO BOTANIC GARDEN
| USC00049026 | 40.7264 	| -122.7947 | 567.2 CA TRINITY RVR HATCHERY
| USC00457941 | 47.6811   | -117.6267 | 729.1 WA SPOKANE WFO
| USC00356550 | 45.6911   | -118.8522 | 460.2 OR PENDLETON WFO   
| USC00193276 | 42.7467   | -71.0425  | 9.1 MA GROVELAND
| USC00146333 | 39.1175   | -95.4100  | 291.7 KS PERRY LAKE   
+-------------+-----------+-----------+-------+
```

Then I decided to go to California.



####  Ex. 3 — Data visualisation

Fisrt, get continent information.

```sql
create TABLE dfs.tmp.`cc` AS select * from (select columns[0] as name, columns[1] as cont, columns[2] as fips from hdfs.`cc\.csv`);

select distinct cont from dfs.tmp.`cc`;
```

There are seven continents. For the temperature variation, 

```sql
create TABLE dfs.tmp.`cc-tmp` AS SELECT SUBSTR(id, 1, 2) as state, x2, x3 FROM (select columns[0] as id, columns[2] as x2, columns[3] as x3 from hdfs.`2017\.csv`) where x2 in ('TAVG');
```

Connect continent with country,

```sql
create TABLE dfs.tmp.`ctavg` AS 
select tbl1.*, tbl2.cont from dfs.tmp.`cc-tmp` as tbl1
join dfs.tmp.`cc` as tbl2
on tbl1.state=tbl2.fips;
```

Find average temperature of each continent,

```sql
select cont, AVG(CAST(x3 AS decimal(28, 2)))/10 as tavg from dfs.tmp.`ctavg` group by (cont);
```

Result in degree C:

```text
+------+------------+
| cont |    tavg    |
+------+------------+
| EU   | 5.473755   |
| OC   | 20.429206  |
| AF   | 23.662490  |
| SA   | 21.286002  |
| AS   | 18.071608  |
| NA   | 7.984370   |
| AN   | -14.699267 |
+------+------------+
```

Find variance of temperature,

```sql
select cont, VARIANCE(CAST(x3 AS decimal(28, 2))/10) as tvar from dfs.tmp.`ctavg` group by (cont);
```

And the result is

```text
+------+------------------+
| cont |       tvar       |
+------+------------------+
| AF   | 45.429743063836  |
| SA   | 51.951518208316  |
| OC   | 51.645486934857  |
| EU   | 163.720392870300 |
| AS   | 149.777752542372 |
| NA   | 138.449822315628 |
| AN   | 371.260814003003 |
+------+------------------+
```





### Reference

1. [File System Storage Plugin](https://drill.apache.org/docs/file-system-storage-plugin)
2. [Text files: csv, etc.](https://drill.apache.org/docs/text-files-csv-tsv-psv)
3. [Write drill query output to csv (or other format)](https://stackoverflow.com/questions/31014910/write-drill-query-output-to-csv-or-some-other-format)
4. [drill aggregate function](<https://drill.apache.org/docs/aggregate-and-aggregate-statistical/>)
5. [drill join](<https://drill.apache.org/docs/using-sql-functions-clauses-and-joins/>)
6. [sql range function](<https://sqlnotebook.com/range-func.html>)
7. [drill shell commands](<https://drill.apache.org/docs/configuring-the-drill-shell/#drill-shell-commands>)

