/*
 * Operating Systems - Exercise 5
 * Student's name: Dor Gross
 * Student's ID:   039344999
 */

#include <stdlib.h>
#include "BoundedBuffer.h"

void bounded_buffer_init(BoundedBuffer *buff, int capacity) {
	/* Ignore NULL value */
	if (buff == NULL) {
		return;
	}
	/* Initialize the values according to the given capacity */
	buff->buffer = (LogMessage**)(malloc(capacity * sizeof(LogMessage)));
	buff->size = 0;
	buff->capacity = capacity;
	buff->head = 0;
	buff->tail = 0;
	buff->finished = 0;
	pthread_mutex_init(&buff->mutex, NULL);
	pthread_cond_init(&buff->cv_empty, NULL);
	pthread_cond_init(&buff->cv_full, NULL);
}

int bounded_buffer_enqueue(BoundedBuffer *buff, LogMessage *message) {
	int succeeded = 0;
	/* Ignore NULL values - treat as finished buffer */
	if (buff == NULL || message == NULL) {
		return succeeded;
	}
	/* The operation locks the given buffer */
	pthread_mutex_lock(&buff->mutex);
	
	/* Wait until we can enqueue, or until the buffer is finished */
	while (!buff->finished && buff->size == buff->capacity) {
		pthread_cond_wait(&buff->cv_full, &buff->mutex);
	}
	/* If the buffer is not finished, then we can enqueue */
	if (!buff->finished) {
		buff->buffer[buff->tail] = message;
		buff->size++;
		buff->tail = (buff->tail + 1) % buff->capacity;
		succeeded = 1;
		/* Let other threads know the buffer is not empty */
		pthread_cond_signal(&buff->cv_empty);
	}
	/* Release the lock and return */
	pthread_mutex_unlock(&buff->mutex);
	return succeeded;
}

LogMessage *bounded_buffer_dequeue(BoundedBuffer *buff) {
	LogMessage *message = NULL;
	/* Ignore NULL value - treat as finished buffer */
	if (buff == NULL) {
		return NULL;
	}
	/* The operation locks the given buffer */
	pthread_mutex_lock(&buff->mutex);
	
	/* Wait until we can dequeue, or until the buffer is finished */
	while (!buff->finished && buff->size == 0) {
		pthread_cond_wait(&buff->cv_empty, &buff->mutex);
	}
	/* If the buffer is not finished, then we can dequeue */
	if (!buff->finished) {
		message = buff->buffer[buff->head];
		buff->size--;
		buff->head = (buff->head + 1) % buff->capacity;
		/* Let other threads know the buffer is not full */
		pthread_cond_signal(&buff->cv_full);
	}
	/* Release the lock and return */
	pthread_mutex_unlock(&buff->mutex);
	return message;
}

void bounded_buffer_finish(BoundedBuffer *buff) {
	/* Ignore NULL value - nothing to do */
	if (buff == NULL) {
		return;
	}
	/* The operation locks the given buffer */
	pthread_mutex_lock(&buff->mutex);
	
	buff->finished = 1;
	
	/* Let other threads know the buffer is finished */
	pthread_cond_signal(&buff->cv_empty);
	pthread_cond_signal(&buff->cv_full);
	
	/* Release the lock and return */
	pthread_mutex_unlock(&buff->mutex);
}

int bounded_buffer_is_finished(BoundedBuffer *buff) {
	int is_finished;
	if (buff == NULL) {
		return 1;
	}
	pthread_mutex_lock(&buff->mutex);
	is_finished = buff->finished;
	pthread_mutex_unlock(&buff->mutex);
	return is_finished;
}

void bounded_buffer_destroy(BoundedBuffer *buff) {
	/* Ignore NULL value */
	if (buff == NULL) {
		return;
	}
	free(buff->buffer);
	pthread_cond_destroy(&buff->cv_full);
	pthread_cond_destroy(&buff->cv_empty);
	pthread_mutex_destroy(&buff->mutex);
}
