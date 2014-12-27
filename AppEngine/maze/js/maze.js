var row = 0
var col = 0
var game_over = false;

var DOWN_MASK = 1
var RIGHT_MASK = 2
var UP_MASK = 4
var LEFT_MASK = 8

var DIRECTION = Object.freeze({
	DOWN: 1,
	RIGHT: 2,
	UP: 4,
	LEFT: 8
});

function is_mobile() {
	return (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent));
}

function showDirectives() {
	if (!is_mobile()) {
		$("#directives_div").append('<div>Up: I , Down: K , Left: J , Right: L</div><hr>').trigger('create');
	}
}

function drawMaze() {
	var maze = get_maze();
	var n = maze_size();
	var row;
	var col;
	var table_content = "";
	var cell_size = computeCellSize();
	for (row = 0; row < n; ++row) {
		table_content += '<tr class="maze">';
		for (col = 0; col < n; ++col) {
			var cell_type = maze[row][col];
			mask_class = '';
			if ((cell_type & DOWN_MASK) > 0) {
				mask_class += ' down';
			}
			if ((cell_type & UP_MASK) > 0) {
				mask_class += ' up';
			}
			if ((cell_type & LEFT_MASK) > 0 || (row == 0 && col == 0)) {
				mask_class += ' left';
			}
			if ((cell_type & RIGHT_MASK) > 0 || (row == n - 1 && col == n - 1)) {
				mask_class += ' right';
			}
			var row_col = row + '_' + col;
			table_content += '<td id="cell_' + row_col + '" class="maze blank' + mask_class + '">';
			if (row == 0 && col == 0) {
				table_content += '<img src="img/current.png" id="on_spot_0_0" style="width: ' + cell_size + 'px;">' +
								 '<img src="img/start.png" style="display: none; width: ' + cell_size + 'px;" id="not_on_spot_0_0"></td>';
			} else if (row == n - 1 && col == n - 1) {
				table_content += '<img src="img/end.png" style="width: ' + cell_size + 'px;" id="not_on_spot_' + row_col + '">' +
								 '<img src="img/current.png" style="display: none; width: ' + cell_size + 'px;" id="on_spot_' + row_col + '"></td>';
			} else {
				table_content += '<img src="img/visited.png" style="width: ' + cell_size + 'px;" id="not_on_spot_' + row_col + '">' +
								 '<img src="img/current.png" style="display: none; width: ' + cell_size + 'px;" id="on_spot_' + row_col + '"></td>';
			}
			table_content += '</td>';
		}
		table_content += '</tr>';
	}
	$('#maze_table').append(table_content);
	$('#maze_table').trigger('create');
}

function nextStep(next_row, next_col) {
	$("#on_spot_" + row + "_" + col).css('display', 'none');
	$("#not_on_spot_" + row + "_" + col).css('display', 'block');
	$("#on_spot_" + next_row + "_" + next_col).css('display', 'block');
	$("#not_on_spot_" + next_row + "_" + next_col).css('display', 'none');
	row = next_row;
	col = next_col;
}

function getStepsCounter() {
	return parseInt(document.getElementById("steps_counter").value);
}

function setStepsCounter(steps) {
	document.getElementById("steps_counter").value = steps;
}

function keyPressed(event) {
	if (game_over) {
		return;
	}
	var chCode = ('charCode' in event) ? event.charCode : event.keyCode;
	switch (chCode) {
	    case 105: // I - Up 
			goUp();
	        break;
    	case 106: // J - Left
			goLeft();
			break;
		case 107: // K - Down
			goDown();
			break;
		case 108: // L - Right
			goRight();
			break;
		case 114: // R - Restart
			restart();
			break;
		case 98: // B - Back (to 5)
			back_to_start();
			break;
		default:
			return;
	}
}

function goUp() {
	doMove(DIRECTION.UP);
}

function goDown() {
	doMove(DIRECTION.DOWN);
}

function goRight() {
	doMove(DIRECTION.RIGHT);
}

function goLeft() {
	doMove(DIRECTION.LEFT);
}

function doMove(direction) {
	var n = maze_size();
	var next_row = row;
	var next_col = col;
	var cell_mask = maze_mask(row, col);
	switch (direction) {
		case DIRECTION.UP:
			if ((cell_mask & UP_MASK) == 0) return;
			next_row -= 1;
			break;
		case DIRECTION.LEFT:
			if ((cell_mask & LEFT_MASK) == 0) return;
			next_col -= 1;
			break;
		case DIRECTION.DOWN:
			if ((cell_mask & DOWN_MASK) == 0) return;
			next_row += 1;
			break;
		case DIRECTION.RIGHT:
			if ((cell_mask & RIGHT_MASK) == 0) return;
			next_col += 1;
			break;
		default:
			return;
	}
	// Check for a valid bound move.
	if (next_col < 0 || next_col >= n || next_row < 0 || next_row >= n) {
		return;
	}
	setStepsCounter(getStepsCounter() + 1);
	nextStep(next_row, next_col);
	// Check if reached the end.
	if (row == n - 1 && col == n - 1) {
		victory();
	}
}

