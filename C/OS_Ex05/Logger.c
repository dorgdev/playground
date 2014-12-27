/*
 * Operating Systems - Exercise 5
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */

#include <fcntl.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>
#include <unistd.h>
#include "Logger.h"

/* Maximal size of a single read oepration from the pipeline */
#define READ_BUFF_SIZE 1024
/* Maximal size of a single read oepration from the stdin */
#define STDIN_READ_BUFF_SIZE 80
/* The size of queue to use in the BoundedBuffer */
#define MESSAGE_QUEUE_SIZE 10
/* Default pipeline's access mode */
#define FILE_ACCESS_RW 0666
/* Maximal size of the date printing */
#define MAX_DATE_SIZE 25
/* Maximal size of a log line */
#define MAX_LINE_SIZE 1024
/* The new-line delimiter between log lines */
#define NEW_LINE_DELIM '\n'
/* The internal delimiter of each log line */
#define LOG_LINE_DELIM '\t'
/* The commands which makes the programs to exit */
#define CMD_EXIT "exit\n"
/* The error printed in case of a malformed log line */
#define MSG_PARSE_ERROR "Cannot parse message!\n"
/* The date's format to use when logging messages */
#define LOG_DATE_FORMAT "%Y-%m-%d %H:%M:%S"
/* A value to indicate an illegal log level */
#define ILLEGAL_LOG_LEVEL -1

/* Defines the log level string representation per level's value */
char *LOG_LEVEL_TO_STRING[] = {"DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"};

/* ****************************** *
 * Internal functions definitions * 
 * ****************************** */

/*
 * A utility function that returns the log level from a given string.
 * If the log level is illegal, or if it fails parsing the given value,
 * returns -1 (ILLEGAL_LOG_LEVEL).
 */
static int read_log_level(const char *log_level_str) {
	int log_level;
	if (log_level_str == NULL) {
		return ILLEGAL_LOG_LEVEL;
	}
	log_level = atoi(log_level_str);
	if (log_level < 0 || log_level > 4 || (log_level == 0 && log_level_str[0] != '0')) {
		return ILLEGAL_LOG_LEVEL;
	}
	return log_level;
}

/*
 * A helper function that prints current time to a string.
 * Time is printed in the format required by exercise specification.
 * Returns the number of character printed into the given buffer.
 */
static int dateprintf(char *buff, int max_size, const char *format) {
	time_t timer;
	struct tm *tm_info;
	time(&timer);
	tm_info = localtime(&timer);
	return strftime(buff, max_size, format, tm_info);
}

/*
 * Handles a single message string.
 * Expected input should be in the format:
 *   <log level>:<message>
 * Illegal structured log messages wouldn't be enqueued into the buffer, and
 * an appropriate warning would be printed to the standard error.
 * Return 0 if the handling failed due to a finished buffer, or 1 otherwise.
 */
static int handle_message(const char* msg, int msg_len, BoundedBuffer *buff) {
	LogMessage *log_msg;
	enum LOG_LEVEL level;
	/* There should be at least 3 characters according to the format, and 
	 * Second character should be a colon */
	if (msg_len < 3 || msg[1] != ':') {
		fprintf(stderr, MSG_PARSE_ERROR);
		return 1;
	}
	level = read_log_level(msg);
	/* Log level sanity check */
	if (level == ILLEGAL_LOG_LEVEL) {
		fprintf(stderr, MSG_PARSE_ERROR);
		return 1;
	}
	/* Create and fill the log message */
	log_msg = (LogMessage*)(malloc(sizeof(LogMessage)));
	log_msg->level = level;
	strncpy(log_msg->message, msg + 2, LOG_MESSAGE_MAX_LEN);
	log_msg->message[msg_len - 1] = '\0';
	/* Try to enqueue the log message */
	if (!bounded_buffer_enqueue(buff, log_msg)) {
		free(log_msg);
		return 0;
	}
	return 1;
}

/*
 * Handles a single line read from the FIFO.
 * Returns 0 if the buffer was finished in the middle of the operation, or 1 otherwise.
 */
static int handle_line(char *line, BoundedBuffer *buff) {
	char *next_msg_start;
	char *curr_msg_start = line;
	int msg_len;
	/* Go over all the messages seprated by a new-line delimeter */
	while ((next_msg_start = strchr(curr_msg_start, NEW_LINE_DELIM)) != NULL) {
		/* Replace the new-line with null-terminating char */
		next_msg_start[0] = '\0';
		msg_len = next_msg_start - curr_msg_start;
		/* Handle the read message */
		if (!handle_message(curr_msg_start, msg_len, buff)) {
			return 0;
		}
		/* Set the next message pointer */
		curr_msg_start = next_msg_start + 1;
	}
	/* Check and handle trailing message (not new-line delimited) */
	if (curr_msg_start[0] != '\0') {
		msg_len = strlen(curr_msg_start);
		return handle_message(curr_msg_start, msg_len, buff);
	}
	return 1;
}

/* 
 * Fills the given buffer with a log line in the following format:
 *    <YYYY-MM-DD hh24:mm:ss> <log level> <message>
 */
