/*
 * Operating Systems - Exercise 5
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */

/*
 * LogMessage.h
 *
 *  Created on: May 13, 2012
 *      Author: yotamhc
 */

#ifndef LOGMESSAGE_H_
#define LOGMESSAGE_H_

#define NUM_LEVELS 5
#define LOG_MESSAGE_MAX_LEN 255

enum LOG_LEVEL {
	DEBUG,
	INFO,
	WARNING,
	ERROR,
	CRITICAL
};

typedef struct {
	char message[LOG_MESSAGE_MAX_LEN];
	enum LOG_LEVEL level;
} LogMessage;

#endif /* LOGMESSAGE_H_ */
