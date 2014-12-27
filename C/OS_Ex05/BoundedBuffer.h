/*
 * Operating Systems - Exercise 5
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */

#ifndef BOUNDEDBUFFER_H_
#define BOUNDEDBUFFER_H_

#include <pthread.h>
#include "LogMessage.h"

typedef struct {
	LogMessage **buffer;
	int size;
	int capacity;
	int head;
	int tail;
	pthread_mutex_t mutex;
	pthread_cond_t cv_empty;
	pthread_cond_t cv_full;
	int finished;
} BoundedBuffer;

/*
 * Initializes the buffer with the specified capacity.
 * This function should allocate the buffer, initialize its properties
 * and also initialize its mutex and condition variables.
 * It should set its finished flag to 0.
 */
void bounded_buffer_init(BoundedBuffer *buff, int capacity);

/*
 * Enqueue a log message (a pointer to a LogMessage struct) to the buffer.
 * This function should add an element to the buffer. If the buffer is full,
 * it should wait until it is not full, or until it has finished.
 * If the buffer has finished (either after waiting or even before), it should
 * simply return 0.
 * If the enqueue operation was successful, it should return 1. In this case it
 * should also signal that the buffer is not empty.
 * This function should be synchronized on the buffer's mutex!
 */
int bounded_buffer_enqueue(BoundedBuffer *buff, LogMessage *message);

/*
 * Dequeues a log message (a pointer to a LogMessage struct) from the buffer.
 * This function should remove the head element of the buffer and return it.
 * If the buffer is empty, it should wait until it is not empty, or until it has finished.
 * If the buffer has finished (either after waiting or even before), it should
 * simply return NULL.
 * If the dequeue operation was successful, it should signal that the buffer is not full.
 * This function should be synchronized on the buffer's mutex!
 */
LogMessage *bounded_buffer_dequeue(BoundedBuffer *buff);

/*
 * Sets the buffer as finished.
 * This function sets the finished flag to 1 and then wakes up all threads that are
 * waiting on the condition variables of this buffer.
 * This function should be synchronized on the buffer's mutex!
 */
void bounded_buffer_finish(BoundedBuffer *buff);

/*
 * Returns 1 if the given buffer was finished, or NULL, and 0 otherwise.
 * Performs the check under a mutex lock (to prevent race conditions)
 */
int bounded_buffer_is_finished(BoundedBuffer *buff);

/*
 * Frees the buffer memory and destroys mutex and condition variables.
 */
void bounded_buffer_destroy(BoundedBuffer *buff);

#endif /* BOUNDEDBUFFER_H_ */