static void fill_log_line(char *line, int line_len, LogMessage *message) {
	int written;
	/* Writes the date */
	written = dateprintf(line, MAX_DATE_SIZE, LOG_DATE_FORMAT);
	line[written++] = LOG_LINE_DELIM;
	/* Writes the log level (string representation) */
	strcpy(line + written, LOG_LEVEL_TO_STRING[message->level]);
	written += strlen(line + written);
	line[written++] = LOG_LINE_DELIM;
	/* Copy the message */
	strncpy(line + written, message->message, line_len - written);
	line[line_len - 1] = '\0';
}

/* ****************************** *
 * External functions definitions * 
 * ****************************** */

void log_to_screen(LoggerData *logger, LogMessage *message) {
	char log_line[MAX_LINE_SIZE];
	fill_log_line(log_line, MAX_LINE_SIZE, message);
	printf("%s\n", log_line);
}

void log_to_file(LoggerData *logger, LogMessage *message) {
	char log_line[MAX_LINE_SIZE];
	/* Make sure the log file was initialized */
	if (logger->log_file == NULL) {
		return;
	}
	fill_log_line(log_line, MAX_LINE_SIZE, message);
	fprintf(logger->log_file, "%s\n", log_line);
}

void *run_listener(void *param) {
	ListenerData *data;
	BoundedBuffer *buff;
	int fd, num, quit = 0;
	char message[READ_BUFF_SIZE];

	data = (ListenerData*)param;
	buff = data->buff;

	mknod(data->pipe_file, S_IFIFO | FILE_ACCESS_RW, 0);

	while (!quit) {
		// Wait for a connection on the pipe
		fd = open(data->pipe_file, O_RDONLY);
		if (fd <= 0) {
			// Handle error
			fprintf(stderr, "Failed to open the logger's pipeline!\n");
			exit(1);
		}
		/* In case the buffer was finished by now */
		quit = bounded_buffer_is_finished(buff);
		/* Read data from pipe */
		while (!quit && (num = read(fd, message, READ_BUFF_SIZE)) > 0) {
			/* make sure the read buffer is null-terminated */
			message[num - 1] = '\0';
			if (!handle_line(message, buff)) {
				/* The buffer is probably done */
				quit = 1;
				break;
			}
		}
		close(fd);
	}

	remove(data->pipe_file);
	return NULL;
}

void *run_logger(void *param) {
	int i;
	LogMessage *message;
	LoggerData *logger_data = (LoggerData*)param;
	while (1) {
		/* Read the next message. Exit if NULL */
		message = bounded_buffer_dequeue(logger_data->buff);
		if (message == NULL) {
			break;
		}
		/* Check the message's log level */
		if (message->level >= logger_data->level) {
			/* Invoke the logging functions one by one */
			for (i = 0; i < logger_data->num_loggers; ++i) {
				logger_data->loggers[i](logger_data, message);
			}
		}
		free(message);
	}
	return NULL;
}

int main(int argc, char *argv[]) {
	char input_line[STDIN_READ_BUFF_SIZE];
	pthread_t tid_logger, tid_listener;
	ListenerData listener_data;
	LoggerData logger_data;
	BoundedBuffer buffer;
	enum LOG_LEVEL log_level;
	FILE *log_file = NULL;
	/* Input's sanity checks */
	if (argc < 3 || argc > 4) {
		fprintf(stderr, "Usage: %s pipe_file_name min_log_level [log_file_path]\n", argv[0]);
		return 1;
	}
	log_level = read_log_level(argv[2]);
	if (log_level == ILLEGAL_LOG_LEVEL) {
		fprintf(stderr, "Illegal log level given: %s\n", argv[2]);
		return 1;
	}
	if (argc == 4) {
		if ((log_file = fopen(argv[3], "w")) == NULL) {
			fprintf(stderr, "Failed to open the output file: %s\n", argv[3]);
			return 1;
		}
	}
	/* Initialize the buffer and log data */
	bounded_buffer_init(&buffer, MESSAGE_QUEUE_SIZE);

	logger_data.log_file = log_file;
	logger_data.loggers[0] = &log_to_screen;
	logger_data.num_loggers = 1;
	logger_data.buff = &buffer;
	logger_data.level = log_level;
	if (log_file != NULL) {
		logger_data.loggers[logger_data.num_loggers++] = &log_to_file;
	}

	listener_data.buff = &buffer;
	listener_data.pipe_file = argv[1];
	/* Start the logger thread and the listener thread */
	pthread_create(&tid_listener, NULL, run_listener, (void*)(&listener_data));
	pthread_create(&tid_logger, NULL, run_logger, (void*)(&logger_data));

	/* Read the input until the exit command arrives */
	while ((fgets(input_line, STDIN_READ_BUFF_SIZE, stdin)) != NULL) {
		if (strcmp(input_line, CMD_EXIT) == 0) {
			break;
		}
	}
	/* Close the buffer and wait for the thread to finish */
	bounded_buffer_finish(&buffer);
	pthread_join(tid_listener, NULL);
	pthread_join(tid_logger, NULL);
	/* Destroy the allocated resources */
	bounded_buffer_destroy(&buffer);
	if (log_file != NULL) {
		fclose(log_file);
	}
	/* Mark a successful run */
	return 0;
}
