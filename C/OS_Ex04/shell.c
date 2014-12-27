/* Operating Systems - Exercise 4
 * Students: Dor Gross, 039344999 & Ben Reuveni, 300095296
 * 
 * This program imitates a shell, with some functionality:
 *  1. Runs external scripts and programs.
 *  2. Changes the work directory (using 'cd')
 *  3. Supports the usage of pipes between commands.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

/* Max input line's length */
#define MAX_LINE_LEN 256
/* Max number of commands in a single line */
#define MAX_COMMANDS_COUNT 16
/* Args' count is calculated by ((MAX_LINE_LEN / 2) + MAX_COMMANDS_COUNT) */
#define MAX_ARGS_COUNT 144
/* The command used to exit the program */
#define EXIT_COMMAND "exit"
/* The command used for changing the current work directory */
#define CD_COMMAND "cd"
/* The delimeter between the shell's arguments */
#define ARGS_DELIMETERS " "
/* Commands' delimeter for foreground execution */
#define FG_STR "&&"
/* Background command's argument */
#define BG_STR "&"
/* Commands' delimeter for pipelined commands */
#define PIPE_STR "|"
/* The message to be printed for an empty or missing command */
#define EMPTY_CMD_ERROR "Invalid null command.\n"
/* An integer representing 'no pipe' value for pipelines tracking */
#define NO_PIPE_DESCRIPTOR -1

/* For readability purposes, define boolean values */
#define TRUE 1
#define FALSE 0
typedef char bool;

/* A strcut describing a command to be executed */
typedef struct {
	char** argv;
	int argc;
	bool background;
	bool piped;
} Command;

/* Total amount of commands */
static int n_commands = 0;
/* The array of commands */
static Command commands[MAX_COMMANDS_COUNT];
/* The array of commands' arguments */
static char *args[MAX_ARGS_COUNT];
/* Whether the program should quit */
static bool quit = FALSE;

/* Prints the shell's prompt line (accroding to the current directory) */
void print_prompt() {
	printf("%s >>> ", getcwd(0,0));
}

/* Changes the working directory according to the user' request */
void change_dir(const char* target) {
	if (chdir(target) != 0) {
		fprintf(stderr, "%s: No such file or directory.\n", target);
	}
}

/* Reads a line from the standard input. Returns a pointer to the first
 * character in the line, or NULL if reached the end of the input */
char* read_line(char *line, int max_line_len) {
	int line_len;
	if ((fgets(line, max_line_len, stdin)) == NULL) {
		/* Reached end of input */
		return 0;
	}
	line_len = strlen(line);
	/* Remove trailing \n */
	if (line[line_len-1] == '\n') {
		line[line_len-1] = '\0';
	}
	return line;
}

/* Parses a line into an array of commands. */
bool parse_line(char* line) {
	char *token;
	int start_idx = 0;
	int count = 0;
	n_commands = 0;
	Command *command;
	/* An empty or null line given */
	if (line == NULL || line[0] == 0) {
		return FALSE;
	}
	/* Parse the commands one by one */
	args[count++] = strtok(line, " ");
	if (strcmp(args[0], PIPE_STR) == 0 || strcmp(args[0], FG_STR) == 0) {
		/* Can't start with a | or && ... */
		fprintf(stderr, EMPTY_CMD_ERROR);
		return FALSE;
	}
	while ((token = strtok(NULL, " ")) != NULL && count < MAX_ARGS_COUNT - 1) {
		args[count++] = token;
		if (strcmp(token, FG_STR) == 0 || strcmp(token, PIPE_STR) == 0) {
			command = &commands[n_commands++];
			command->argv = args + start_idx;
			command->argc = count - start_idx - 1;
			if (command->argc == 0) {
				/* Illegal empty command */
				fprintf(stderr, EMPTY_CMD_ERROR);
				return FALSE;
			}
			command->background = FALSE;
			if (strcmp(token, PIPE_STR) == 0) {
				command->piped = TRUE;
			} else {
				command->piped = FALSE;
			}
			args[count-1] = NULL;
			args[count++] = NULL;
			start_idx = count;
		}
	}
	if (args[count - 1] == NULL) {
		/* We got a trailing | or &&. Illegal */
		fprintf(stderr, EMPTY_CMD_ERROR);
		return FALSE;
	}
	/* Close the last command */
	command = &commands[n_commands++];
	command->argv = args + start_idx;
	command->piped = FALSE;
	if (strcmp(args[count-1], BG_STR) == 0) {
		command->background = TRUE;
		args[count-1] = NULL;
		command->argc = count - start_idx - 1;
	} else {
		command->background = FALSE;
		args[count] = NULL;
		command->argc = count - start_idx;
	}
	if (command->argc == 0) {
		/* Last command shouldn't be empty (with a trailing &) */
		fprintf(stderr, EMPTY_CMD_ERROR);
		return FALSE;
	}
	return TRUE;
}

