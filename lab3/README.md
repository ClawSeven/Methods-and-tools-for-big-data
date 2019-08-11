# Lab 3

### Apache Spark vs. Apache Drill

Apache Drill and Apache Spark are both distributed computation engines with support for Hadoop.



### Drill

To activate drill,

```bash
$ drill-embedded
```

to quit, 

```bash
$ apache drill> !quit
```

#### Add file plugin

```json
{
    "type" : "file",
    "enabled" : true,
    "connection" : "hdfs://localhost:9000/",
    "workspaces" : {
      "root" : {
        "location" : "/Users/kelly/drill",
        "writable" : true,
        "defaultInputFormat" : null
      }
    },
    "formats" : {
      "json" : {
        "type" : "json"
      }
    }
  }
```



Drill can also be used from port `http://localhost:8047` after activated (use this URL when running Drill in embedded mode `./drill-embedded`).

```sql
SELECT `employee_id`, `full_name`, `store_id` FROM cp.`employee.json` ORDER BY `store_id` DESC;

-- Select lowest grade or highest average score (of a student)
SELECT MAX(`position_id`) FROM cp.`employee.json`;
-- Calculate median
SELECT SUM(`store_id`) FROM cp.`employee.json`;
```

```sql
-- get median
select ol.name, ol.sid from
	(select `store_id` as sid, `full_name` as name, (select count(*)/2 from cp.`employee.json`) as ct, row_number() over (order by `store_id`)  as num from cp.`employee.json`) as ol 
	where (ol.num>ol.ct-1 and ol.num<ol.ct+1);
```

##### Connect to python

```python
from pydrill.client import PyDrill
drill = PyDrill(host='localhost', port=8047)

# check if activated
drill.is_active()
```

#### read csv

```sql
-- read csv as table
SELECT COLUMNS[1] AS user_id,
COLUMNS[0] AS username, COLUMNS[2] AS grade
FROM (select * from hdfs.`gradebook\.csv`);

SELECT username, MAX(grade) FROM (SELECT COLUMNS[1] AS user_id,
COLUMNS[0] AS username, COLUMNS[2] AS grade
FROM (select * from hdfs.`gradebook\.csv`)) GROUP BY username;

from hdfs.`gradebook\.csv`;
```

For csv file, it can also be set to [skip or not skip first line](<https://drill.apache.org/docs/text-files-csv-tsv-psv/>) in the config file.



##### lowest grade

```sql
SELECT username, MIN(grade) FROM (SELECT COLUMNS[1] AS user_id,
COLUMNS[0] AS username, COLUMNS[2] AS grade
FROM (select * from hdfs.`gradebook\.csv`)) GROUP BY username;
```

##### highest average

```sql
SELECT MAX(avg_grade) FROM (
  SELECT avg(grade) as avg_grade FROM (
    SELECT COLUMNS[1] AS user_id, CAST(COLUMNS[2] AS INT) AS grade FROM (
      select * from hdfs.`gradebook\.csv`)
 	 	) GROUP BY user_id
);
```

##### get median

```sql
SELECT ol.user_name, ol.grade FROM (
  select user_name, grade, (select count(*)/2 from hdfs.`gradebook\.csv`) as ct, row_number() over (order by grade) AS num FROM (
  SELECT COLUMNS[0] AS user_name, CAST(COLUMNS[2] AS INT) AS grade FROM (
      select * from hdfs.`gradebook\.csv`)
  )
) as ol WHERE ( ol.num>ol.ct-1 and ol.num<ol.ct+1);
```



### Spark

RDD: Resilient Distributed Dataset

Spark shell is only intended to be use for testing and perhaps development of small applications and is only an interactive shell and should not be use to run production spark applications.

For production application deployment use spark-submit. It will allow you to run applications in yarn-cluster mode.



#### pyspark

```sh
# upload on hdfs
sbin/hdfs dfs -put smallbook.csv hdfs://localhost:9000/
```

In pyspark, under local mode,

```python
#!pyspark
from pyspark.context import SparkContext
sc = SparkContext.getOrCreate(SparkConf().setMaster("local[*]"))	# create an instance

# create an RDD from file
content = sc.textFile("smallbook.csv")

# .collect() serialize the RDD into a <list>
content.collect()

def f(x):
    a = x.split(',')
    return (a[1], int(a[2]))

mp = content.map(f)
test = mp.reduceByKey(lambda x, y: y if y>x else x)
test.collect()
# result:
# >>>> [('6504049265', 95), ('5077456473', 97), ('3899949092', 94)]
```



#### How to use flatMap

flatMap returns a serialized result.

```python
fm = content.flatMap(f)
fm.collect()			# display content of flatMap result
```





#### Reference

1. [Text Files: CSV, TSV, PSV](<https://drill.apache.org/docs/text-files-csv-tsv-psv/>)
2. [File System Storage Plugin](https://drill.apache.org/docs/file-system-storage-plugin)
3. [drill use](<https://drill.apache.org/docs/use/>)
4. [create table as CTAS](<https://drill.apache.org/docs/create-table-as-ctas/>), not successful yet
5. [Apache Drill not able to set default workspace](https://stackoverflow.com/questions/40629662/apache-drill-not-able-to-set-default-workspace)
6. [Drill workspaces](<https://drill.apache.org/docs/workspaces/#no-workspaces-for-hive-and-hbase>)
7. [pyspark RDD](<https://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD>)
8. [pyspark: textFile() is an instance method](https://stackoverflow.com/questions/47665491/pyspark-throws-typeerror-textfile-missing-1-required-positional-argument-na)
9. [Difference between map and flatMap transformations in Spark (pySpark)](<https://www.linkedin.com/pulse/difference-between-map-flatmap-transformations-spark-pyspark-pandey>)



