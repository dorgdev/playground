all: logger

clean:
	rm logger *.o

logger: BoundedBuffer.o Logger.o
	gcc -Wall BoundedBuffer.o Logger.o -lpthread -o logger

BoundedBuffer.o:
	gcc -Wall -c BoundedBuffer.c

Logger.o:
	gcc -Wall -c Logger.c
