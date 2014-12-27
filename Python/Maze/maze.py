#!/usr/bin/env python
#!/usr/bin/python

import random
import subprocess
import os
import sys
import termios
import fcntl
import time

def getch():
  fd = sys.stdin.fileno()

  oldterm = termios.tcgetattr(fd)
  newattr = termios.tcgetattr(fd)
  newattr[3] = newattr[3] & ~termios.ICANON & ~termios.ECHO
  termios.tcsetattr(fd, termios.TCSANOW, newattr)

  oldflags = fcntl.fcntl(fd, fcntl.F_GETFL)
  fcntl.fcntl(fd, fcntl.F_SETFL, oldflags | os.O_NONBLOCK)

  try:        
    while 1:            
      try:
        c = sys.stdin.read(1)
        break
      except IOError: pass
  finally:
    termios.tcsetattr(fd, termios.TCSAFLUSH, oldterm)
    fcntl.fcntl(fd, fcntl.F_SETFL, oldflags)
  return c

def clear_screan():
	tmp = subprocess.call('clear',shell=True)

def in_bound(n, x):
	return x >= 0 and x < n

def valid_neighbours(n, i, j):
	neighbours = []
	if i > 0:
		neighbours.append((i - 1, j))
	if i < n - 1:
		neighbours.append((i + 1, j))
	if j > 0:
		neighbours.append((i, j - 1))
	if j < n - 1:
		neighbours.append((i, j + 1))
	return neighbours

def next_cells(visited, n, i, j):
	return [x for x in valid_neighbours(n, i, j) if x not in visited]

def dead_end(visited, n, i, j):
	return len(next_cells(visited, n, i, j)) == 0

def add_adj(adj, i, j, x, y):
	if (i, j) not in adj:
		adj[(i, j)] = set()
	adj[(i, j)].add((x, y))
	if (x, y) not in adj:
		adj[(x, y)] = set()
	adj[(x, y)].add((i, j))
	
def random_maze(n):
	adj = {}
	visited_set = set()
	visited_set.add((0,0))
	visited_no_deadend = [(0,0)]
	count = n ** 2 - 1
	while count > 0:
		# Choose a new starting point, and follow it until we can't proceed anymore
		i, j = random.choice(visited_no_deadend)
		while not dead_end(visited_set, n, i, j):
			# We have a starting point. Create a path
			next_i, next_j = random.choice(next_cells(visited_set, n, i, j))
			visited_no_deadend.append((next_i, next_j))
			visited_set.add((next_i, next_j))
			add_adj(adj, i, j, next_i, next_j)
			count -= 1
			i, j = next_i, next_j
		visited_no_deadend.remove((i, j))
	return adj

def print_maze(adj, x = -1, y = -1, been_to = set()):
	n = int(len(adj) ** 0.5)
	maze = "#" * (n * 2 + 1)
	print maze
	for i in range(n):
		this_line = "#" if i > 0 else " "
		next_line = "#"
		for j in range(n):
			on_spot = i == x and j == y
			if i == x and j == y:
				cell_char = '0'
			elif (i, j) in been_to:
				cell_char = '.'
			else:
				cell_char = ' '
			if in_bound(n, j + 1):
				this_line += "%s%s" % (cell_char, " " if (i, j) in adj[(i, j + 1)] else "#")
			else:
				this_line += "%s%s" % (cell_char, " " if i == n - 1 else "#")
			if in_bound(n, i + 1):
				next_line += "%s#" % (" " if (i, j) in adj[(i + 1, j)] else "#")
			else:
				next_line += "##"
		print this_line
		print next_line
		maze += "%s\n%s\n" % (this_line, next_line)

def min_steps(adj):
	n = int(len(adj) ** 0.5)
	if n == 1:
		return 0
	visited = set()
	queue = [(0, 0, 0)]
	while len(queue) > 0:
		x, y, d = queue.pop()
		for neighbour in adj[(x,y)]:
			if neighbour == (n - 1, n - 1):
				return d + 1
			if neighbour not in visited:
				queue.insert(0, (neighbour[0], neighbour[1], d + 1))
				visited.add(neighbour)
	raise Exception("No path from start to end")
	
			
def play(n, level = 1):
	spot_x, spot_y = 0, 0
	maze = random_maze(n)
	min_steps_possible = min_steps(maze)
	steps = 0
	been_to = set()
	while spot_x != n - 1 or spot_y != n - 1:
		been_to.add((spot_x, spot_y))
		next_x, next_y = spot_x, spot_y
		first_read = True
		while (next_x, next_y) not in maze[(spot_x, spot_y)]:
			clear_screan()
			print "Up: i , Down: k , Right: l , Left: j , Quit: q"
			print "Level: %d" % (level)
			print_maze(maze, spot_x, spot_y, been_to)
			print "Steps: %d" % (steps)
			if first_read:
				print "Where to go next?"
				first_read = False
			else:
				print "Please choose a valid next step."
			c = getch()
			next_x, next_y = spot_x, spot_y
			if c == 'i':
				next_x -= 1
			elif c == 'k':
				next_x += 1
			elif c == 'j':
				next_y -= 1
			elif c == 'l':
				next_y += 1
			elif c == 'q':
				return False
		steps += 1
		spot_x, spot_y = next_x, next_y
	clear_screan()
	print
	print
	print_maze(maze, spot_x, spot_y)
	print "Steps: %d (min: %d)" % (steps, min_steps_possible)
	print "VICTORY!"
	return True
	
def full_game(n = 3):
	level = 1
	while True:
		if not play(n, level):
			return
		print
		for x in range (3, 0 , -1):  
			sys.stdout.write("\rNext level in %d..." % (x))
			sys.stdout.flush()
			time.sleep(1)
		n += 2
		level += 1
		
if __name__ == "__main__":
    full_game()

	
	
	