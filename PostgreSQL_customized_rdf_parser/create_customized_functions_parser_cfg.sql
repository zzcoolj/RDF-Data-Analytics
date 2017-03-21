-- The command to execute this script in psql:
-- \i /Users/zzcoolj/rdfanalytics/trunk/Codes/PostgreSQL/customized_parser_c_language/create_customized_functions_parser_cfg.sql

-- Create 4 functions for the customized parser
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

-- Create customized parser 
CREATE TEXT SEARCH PARSER testparser (
    START =    testprs_start,
    GETTOKEN = testprs_getlexeme,
    END =      testprs_end,
    LEXTYPES = testprs_lextype
);

-- Create customized dictionary (stop words)
CREATE TEXT SEARCH DICTIONARY url_simple (
    template = simple,
    stopwords = url
);

-- Set url_simple dictionary return NULL for every unmathced token (not too greedy).
ALTER TEXT SEARCH DICTIONARY url_simple ( accept = false );

-- Create customized dictionary (stop words)
CREATE TEXT SEARCH DICTIONARY english_simple (
    template = simple,
    stopwords = english
);

-- Set english_simple dictionary return NULL for every unmathced token (not too greedy).
ALTER TEXT SEARCH DICTIONARY english_simple ( accept = false );

-- Create customized snowball dictionary
CREATE TEXT SEARCH DICTIONARY url_snowball (
    template = snowball,
    language = english
);

-- Create customized configuration
CREATE TEXT SEARCH CONFIGURATION testcfg (PARSER = testparser);
ALTER TEXT SEARCH CONFIGURATION testcfg 
	ALTER MAPPING FOR word 
	WITH url_simple, english_simple, url_snowball;