cmake -DCMAKE_C_COMPILER=/usr/bin/gcc -DCMAKE_CXX_COMPILER=/usr/bin/g++
CFLAGS  = -O3 -I. -I/Applications/Postgres.app/Contents/Versions/9.5/include/postgresql/server -Wall

LIBS    =

DEPENDS = test_parser.o

test_parser.so: ${DEPENDS}
	$(CC) -bundle -flat_namespace -undefined suppress -o $@ ${DEPENDS}

test_parser.o: test_parser.c
	$(CC) $(CFLAGS) -c $*.c

clean:
	rm -f core core.* *.o *.a *~ *% *.so.*

install:
	cp -f test_parser.so /Applications/Postgres.app/Contents/Versions/9.5/lib/postgresql/