/* Closes a single pipe descriptor (input ot output) */
void close_des(int* des) {
	if (*des != NO_PIPE_DESCRIPTOR) {
		close(*des);
		*des = NO_PIPE_DESCRIPTOR;
	}
}
/* Closes a given pipe descriptor input-output tuple */
void close_fildes(int fildes[2]) {
	close_des(fildes);
	close_des(&fildes[1]);
}

/* Iterates over the parsed commands and executes them.
 * If a command fails, stops the iteration and informs the user.
 * Iteration also takes care of piped commands and redirects the standard input
 * and output of piped commands.  */
void run_commands() {
	int command_idx, child_status;
	Command command;
	pid_t pid;
	/* The input & output descriptort of an new pipe (first used for output) */
	int output_fildes[2] = {NO_PIPE_DESCRIPTOR, NO_PIPE_DESCRIPTOR};
	/* The input descriptor (only) of a former used pipe (for reading) */
	int input_des = NO_PIPE_DESCRIPTOR;
	for (command_idx = 0; command_idx < n_commands; ++command_idx) {
		/* Handle piping values */
		close_des(&input_des);
		if (output_fildes[0] != NO_PIPE_DESCRIPTOR) {
			input_des = output_fildes[0];
			output_fildes[0] = NO_PIPE_DESCRIPTOR;
			/* We don't need the pipe's input descriptor anymore */
			if (output_fildes[1] != NO_PIPE_DESCRIPTOR) {
				close_des(&output_fildes[1]);
			}
		}
		/* The enxt command to be handled */
		command = commands[command_idx];
		/* Open a pipe if needed */
		if (command.piped) {
			if (pipe(output_fildes)) {
				fprintf(stderr, "Error: Failed piping.\n");
				break;
			}
		}
		/* Handle a 'cd' command specially */
		if (strcmp(command.argv[0], CD_COMMAND) == 0) {
			if (command.argc > 1) {
				change_dir(command.argv[1]);
			} else {
				fprintf(stderr, "%s: Missing target directory.\n", CD_COMMAND);
			}
			continue;
		}
		/* Fork the current command */
		if ((pid = fork()) < 0) {
			/* The fork failed */
			fprintf(stderr, "Error: Failed executing.\n");
			break;
		}
		/* Fork succeeded, handle child and parent processes differently */
		if (pid == 0) {
			/* Child process*/
			/* Handle input and output of the child process (if piped) */
			if (input_des != NO_PIPE_DESCRIPTOR) {
				close(0);
				dup(input_des);
			}
			if (output_fildes[1] != NO_PIPE_DESCRIPTOR) {
				close(1);
				dup(output_fildes[1]);
				/* We don't need the pipe's output descriptor in the child */
				close(output_fildes[0]);
			}
			/* Execute the comamnd */
			execvp(command.argv[0], command.argv);
			fprintf(stderr, "%s: Failed executing.\n", command.argv[0]);
			quit = TRUE;
			break;
		}
		/* Parent process */
		if (!command.background) {
			/* Wait for the child process */
			waitpid(pid, &child_status, 0);
			if (WEXITSTATUS(child_status)) {
				/* Assuming the child logged its error, so we don't print it */
				break;
			}
		}
	}
	/* Close open pipelines descriptors */
	close_des(&input_des);
	close_fildes(output_fildes);
}

/* Main function. Performs the main loop of the program */
int main(int argc, char* argv[]) {
	char line[MAX_LINE_LEN];
	printf("**************************************************************\n"
		   "* Welcome to OS Ex4 shell, a shell that also supports pipes! *\n"
	       "**************************************************************\n");
	/* Keep reading input until the users type the EXIT_COMMAND or
	 * until the end of the input is rached. */
	while (!quit) {
		n_commands = 0;
		print_prompt();
		/* Read the input */
		if (read_line(line, MAX_LINE_LEN) == NULL || !strcmp(line, EXIT_COMMAND)) {
			printf("****************************************************\n"
				   "* We hope you enjoyed using this shell :) Goodbye! *\n"
			       "****************************************************\n");
			return 0;
		}
		/* Parse the input */
		if (!parse_line(line)) {
			// An error occured, don't execute the commands
			continue;
		}
		/* Run the commands */
		run_commands();
	}
	/* Child process failed. Return an error */
	return 1;
}
