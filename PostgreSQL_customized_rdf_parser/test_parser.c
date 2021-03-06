/*
 * testparser v0.02
 */


#include "postgres.h"
#include "utils/builtins.h"

#include "fmgr.h"

#ifdef PG_MODULE_MAGIC
 PG_MODULE_MAGIC;
#endif
/*
 * types
 */

/* self-defined type */
 typedef struct {
  char *  buffer; /* text to parse */
  int     len;    /* length of the text in buffer */
  int     pos;    /* position of the parser */
 } ParserState;

/* copy-paste from wparser.h of tsearch2 */
 typedef struct {
 	int     lexid;
 	char    *alias;
 	char    *descr;
 } LexDescr;

/*
 * prototypes
 */
 PG_FUNCTION_INFO_V1(testprs_start);
 Datum testprs_start(PG_FUNCTION_ARGS);

 PG_FUNCTION_INFO_V1(testprs_getlexeme);
 Datum testprs_getlexeme(PG_FUNCTION_ARGS);

 PG_FUNCTION_INFO_V1(testprs_end);
 Datum testprs_end(PG_FUNCTION_ARGS);

 PG_FUNCTION_INFO_V1(testprs_lextype);
 Datum testprs_lextype(PG_FUNCTION_ARGS);

/*
 * functions
 */
 Datum testprs_start(PG_FUNCTION_ARGS)
 {
 	ParserState *pst = (ParserState *) palloc(sizeof(ParserState));
 	pst->buffer = (char *) PG_GETARG_POINTER(0);
 	pst->len = PG_GETARG_INT32(1);
 	pst->pos = 0;

 	PG_RETURN_POINTER(pst);
 }

 Datum testprs_getlexeme(PG_FUNCTION_ARGS)
 {
 	ParserState *pst   = (ParserState *) PG_GETARG_POINTER(0);
 	char        **t    = (char **) PG_GETARG_POINTER(1);
 	int         *tlen  = (int *) PG_GETARG_POINTER(2);
 	int         type;

 	*tlen = pst->pos;
 	*t = pst->buffer +  pst->pos;

 	// if ((pst->buffer)[pst->pos] == ' ') {
  //   /* blank type */
 	// 	type = 12;
  //   /* go to the next non-white-space character */
 	// 	while (((pst->buffer)[pst->pos] == ' ') && (pst->pos < pst->len)) {
 	// 		(pst->pos)++;
 	// 	}
 	// } else {
  //   /* word type */
 	// 	type = 3;
  //   /* go to the next white-space character */
 	// 	while (((pst->buffer)[pst->pos] != ' ') && (pst->pos < pst->len)) {
 	// 		(pst->pos)++;
 	// 	}
 	// }

 	if ((pst->buffer)[pst->pos] == '/') {
    /* slash type */
 		type = 12;
    /* go to the next non-slash character */
 		while (((pst->buffer)[pst->pos] == '/') && (pst->pos < pst->len)) {
 			(pst->pos)++;
 		}
 	} else if ((pst->buffer)[pst->pos] == '.') {
 		type = 12;
 		while (((pst->buffer)[pst->pos] == '.') && (pst->pos < pst->len)) {
 			(pst->pos)++;
 		} 
 	} else if ((pst->buffer)[pst->pos] == ':') {
 		type = 12;
 		while (((pst->buffer)[pst->pos] == ':') && (pst->pos < pst->len)) {
 			(pst->pos)++;
 		} 
 	} else if ((pst->buffer)[pst->pos] == ' ') {
    type = 12;
    while (((pst->buffer)[pst->pos] == ' ') && (pst->pos < pst->len)) {
      (pst->pos)++;
    } 
  } else {
    /* word type */
 		type = 3;
    /* go to the next white-space character */
 		while (((pst->buffer)[pst->pos] != '/') 
 			&& ((pst->buffer)[pst->pos] != '.')
 			&& ((pst->buffer)[pst->pos] != ':')
      && ((pst->buffer)[pst->pos] != ' ')
 			&& (pst->pos < pst->len)) {
 			(pst->pos)++;
 	}
 }

 *tlen = pst->pos - *tlen;
  /* we are finished if (*tlen == 0) */
 if (*tlen == 0) type=0;

 PG_RETURN_INT32(type);
}

Datum testprs_end(PG_FUNCTION_ARGS)
{
	ParserState *pst = (ParserState *) PG_GETARG_POINTER(0);
	pfree(pst);
	PG_RETURN_VOID();
}

Datum testprs_lextype(PG_FUNCTION_ARGS)
{
  /*
    Remarks:
    - we have to return the blanks for headline reason
    - we use the same lexids like Teodor in the default
      word parser; in this way we can reuse the headline
      function of the default word parser.
  */
      LexDescr *descr = (LexDescr *) palloc(sizeof(LexDescr) * (2+1));

  /* there are only two types in this parser */
      descr[0].lexid = 3;
      descr[0].alias = pstrdup("word");
      descr[0].descr = pstrdup("Word");

      descr[1].lexid = 12;
      descr[1].alias = pstrdup("blank");
      descr[1].descr = pstrdup("Space symbols, slash, dot and colon");
      
      descr[2].lexid = 0;

      PG_RETURN_POINTER(descr);
  }