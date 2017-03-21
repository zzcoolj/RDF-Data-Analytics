CREATE FUNCTION test() RETURNS void AS $$
BEGIN
    RAISE NOTICE 'hello world';
END
$$ LANGUAGE plpgsql;

-------------------------------------------

p1:		http://purl.org/dc/elements/1.1/creator
p25:	http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#year

SELECT author_year.articles_number, author_year.year, count(*) 
FROM

(SELECT property_1, property_25 AS year, count(*) AS articles_number
FROM crosstabed_dblp_complete
WHERE property_1 IS NOT NULL
AND property_25 IS NOT NULL
GROUP BY property_1, property_25) author_year

GROUP BY 1,2
ORDER BY 2,1;


-----------------------------------------------
FULL TEXT INDEXE TABLES
-----------------------------------------------
1.
CREATE INDEX property1_idx ON property1_table USING GIN (to_tsvector('english', value));

2.
-- Value column (type: Text; Unencoded) 
-- => add new column: textsearchable_index_col = stem(value) (type: tsvector; Unencoded)
-- => create index for the new column (encoded)

-- So we do not have to encoded textsearchable_index_col, cause we could consider index as the encoding for the column textsearchable_index_col.


-- Add new column
ALTER TABLE property1_table ADD COLUMN textsearchable_index_col tsvector;
UPDATE property1_table SET textsearchable_index_col = to_tsvector('english', value);

-- Index the added column
-- Attention: 	indexes are not mandatory for full text searching, 
-- 				but in cases where a column is searched on a regular basis, an index is usually desirable.
CREATE INDEX textsearch_idx ON property1_table USING GIN (textsearchable_index_col);

-- Search: e.g. Select the ten most recent documents that contain create and table in the textsearchable_index_col (indexed)
SELECT key
FROM property1_table
WHERE textsearchable_index_col @@ to_tsquery('create & table')
ORDER BY last_mod_date DESC
LIMIT 10;

3.
-- Parsing documents
SELECT to_tsvector('english', 'http://pages.saclay.inria.fr/ioana.manolescu/');

4.
-- Not work: sqlquery is a text value containing an SQL query which must return a single tsvector column
SELECT * FROM ts_stat('SELECT 1 FROM SELECT to_tsvector("SELECT object FROM dblp_forth LIMIT 100")')
ORDER BY nentry DESC, ndoc DESC, word
LIMIT 10;

5.
-- Parsers
SELECT alias, description, token FROM ts_debug('http://pages.saclay.inria.fr/ioana.manolescu/');
-- Could add conditions for alias type 
WHERE alias = 'url';

6.
pg_config --sharedir
-- $SHAREDIR: /Applications/Postgres.app/Contents/Versions/9.5/share/postgresql
cd /Applications/Postgres.app/Contents/Versions/9.5/share/postgresql/tsearch_data/

7. 
-- Dictionary testing
ts_lexize(dict regdictionary, token text) returns text[]

8. 
-- Set up the configuration
\dF
SET default_text_search_config = 'name_of_the_configuration.pg';
SHOW default_text_search_config;

9.
-- Testing
SELECT * FROM ts_parse('default', 'http://pages.saclay.inria.fr/ioana.manolescu/');

10.
-- Create stop words file url.stop in /Applications/Postgres.app/Contents/Versions/9.5/share/postgresql/tsearch_data/

-- Create simple dictionary url_stop
CREATE TEXT SEARCH DICTIONARY url_stop_dict (
    TEMPLATE = pg_catalog.simple,
    STOPWORDS = url
);

-- Create configuration public.rdf to use the dictionary url_stop
CREATE TEXT SEARCH CONFIGURATION public.rdf ( COPY = pg_catalog.english );
ALTER TEXT SEARCH CONFIGURATION public.rdf
   ALTER MAPPING FOR asciiword, asciihword, hword_asciipart, hword, hword_part, word 
   WITH url_stop_dict, english_stem;

-- Dictionary testing
SELECT ts_lexize('url_stop_dict', 'http://pages.saclay.inria.fr/ioana.manolescu/');

-- Configuration testing
SELECT * FROM ts_debug('public.rdf','http://pages.saclay.inria.fr/ioana.manolescu/');

-- Drop configuration
DROP TEXT SEARCH CONFIGURATION public.rdf;

-- Drop dictionary
DROP TEXT SEARCH DICTIONARY url_stop_dict;

11.
-- Some important path
pg_config --includedir-server
pg_config  --pkglibdir

12. 
-- Compile functions (Don't need to do this, Makefile will handle this.)
cc -c test_parser.c
cc -bundle -flat_namespace -undefined suppress -o test_parser.so test_parser.o

13.
-- Cope test_parser.so file to /Applications/Postgres.app/Contents/Versions/9.5/lib/postgresql (pg_config  --pkglibdir)

14. 
-- Create 4 functions
CREATE FUNCTION testprs_start(internal,int4)
RETURNS internal 
AS '$libdir/test_parser.so' 
LANGUAGE C;

CREATE FUNCTION testprs_getlexeme(internal,internal,internal)
RETURNS internal
AS '$libdir/test_parser.so'
LANGUAGE C;

CREATE FUNCTION testprs_end(internal)
RETURNS void
AS '$libdir/test_parser.so'
LANGUAGE C;

CREATE FUNCTION testprs_lextype(internal)
RETURNS internal
AS '$libdir/test_parser.so'
LANGUAGE C;

-- Create parser 
CREATE TEXT SEARCH PARSER testparser (
    START =    testprs_start,
    GETTOKEN = testprs_getlexeme,
    END =      testprs_end,
    LEXTYPES = testprs_lextype
);

CREATE TEXT SEARCH CONFIGURATION testcfg (PARSER = testparser);
ALTER TEXT SEARCH CONFIGURATION testcfg ADD MAPPING FOR word WITH simple;

15. 
-- Test the customized parser
SELECT * FROM ts_token_type('testparser');
SELECT * FROM ts_parse('testparser', 'http://pages.saclay.inria.fr/ioana.manolescu/');
