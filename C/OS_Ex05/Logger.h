/*
 * Operating Systems - Exercise 5
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#include <stdio.h>
#include "BoundedBuffer.h"
#include "LogMessage.h"

#define MAX_LOGGERS 10

/* Forward declaration of the struct for self-usage */
struct logger_data;

/* Struct for Logger thread parameters */
typedef struct logger_data {
	/* The bounded buffer */
	BoundedBuffer *buff;
	/* Log level of the system */
	enum LOG_LEVEL level;
	/* File pointer to write file logging into */
	FILE *log_file;
	/* Array of logging functions to use */
	void(*loggers[MAX_LOGGERS])(struct logger_data*, LogMessage*);
	/* Number of functions to use from the loggers array */
	int num_loggers;
} LoggerData;

/* Struct for Listener thread parameters */
typedef struct {
	/* The bounded buffer */
	BoundedBuffer *buff;
	/* The pipe file name */
	char *pipe_file;
} ListenerData;

/*
 * Listener thread starting point.
 * Creates a named pipe (the name should be supplied by the main function) and waits for
 * a connection on it. Once a connection has been received, reads the data from it and
 * parses the message out of the data buffer. For each message, a new LogMessage instance should
 * be allocated, then enqueued to the message queue.
 * If the enqueue operation fails (returns 0), it means that the application is trying to exit.
 * Therefore, the Listener thread should stop. Before stopping, it should remove the pipe file
 * and free the memory of the message it failed to enqueue.
 */
void *run_listener(void *param);

/*
 * Logger thread starting point.
 * The logger reads a message from the message queue, one by one, and logs them (** only
 * if their level is high enough **), using all given logging functions.
 * After logging, the logger should free the memory of the dequeued message (it
 * was allocated by the Listener thread).
 * If the dequeue operation fails (returns NULL), it means that the application is trying
 * to exit and therefore the thread should simply terminate.
 */
void *run_logger(void *param);

/*
 * Screen logging function.
 * Outputs log message to standard output.
 */
void log_to_screen(LoggerData *logger, LogMessage *message);

/*
 * File logging function.
 * Outputs log message to the log file (should be opened in advance).
 */
void log_to_file(LoggerData *logger, LogMessage *message);

/*
 * Main function.
 * Reads command line arguments in the format:
 * 		./logger pipe_name log_level [log_file]
 * Where pipe_name is the name of FIFO pipe that the Listener should create,
 * log_level is the system's level of logging, and log_file is an optional parameter that
 * specifies the output file to log to (if not specified, no file logging is done).
 * This function should create the message queue and prepare the parameters to the Listener and
 * Logger threads. Then, it should create these threads.
 * After threads are created, this function should control them as follows:
 * it should read input from user, and if the input line is "exit" (or "exit\n"), it should
 * set the message queue as "finished". This should make the threads terminate (possibly only
 * when the next connection is received).
 * At the end, the function should join the threads and exit.
 */
int main(int argc, char *argv[]);

#endif /* LOGGER_H_ */
