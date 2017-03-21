0.
CREATE TABLE dblp_complete(
subject text,
property text,
object text
 );

 COPY dblp_complete FROM '/Users/zzcoolj/rdfanalytics/trunk/Codes/exploratory-rdf-analytics/data/dblp_complete.dump';
 
1.
CREATE TABLE crosstabed_testTable AS
SELECT * FROM crosstab(
  'select subject, property, object from testTable order by 1',
  'SELECT DISTINCT property FROM testTable ORDER BY 1 LIMIT 3'
)
AS testTable(subject varchar(2), property_1 varchar(2), property_2 varchar(2), property_3 varchar(2));

--TODO Change category_sql
CREATE TABLE crosstabed_testTable AS
SELECT * FROM crosstab(
  'select subject, property, object from testTable order by 1',
  'p1,p2,p3'
)
AS testTable(subject varchar(2), property_1 varchar(2), property_2 varchar(2), property_3 varchar(2));

2.1 (Cube_Experment solution)
SELECT has_property_1, has_property_2, has_property_3, count FROM 
(
	select property_1 is not NULL AS has_property_1, 
		property_2 is not NULL AS has_property_2, 
		property_3 is not NULL AS has_property_3, 
		grouping(property_1 is not NULL,property_2 is not NULL,property_3 is not NULL),
		count(*) AS count
	from crosstabed_testtable 
	group by cube (1,2,3)
	ORDER BY 4 DESC
) AS a
where (has_property_1 = 't' or has_property_1 is NULL)
and (has_property_2 = 't' or has_property_2 is NULL)
and (has_property_3 = 't' or has_property_3 is NULL);

2.2 (Cube_Selected solution)
SELECT * FROM 
(
	select property_1 is not NULL AS has_property_1, 
		property_2 is not NULL AS has_property_2, 
		property_3 is not NULL AS has_property_3, 
		count(*) AS count
	from crosstabed_testtable 
	group by cube (1,2,3)
) AS a
where (has_property_1 = 't' or has_property_1 is NULL)
and (has_property_2 = 't' or has_property_2 is NULL)
and (has_property_3 = 't' or has_property_3 is NULL)
order by 4 DESC;

2.3 (Group by_Selected solution)
SELECT * FROM 
(
	select property_1 is not NULL AS has_property_1, 
		property_2 is not NULL AS has_property_2, 
		property_3 is not NULL AS has_property_3, 
		count(*) AS count
	from crosstabed_testtable 
	group by 1,2,3
) AS a

--Try part
SELECT DISTINCT subject
FROM table AS t1
WHERE property = property_1

INTERSECT

SELECT DISTINCT subject
FROM table AS t2
WHERE property = property_2

INTERSECT

...


SELECT subject
FROM table AS t1, table AS t2
WHERE t1.property = property_1 
AND t2.property = property_2
AND t1.subject = t2.subject

3.
CREATE TABLE crosstabed_dblp_forth_typeNull AS
SELECT * FROM crosstabed_dblp_forth WHERE property_26 IS NOT NULL;