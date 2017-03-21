-- 1. Subjects which contian "Ioana"
SELECT subject, object
FROM dblp_complete
WHERE to_tsvector('testcfg', object) @@ to_tsquery('english', 'Ioana');


-- 2. Number of articles about XML in every year
-- p5:	http://purl.org/dc/elements/1.1/title
-- p25:	http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#year
-- p26:	http://www.w3.org/1999/02/22-rdf-syntax-ns#type
SELECT property_25 AS year, count(*)
FROM crosstabed_dblp_complete
WHERE to_tsvector('testcfg', property_5) @@ to_tsquery('english', 'XML')
AND property_26 = 'http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#Article'
GROUP BY property_25
ORDER BY property_25;

-- 3. Creator whose name starts with "Christo"
-- p1:	http://purl.org/dc/elements/1.1/creator
SELECT property_1, property_25
FROM crosstabed_dblp_complete
WHERE to_tsvector('testcfg', property_1) @@ to_tsquery('english', 'Christo:*'); 
