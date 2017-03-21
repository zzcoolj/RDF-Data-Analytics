/* 
http://no0p.github.io/postgresql/2015/01/24/postgresql-regexp-stopwords.html
Custom Dictionary
A github project
*/

-- http://shisaa.jp/postset/postgresql-full-text-search-part-2.html

-- 0 Basic Knowledge
-- 0.1 A dictionary template is the skeleton (hence template) of a dictionary. It defines the actual C functions that will do the heavy lifting. 
-- A dictionary is an instantiation of that template - providing it with data to work with.
-- 0.2 tsearch_data directory path: 
/Applications/Postgres.app/Contents/Versions/9.5/share/postgresql/tsearch_data/
-- 0.3 Show all the dictionaries
SELECT dictname FROM pg_catalog.pg_ts_dict;


-- 1. Setting up a dictionary
-- 1.1 Write a stop word file in tsearch_data directory
-- 1.2 Setup our own simple dictionary based on the simple dictionary template
CREATE TEXT SEARCH DICTIONARY shisaa_simple (
    template = simple,
    stopwords = shisaa_stop
);


-- 2. Setting up a configuration
-- 2.1 Create an empty configuration
CREATE TEXT SEARCH CONFIGURATION shisaa (copy/parser='default');
-- With parser you define which parser to use and it will create an empty configuration. PostgreSQL has only one parser by default which is named default.
-- If you choose copy then you will have to provide an existing configuration name (like english) from which you would like to make a copy.
-- 2.2 Mapping dictionary to token categories
ALTER TEXT SEARCH CONFIGURATION shisaa
    ALTER MAPPING FOR asciiword, asciihword, hword_asciipart,
                  word, hword, hword_part
    WITH shisaa_simple;
-- 2.3 Test configuration (optional)
\dF+ shisaa


-- 3. Stemming (Optional)
-- 3.1 Dictionary template snowball is designed for stemming.
CREATE TEXT SEARCH DICTIONARY shisaa_snowball (
    template = snowball,
    language = english
);
-- 3.2 When chaining dictionaries together to put this dictionary at the end of the chain.
ALTER TEXT SEARCH CONFIGURATION shisaa
    ALTER MAPPING FOR asciiword, asciihword, hword_asciipart,
                  word, hword, hword_part
    WITH shisaa_simple, shisaa_snowball; 
-- 3.3 Set simple dictionary return NULL for every unmathced token (not too greedy).
ALTER TEXT SEARCH DICTIONARY shisaa_simple ( accept = false );


-- 4. Synonyms (Optional)
-- 4.1 Create a synonym file.
-- 4.2 Setup the dictionary.
CREATE TEXT SEARCH DICTIONARY shisaa_synonym (
    template = synonym,
    synonyms = shisaa_syn
);
-- 4.3 Mapping
ALTER TEXT SEARCH CONFIGURATION shisaa
    ALTER MAPPING FOR asciiword, asciihword, hword_asciipart,
                  word, hword, hword_part
    WITH shisaa_simple, shisaa_synonym, shisaa_snowball;


-- 5. Thesaurus dictionary (Optional)
-- 5.1 Create a .ths file
-- 5.2 Set the subdictionary
-- 5.3 Setup the dictionary
CREATE TEXT SEARCH DICTIONARY shisaa_thesaurus (
    TEMPLATE = thesaurus,
    DICTFILE = shisaa_thesaurus,
    DICTIONARY = shisaa_snowball
);
-- 5.4 Mapping
ALTER TEXT SEARCH CONFIGURATION shisaa
    ALTER MAPPING FOR asciiword, asciihword, hword_asciipart,
                  word, hword, hword_part
    WITH shisaa_simple, shisaa_thesaurus, shisaa_snowball;


-- 6. Ispell dictionary (Optional)


-- 7. Test the customized configuration
SELECT * FROM ts_debug('testcfg','http://pages.saclay.inria.fr/ioana.manolescu/');
SELECT to_tsvector('testcfg','http://pages.saclay.inria.fr/ioana.manolescu/');