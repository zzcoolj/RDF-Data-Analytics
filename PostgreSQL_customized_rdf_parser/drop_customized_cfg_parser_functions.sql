-- The command to execute this script in psql:
-- \i /Users/zzcoolj/rdfanalytics/trunk/Codes/PostgreSQL/customized_parser_c_language/drop_customized_cfg_parser_functions.sql

-- Delete customized configuration
DROP TEXT SEARCH CONFIGURATION testcfg;

-- Delete customized dictionaries
DROP TEXT SEARCH DICTIONARY url_simple;
DROP TEXT SEARCH DICTIONARY english_simple;
DROP TEXT SEARCH DICTIONARY url_snowball;

-- Delete customized parser
DROP TEXT SEARCH PARSER testparser;

-- Delete 4 functions of the customized parser
DROP FUNCTION testprs_start(internal,int4);
DROP FUNCTION testprs_getlexeme(internal,internal,internal);
DROP FUNCTION testprs_end(internal);
DROP FUNCTION testprs_lextype(internal);