function restart() {
	setStepsCounter(0);
	nextStep(0, 0);
}

function back_to_start() {
	if (confirm("Back to a 5X5 maze?")) {
		window.location.href = "/";
	}
}

function disableRestart() {
	$("#restart_button").attr('disabled', 'disabled');
	game_over = true;
}

function enableNextLevel() {
	var button = document.getElementById("next_level_button");
	button.disabled = "";
	button.focus();
}

function victory() {
	var maze_table = $("#maze_table");
	var victory_table = $("#victory_table");
	victory_table.width(maze_table.width());
	victory_table.height(maze_table.height());
	maze_table.css("display", "none");
	victory_table.css("display", "block");
	disableRestart();
	enableNextLevel();
}

function nextLevel() {
	// TODO: Load the page earlier, and add a nice 3D transition (from JQuey Mobile Tutorial).
	window.location.href = "/?size=" + (maze_size() + 3);
}

function computeCellSize() {
	var num_cells = maze_size();
	// Before calculating the screen size, we need to substract several components and paddings from the screen's height and width.
	var actual_height = $(window).height() - $('#header_div').height() - $('#footer_div').height() - $('#steps_div').height() - $('#directives_div').height() - 70 /* padding */;
	var actual_width = $(window).width() - 40;  // Leave some space (20px each side) between the maze and the page edges.
	// First, find the maximal space we have (the smaller of the width and height).
	var space = Math.min(actual_width, actual_height);
	// Leaave space for the 1px borders (edges included).
	space -= num_cells + 1;
	// If the outcome is larger than 32, will we use 32.
	return Math.min(32, Math.floor(space / maze_size()));
}

function doOnLoad() {
	showDirectives();
	drawMaze();
	(function() {
		// initializes touch and scroll events
		var supportTouch = $.support.touch,
					scrollEvent = "touchmove scroll",
	                touchStartEvent = supportTouch ? "touchstart" : "mousedown",
	                touchStopEvent = supportTouch ? "touchend" : "mouseup",
	                touchMoveEvent = supportTouch ? "touchmove" : "mousemove";

		// handles swipeup and swipedown
		$.event.special.swipeupdown = {
				    setup: function() {
					var thisObject = this;
	                var $this = $(thisObject);

	                $this.bind(touchStartEvent, function(event) {
	                    var data = event.originalEvent.touches ?
	                            event.originalEvent.touches[ 0 ] :
	                            event,
	                            start = {
	                                time: (new Date).getTime(),
	                                coords: [ data.pageX, data.pageY ],
	                                origin: $(event.target)
	                            },
	                            stop;

	                    function moveHandler(event) {
	                        if (!start) {
	                            return;
	                        }

	                        var data = event.originalEvent.touches ?
	                                event.originalEvent.touches[ 0 ] :
	                                event;
	                        stop = {
	                            time: (new Date).getTime(),
	                            coords: [ data.pageX, data.pageY ]
	                        };

	                        // prevent scrolling
	                        if (Math.abs(start.coords[1] - stop.coords[1]) > 10) {
	                            event.preventDefault();
	                        }
	                    }

	                    $this
	                            .bind(touchMoveEvent, moveHandler)
	                            .one(touchStopEvent, function(event) {
	                        $this.unbind(touchMoveEvent, moveHandler);
	                        if (start && stop) {
	                            if (stop.time - start.time < 1000 &&
	                                    Math.abs(start.coords[1] - stop.coords[1]) > 30 &&
	                                    Math.abs(start.coords[0] - stop.coords[0]) < 75) {
	                                start.origin
	                                        .trigger("swipeupdown")
	                                        .trigger(start.coords[1] > stop.coords[1] ? "swipeup" : "swipedown");
	                            }
	                        }
	                        start = stop = undefined;
	                    });
	                });
	            }
	        };

	//Adds the events to the jQuery events special collection
	        $.each({
	            swipedown: "swipeupdown",
	            swipeup: "swipeupdown"
	        }, function(event, sourceEvent){
	            $.event.special[event] = {
	                setup: function(){
	                    $(this).bind(sourceEvent, $.noop);
	                }
	            };
	        });

	    })();
	
	$('#maze_table').on('swipeup', goUp);
	$('#maze_table').on('swipedown', goDown);
	$('#maze_table').on('swipeleft', goLeft);
	$('#maze_table').on('swiperight', goRight);
	
	var elem = document.getElementById("page_div");
	if (elem.requestFullscreen) {
	  elem.requestFullscreen();
	} else if (elem.msRequestFullscreen) {
	  elem.msRequestFullscreen();
	} else if (elem.mozRequestFullScreen) {
	  elem.mozRequestFullScreen();
	} else if (elem.webkitRequestFullscreen) {
	  elem.webkitRequestFullscreen();
	}
	
	$("#next_level_button").attr('disabled', 'disabled');
	if (maze_size() == 5) {
		$("#back_to_start_button").attr('disabled', 'disabled');
	}
}