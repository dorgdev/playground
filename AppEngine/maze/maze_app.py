import cgi
import os
import urllib
import random

from google.appengine.api import users
from google.appengine.ext import ndb

import jinja2
import webapp2

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

DOWN_MASK = 1
RIGHT_MASK = 2
UP_MASK = 4
LEFT_MASK = 8

DEFAULT_SIZE = 5

class Maze(webapp2.RequestHandler):
	def valid_neighbours(self, n, i, j):
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

	def next_cells(self, visited, n, i, j):
		return [x for x in self.valid_neighbours(n, i, j) if x not in visited]

	def dead_end(self, visited, n, i, j):
		return len(self.next_cells(visited, n, i, j)) == 0

	def connect_cells(self, i, j, x, y):
		mask_from = 0
		mask_to = 0
		if x == i - 1:
			mask_from = UP_MASK
			mask_to = DOWN_MASK
		elif x == i + 1:
			mask_from = DOWN_MASK
			mask_to = UP_MASK
		elif y == j - 1:
			mask_from = LEFT_MASK
			mask_to = RIGHT_MASK
		else:
			mask_from = RIGHT_MASK
			mask_to = LEFT_MASK
		self.maze[i][j] ^= mask_from
		self.maze[x][y] ^= mask_to

	def random_maze(self, n):
		self.maze = []
		for i in range(n):
			self.maze.append([0] * n)
		visited_set = set()
		visited_set.add((0,0))
		visited_no_deadend = [(0,0)]
		count = n ** 2 - 1
		while count > 0:
			# Choose a new starting point, and follow it until we can't proceed anymore
			i, j = random.choice(visited_no_deadend)
			while not self.dead_end(visited_set, n, i, j):
				# We have a starting point. Create a path
				next_i, next_j = random.choice(self.next_cells(visited_set, n, i, j))
				visited_no_deadend.append((next_i, next_j))
				visited_set.add((next_i, next_j))
				self.connect_cells(i, j, next_i, next_j)
				count -= 1
				i, j = next_i, next_j
			visited_no_deadend.remove((i, j))

	def create_page(self):
		self.page = """
<html>
  <head>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.css">
	<script src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
	<script src="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.js"></script>
    <link type="text/css" rel="stylesheet" href="/css/maze.css" />
    <script src="/js/maze.js"></script>
  </head>
  <body onkeypress="keyPressed(event)">
  <script>
function maze_size() {
	return %d;
};
function get_maze() {
	return %s;
}
function maze_mask(i, j) {
	var maze_array = get_maze();
	return maze_array[i][j];
}
$(document).ready(doOnLoad);
  </script>
	<div data-role="page" id="page_div">
	
	<div data-role="header" id="header_div">
		<h1>A-Maze-ing!</h1>
	</div>
	
    <div data-role="main" class="ui-content" id="main_div">
      <div data-role="fieldcontain" id="steps_div">
        <label for="steps">Steps:</label>
        <input type="number" name="steps" id="steps_counter" readonly="readonly" value="0"/>
      </div>
	  <div id="directives_div"></div>
	  <div id="maze_div">
        <table class="maze" id="maze_table">
        </table>
      </div>
      <div>
	    <table id="victory_table" style="display: none;"><tr><td><H1>Victory</H1></td></tr></table>
      </div>
	</div>

	<div data-role="footer" id="footer_div" data-position="fixed">
	  <div data-role="navbar">
	    <ul>
	      <li><input type="button" class="ui-shadow" data-icon="recycle" data-iconpos="top" id="back_to_start_button" onclick="back_to_start();"/></li>
	      <li><input type="button" class="ui-shadow" data-icon="refresh" data-iconpos="top" id="restart_button" onclick="restart();"/></li>
	      <li><input type="button" class="ui-shadow" data-disabled="true" data-icon="arrow-r" data-iconpos="top" id="next_level_button" onclick="nextLevel();"/></li>
	    </ul>
	  </div>
	</div>
	
	</div>
  </body>
</html>""" % (len(self.maze), str(self.maze))

	def get(self):
		maze_size = int(self.request.get('size', DEFAULT_SIZE))
		self.random_maze(maze_size)
		self.create_page();
		self.response.write(self.page)

application = webapp2.WSGIApplication([
    ('/', Maze),
], debug=True)


