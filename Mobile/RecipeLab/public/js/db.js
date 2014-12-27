/**
 * Holds all the functions that handle the communication with the DB
 * of the application.
 */

var database = undefined;

// Try to open the local DB and have it available.
try {
  if (!window.openDatabase) {
    alert('not supported');
  } else {
    // setting for our database
    var shortName = 'recipedb';
    var version = '1.0';
    var displayName = 'Recipe LAb DB';
    var maxSize = 1048576; // 1 MB should be enough for now.
    var database = openDatabase(shortName, version, displayName, maxSize);
  }
} catch(e) {
  // Error handling code goes here.
  if (e === 2) {
    // Version number mismatch.
    alert('Invalid database version.');
  } else {
    alert('Unknown error when opening the DB: ' + e);
  }
}

/*
 * Cleans the DB from any user information.
 */
var clean_db = function(callback) {
  database.transaction(function(transaction) {
    transaction.executeSql('DELETE FROM units;');
    transaction.executeSql('DELETE FROM cuisines;');
    transaction.executeSql('DELETE FROM recipes;');
    transaction.executeSql('DELETE FROM recipes_cuisines;');
    transaction.executeSql('DELETE FROM ingredients;');
  }, function() { 
    alert('Failed deleting user data!'); 
    if (callback !== undefined) {
      callback(false);
    }
  }, function() { 
    if (callback !== undefined) { 
      callback(true); 
    } 
  });
};

/**
 * Creates the tables of the DB (if they don't already exist).
 */
var create_tables = function() {
  database.transaction(function(transaction) {
    transaction.executeSql(
      'CREATE TABLE IF NOT EXISTS cuisines (' +
      'cuisine TEXT NOT NULL PRIMARY KEY);');

    transaction.executeSql(
      'CREATE TABLE IF NOT EXISTS units (' +
      'unit TEXT NOT NULL PRIMARY KEY);');

    transaction.executeSql(
      'CREATE TABLE IF NOT EXISTS recipes (' +
      'r_id INTEGER PRIMARY KEY,' +
      'name TEXT NOT NULL,' +
      'create_date INTEGER NOT NULL,' +
      'creator TEXT NOT NULL,' +
      'instructions TEXT NOT NULL,' +
      'pic_url TEXT,' +
      'pic_bin TEXT,' +
      'fav BOOLEAN NOT NULL);');
      
    transaction.executeSql(
      'CREATE TABLE IF NOT EXISTS recipes_cuisines (' +
      'r_id INTEGER NOT NULL,' +
      'cuisine TEXT NOT NULL,' +
      'FOREIGN KEY(r_id) REFERENCES recipes(r_id),' +
      'PRIMARY KEY (r_id, cuisine));');

    transaction.executeSql(
      'CREATE TABLE IF NOT EXISTS ingredients (' +
      'r_id INTEGER NOT NULL,' +
      'name TEXT NOT NULL,' +
      'type TEXT NOT NULL,' +
      'amount INTEGER NOT NULL,' +
      'FOREIGN KEY(r_id) REFERENCES recipes(r_id),' +
      'PRIMARY KEY (r_id, name));');
  });
};

/**
 * Retrieves from the server the basic information for the user, and
 * inserts it into the DB.
 */
var refresh_db_info = function() {
  // First, make sure all the DB tables exist and ready.
  create_tables();
  // Send a refresh request (to get the up-to-date data.
  var refresh_request = new XMLHttpRequest();
  if (refresh_request) {
    refresh_request.open('GET', '/refresh_data', false);
    refresh_request.setRequestHeader('recipelab-user', get_cookie('user'));
    refresh_request.onreadystatechange = function() {
      if (refresh_request.readyState === 4) {
        if (refresh_request.status === 200) {
          // The return object should be an object with the following keys and values:
          //  * ing_unit_types: An array with all the ingredient unit types.
          //  * cuisines_types: An array with all the cuisines types.
          //  * recipes: All the user's available recipes.
          var refreshed_data = JSON.parse(refresh_request.responseText);
          database.transaction(function(transaction) {
            // First, delete the stale data.
            transaction.executeSql('DELETE FROM units;');
            transaction.executeSql('DELETE FROM cuisines;');
            transaction.executeSql('DELETE FROM recipes;');
            transaction.executeSql('DELETE FROM recipes_cuisines;');
            transaction.executeSql('DELETE FROM ingredients;');
            // Insert the ingredients units into the UNITS table.
            var units = refreshed_data.ing_unit_types;
            for (var i = 0; i < units.length; ++i) {
              transaction.executeSql('INSERT INTO units (unit) VALUES (?)', [units[i]]);
            }
            // insert new CUISINES data into the DB.
            var cuisines = refreshed_data.cuisines_types;
            for (var i = 0; i < cuisines.length; ++i) {
              transaction.executeSql('INSERT INTO cuisines (cuisine) VALUES (?)', [cuisines[i]]);
            }
            // Finally, read the recipes from server and store them in the DB.
            var recipes = refreshed_data.recipes;
            for (var i = 0; i < recipes.length; ++i) {
              tran_insert_recipe(recipes[i], transaction);
            }
          }, function() {
            // An error occurred. Rollback so the at least the old (and maybe not so
            // stale) data will remain avaiable. It's better than nothing...
            alert('Failed to refresh the local data against the server.');
            return true;  // True => Perform rollback.
          });
        }
      } else {
        var msg = 'Failed to get up-to-date data (status=' + refresh_request.status + ').';
        alert(msg);
        console.log(msg);
      }
    };
    refresh_request.send(null);
  }
};

/**
 * A wrapper for adding the user's info into the HTTP request, to make sure the
 * permissions are set in the request. While at it, also setting the HTTP method and
 * UTI of the request. If AJAX is not supported, returns 'undefined'.
 * 
 * Note: creates async messages only. 
 */
var new_request = function(http_method, uri) {
  var request = new XMLHttpRequest();
  if (request) {
    request.open(http_method, uri, true);
    request.setRequestHeader('recipelab-user', get_cookie('user'));
    return request;
  } else {
    var msg = 'XMLHttpRequest not supprted.';
    alert(msg);
    console.log(msg);
    return undefined;
  }
};

/*
 * Read the available cuisines, and invokes the given callback with the
 * array of cuisines once the result returns.
 */
var read_cuisines = function(callback) {
  // Create a callback that will retrieve the cuisines types from the DB result.
  var bridge_callback = function(transaction, result) {
    // Respond to the orignal callback with the found cuisine types.
    var cuisines = [];
    for (var i = 0; i < result.rows.length; ++i) {
      cuisines.push(result.rows.item(i).cuisine);
    }
    callback(cuisines);
  };
  // Create a callback that will retrieve the cuisines from the server in case of a DB failure.
  var failure_callback = function(transaction, error) {
    var request = new_request('GET','/cuisines');
    if (request) {
      request.onreadystatechange = function() {
        if (request.readyState === 4) {
          if (request.status === 200) {
              var cuisines = JSON.parse(request.responseText);
              callback(cuisines);
          } else {
            var msg = 'Failed to get cuisines type (status=' + request.status + ').';
            alert(msg);
            console.log(msg);
          }
        }
      };
      request.send(null);
    }
  };
  database.transaction(function(transaction) {
    transaction.executeSql('SELECT cuisine FROM cuisines;', [], bridge_callback, failure_callback);
  });
};

/**
 * Expects a recipe in the form of an object with the following values:
 * {
 *   id: [recipe's ID],
 *   name: [recipe's name],
 *   date: [recipe's date],
 *   creator: [recipe's creator],
 *   ingredients: [recipe's ingredients - a list of trio-tuple: [amount, type, name] ],
 *   types: [recipe's cuisines types (in an array)],
 *   img: [recipe's image URL],
 *   instructions: [recipe's processing instructions]
 *   fav: true/false
 * }
 * And inserts it into the DB.
 * 
 * It is optional to specify a failure callback for the transaction. It will be called
 * in any failure along the way (to allow rollback).
 * 
 * Note: should be invoked from with a DB transaction (transaction arg).
 */
var tran_insert_recipe = function(recipe, transaction, failure_callback) {
  var use_img = recipe.img !== undefined && recipe.img !== '';
  var recipe_img = use_img ? recipe.img : '';
  var recipe_img_bin = use_img ? '' : recipe.img_bin;
  // Insert into the main rceipe table.
  transaction.executeSql(
    'INSERT INTO recipes (r_id, name, create_date, creator, instructions, pic_url, pic_bin, fav) ' +
    'VALUES (?,?,?,?,?,?,?,?)',  [recipe.id, recipe.name, recipe.date, recipe.creator, 
    recipe.instructions, recipe_img, recipe_img_bin, (recipe.fav ? 1 : 0)], undefined, failure_callback);
  // Set all the cuisines.
  for (var j = 0; j < recipe.types.length; ++j) {
    transaction.executeSql(
      'INSERT INTO recipes_cuisines (r_id, cuisine) VALUES (?,?);',
      [recipe.id, recipe.types[j]], undefined, failure_callback);
  }
  // Insert all the ingredients.
  for (var j = 0; j < recipe.ingredients.length; ++j) {
    var ing = recipe.ingredients[j];
    transaction.executeSql(
      'INSERT INTO ingredients (r_id, name, type, amount) VALUES (?,?,?,?);',
      [recipe.id, ing[2], ing[1], ing[0]], undefined, failure_callback);
  }
  // And we're done :)
};

/**
 * Delete the recipe with the given ID from the DB (with all its related rows in all tables).
 * 
 * It is optional to specify a failure callback for the transaction. It will be called
 * in any failure along the way (to allow rollback).
 * 
 * Note: should be invoked from with a DB transaction (transaction arg).
*/
var tran_delete_recipe = function(id, transaction, failure_callback) {
  transaction.executeSql('DELETE FROM recipes_cuisines WHERE r_id=?', [id], 
                         undefined, failure_callback);
  transaction.executeSql('DELETE FROM ingredients WHERE r_id=?', [id], 
                         undefined, failure_callback);
  transaction.executeSql('DELETE FROM recipes WHERE r_id=?', [id], 
                         undefined, failure_callback);
};

/**
 * Retrieves all the recipes, and invokes the callback with an array holding all
 * the available recipes. Each recipe is in the form:
 * {
 *   id: [recipe's ID],
 *   name: [recipe's name],
 *   date: [recipe's date],
 *   creator: [recipe's creator],
 *   ingredients: [recipe's ingredients - a list of trio-tuple: [amount, type, name] ],
 *   types: [recipe's cuisines types (in an array)],
 *   img: [recipe's image URL],
 *   instructions: [recipe's processing instructions]
 *   fav: true/false
 * }
 * 
 * Receives the condition (cond) from which the data should be extracted from the DB and the
 * failover URI to retrieve the data from the server.
 * 
 * Condition is expected in a form of an array with 2 values:
 * (1) A string which will be concated after the WHERE clause.
 * (2) The list of values received from the user, and replaced with '?' in (1) above.
 */
var read_recipes = function(uri, cond, callback) {
  // Create a failure callback in case the DB is not available.
  var failure_callback = function(transaction, error) {
    var request = new_request('GET', uri);
    if (request) {
      request.onreadystatechange = function() {
        if (request.readyState === 4) {
          if (request.status === 200) {
              var all_recipes = JSON.parse(request.responseText);
              callback(all_recipes);
          } else {
            var msg = 'Failed to get recipes (status:' + request.status + '  URI:' + uri + ').';
            alert(msg);
            console.log(msg);
          }
        }
      };
      request.send(null);
    }
  };
  if (cond === undefined) {
    // Undefined condition, use the server (skip DB).
    failure_callback(undefined, undefined);
    return;
  }
  // Create a bridge callback that will seek the data in the DB.
  var bridge_callback = function(transaction, result) {
    // Circuite in case of no results.
    if (result.rows.length === 0) {
      callback([]);
      return;
    }
    recipes = {};
    recipe_ids = '';
    for (var i = 0; i < result.rows.length; ++i) {
      var row_item = result.rows.item(i);
      recipes[row_item.r_id] = {
        id: row_item.r_id,
        name: row_item.name,
        date: row_item.create_date,
        creator: row_item.creator,
        ingredients: [],
        types: [],
        img: row_item.pic_url,
        img_bin: row_item.pic_bin,
        instructions: row_item.instructions,
        fav: (row_item.fav === 1)
      };
      if (i !== 0) {
        recipe_ids += ',';
      }
      recipe_ids += row_item.r_id;
    }
    // Add the ingredients.
    database.transaction(function(transaction) {
      transaction.executeSql(
          'SELECT * FROM ingredients WHERE r_id in (?);', [recipe_ids],
          function (transaction, result) {
            for (var i = 0; i < result.rows.length; ++i) {
              var row_item = result.rows.item(i);
              var ing = [row_item.amount, row_item.type, row_item.name];
              recipes[row_item.r_id].ingredients.push(ing);
            }
            // Now add the cuisines types.
            database.transaction(function(transaction) {
              transaction.executeSql(
                  'SELECT * FROM recipes_cuisines WHERE r_id in (?);', [recipe_ids],
                  function (transaction, result) {
                    for (var i = 0; i < result.rows.length; ++i) {
                      var row_item = result.rows.item(i);
                      recipes[row_item.r_id].types.push(row_item.cuisine);
                    }
                    // Build the return value (as array).
                    return_value = [];
                    for (var key in recipes) {
                      return_value.push(recipes[key]);
                    }
                    // Finally, call the callback with the built result. 
                    callback(return_value);
                  }, failure_callback);
            });
          }, failure_callback);
    });
  };
  database.transaction(function(transaction) {
    // Construct the query.
    var fields = 'r_id,name,create_date,creator,instructions,pic_url,pic_bin,fav';
    var query = 'SELECT ' + fields + ' FROM recipes';
    if (cond[0] !== '') {
      query += ' WHERE ' + cond[0];
    }
    query += ' ORDER BY r_id ASC;';
    transaction.executeSql(query, cond[1], bridge_callback, failure_callback);
  });
};

/**
 * Retrieve the recipe with the given ID and calls the callback with the recipe data.
 */
var recipe_by_id = function(id, callback) {
  var uri = '/recipe?id=' + id;
  var cond = ['r_id=?', [id]];
  read_recipes(uri, cond, function(recipes) {
    // There should at most a single recipe in the array (one per ID...).
    if (recipes.length > 0) {
      callback(recipes[0]);
    } else {
      callback();
    }
  });
};

/**
 * Retrieve all recipes of the user and calls the callback with the recipes data.
 */
var all_recipes = function(callback) {
  var uri = '/recipes';
  var cond = ['', []];  // No special condition. empty parameters.
  read_recipes(uri, cond, callback);
};

/**
 * Retrieve all recipes of the given type and calls the callback with the recipes data.
 */
var all_by_cuisine = function(value, callback) {
  var uri = '/cuisine?cuisine=' + value;
  database.transaction(function(transaction) {
    transaction.executeSql(
        'SELECT r_id FROM recipes_cuisines WHERE cuisine=\'?\'', [value],
        function(transaction, result) {  // Success
          // Circuite the case of no recipes.
          if (result.rows.length === 0) {
            callback([]);
            return;
          }
          var ids = '' + result.rows.item(0).r_id;
          for (var i = 1; i < result.rows.length; ++i) {
            ids += ',' + result.rows.item(i).r_id;
          }
          var cond = ['r_id IN (?)', [ids]];
          read_recipes(uri, cond, callback);
        }, function(transaction, error) {  // Failure
          read_recipes(uri, undefined /* Read straight from the server */, callback);
        });
  });
};

/**
 * Retrieve all favorite recipes.
 */
var all_favorites = function(callback) {
  var uri = '/favs';
  var cond = ['fav=?', [1]];
  read_recipes(uri, cond, callback);
};

/**
 * Retrieve all the recipes IDs with the given value as a substring of their name.
 */
var all_by_name = function(value, callback) {
  var uri = '/search?type=name&val=' + value;
  var cond = ['name like \'%?%\'', [value ]];
  read_recipes(uri, cond, callback);
};

/**
 * Retrieve all the recipes with the given value as a substring of their creator's name.
 */
var all_by_creator = function(value, callback) {
  var uri = '/search?type=creator&val=' + value;
  var cond = ['creator like \'%?%\'', [value]];
  read_recipes(uri, cond, callback);
};

/**
 * Retrieve all the recipes which were created after the given date.
 */
var all_by_date = function(value, callback) {
  var uri = '/search?type=date&val=' + value;
  var cond = ['create_date >= ?', [value]];
  read_recipes(uri, cond, callback);
};

/**
 * Changes the favortie state of the recipe with the given ID.
 * Invokes the given callback with the new value (true/false).
 */
var toggle_fav = function(id, callback) {
  // Create a bridge callback for the DB result.
  var bridge_callback = function(transaction, result) {
    if (result.rows.length > 0) {
      // First, change the value of the fovorite in the local DB.
      var new_fav = 1 - result.rows.item(0).fav;
      transaction.executeSql('UPDATE recipes SET fav=? WHERE r_id=?', [new_fav, id]);
      callback(new_fav);
      // Now that the user was informed with the new value, also send a request for
      // the server to change the stored state (for persistency).
      var request = new_request('GET', '/set_fav?id=' + id + '&fav=' + new_fav);
      if (request) {
        request.onreadystatechange = function() {
          if (request.readyState === 4) {
            if (request.status === 200) {
                var returned_fav = JSON.parse(request.responseText);
                if (returned_fav !== new_fav) {
                  alert('Concurrent updates? Failed to update the server with ' +
                        'the new starred state (result code: ' + returned_fav + ').');
                }
            } else {
              var msg = 'Failed to toggle favorability (status:' + 
                        request.status + '  ID:' + id + ').';
              alert(msg);
              console.log(msg);
            }
          }
        };
        request.send(null);
      }
    }
  };
  // A failure callback to get the data from the server side in case of a DB failure.
  var failure_callback = function(transaction, error) {
    var request = new_request('GET', '/toggle_fav?id=' + id);
    if (request) {
      request.onreadystatechange = function() {
        if (request.readyState === 4) {
          if (request.status === 200) {
              var returned_fav = JSON.parse(request.responseText);
              callback(parseInt(returned_fav));
          } else {
            var msg = 'Failed to toggle favorability (status:' + 
                      request.status + '  ID:' + id + ').';
            alert(msg);
            console.log(msg);
          }
        }
      };
      request.send(null);
    }
  };
  database.transaction(function(transaction) {
    transaction.executeSql('SELECT fav FROM recipes WHERE r_id=?', [id], 
                           bridge_callback, failure_callback);
  });
};

/**
 * Tries to delete a recipe with the given ID.
 * Invokes the callback with the result (true/false for success/failure).
 */
var send_delete_request = function(id, callback) {
  // First, delete the recipe from the server (for persistency).
  var uri = '/recipe?id=' + id;
  var request = new_request('DELETE', uri);
  if (request) {
    request.onreadystatechange = function() {
      if (request.readyState === 4) {
        if (request.status === 200) {
            var result = JSON.parse(request.responseText);
            if (result !== 'OK') {
              // Failed to delete the recipe, so not deleting the recipe from the local
              // DB. Invoke the callback to indicate the failure.
              callback(result);
            }
            // Proceed with deleting the recipe from the local DB. There are several
            // tables and it should be done "ALL or NONE", so make sure the failure
            // callback performs a rollback in case of a failure.
            var failure_callback = function(transaction, error) {
              // This is the failure callback. It could lead to a strange case where the
              // server succeeded and the DB failed. In this case, we just mark an 
              // error (to be on the safe side).
              callback('Recipe was delete from the server, but failed to be deleted localy: ' +
                       error.message + '\nPlease consider logging out and in again to fix ' +
                       'the problem.');
              // Perform a rollback.
              return true;
            };
            database.transaction(function(transaction) {
              tran_delete_recipe(id, transaction, failure_callback);
              callback('OK');  // SUCCESS
            });
        } else {
          var msg = 'Failed to delete the recipe (status:' + request.status + '  URI:' + uri + ').';
          alert(msg);
          console.log(msg);
        }
      }
		};
		request.send(null);
  }
};

/**
 * Tries to update the given recipes (against the server and DB).
 * Invokes the given callback with the ID of the updated recipe (as it might
 * change the ID of shared recipes).
 */
var update_recipe = function(recipe, callback) {
  // First, send an update request to the server. If it decides to create a new recipe
  // (in case it is a shared recipe), then we should act accordingly.
  var old_id = recipe.id;
  request = new_request('PUT', '/recipe');
  if (request) {
    request.onreadystatechange = function() {
      if (request.readyState === 4) {
        if (request.status === 200) {
          var new_id = parseInt(JSON.parse(request.responseText));
          if (new_id < 0) {
            // Operation failed. Inform and return.
            callback(new_id);
            return;
          }
          // Read the previous recipe from the database, and use it for the update.
          recipe_by_id(old_id, function(old_recipe) {
	        if ((recipe.img === undefined || recipe.img === '') &&
	            (recipe.img_bin === undefined || recipe.img_bin === '')) {
			  recipe.img = old_recipe.img;
              recipe.img_bin = old_recipe.img_bin;
            }
            // Server updated. Now update the local DB. We could have perform surgical updates,
            // but that would require a lot of DIFFs, so instead we simply delete and insert the
            // recipe from the tables.
            var failure_callback = function(transaction, error) {
              // This is the failure callback. It could lead to a strange case where the
              // server succeeded and the DB failed. In this case, we just mark an 
              // error (to be on the safe side).
              alert('Recipe was updated in the server, but failed to be updated localy: ' +
                    error.message + '\nPlease consider logging out and in again to fix ' +
                    'the problem.');
              callback(-1);
              return true; // Perform rollback.
            };
            database.transaction(function(transaction) {
              // First, delete the old recipe rows.
              tran_delete_recipe(recipe.id /* old ID */, transaction, failure_callback);
              // And no add the recipe again with the new ID (could be the old one as well...).
              recipe.id = new_id;
              tran_insert_recipe(recipe, transaction, failure_callback);
              callback(new_id);  // SUCCESS
            });
          });
        } else {
          var msg = 'Failed to modify the recipe (status:' + request.status + '  URI: /recipe).';
          alert(msg);
          console.log(msg);
        }
      }
		};
    request.setRequestHeader("Content-Type", "application/json");
		request.send(JSON.stringify(recipe));
  }
};

/**
 * Tries to insert a new recipes (into the DB and sent to the server).
 * Invokes the given callback with the ID of the newly added recipe.
 */
var insert_recipe = function(recipe, callback) {
  // First update the server, and retrieve the new recipe's ID.
  var request = new_request('POST', '/recipe');
  if (request) {
    request.onreadystatechange = function() {
      if (request.readyState === 4) {
        if (request.status === 200) {
          var new_id = parseInt(JSON.parse(request.responseText));
          if (new_id < 0) {
            // Failure. Notify and exit.
            callback(new_id);
            return;
          }
          // The server succeeed, augment the recipe with the new ID and insert it to the DB.
          recipe.id = new_id;
          var failure_callback = function(transaction, error) {
            // This is the failure callback. It could lead to a strange case where the
            // server succeeded and the DB failed. In this case, we just mark an 
            // error (to be on the safe side).
            alert('Recipe was added to server, but failed to add it localy: ' + error.message + 
                  '\nPlease consider logging out and in again to fix the problem.');
            callback(-1);
            return true; // Perform rollback.
          };
          database.transaction(function(transaction) {
            tran_insert_recipe(recipe, transaction, failure_callback);
            // Success. Inform the caller about the new ID.
            callback(new_id);
          });
        } else {
          var msg = 'Failed to add the recipe (status:' + request.status + '  URI: /recipe).';
          alert(msg);
          console.log(msg);
        }
      }
		};
    request.setRequestHeader("Content-Type", "application/json");
		request.send(JSON.stringify(recipe));
  }
};

/**
 * Reads all the ingredients unit types from the DB and invokes the callback
 * with an array containing the results.
 */
var all_ing_units = function(callback) {
  // A bridge callback to translate the DB result into an array of ingredients types.
  var bridge_callback = function(transaction, result) {
    var units_arr = [];
    for (var i = 0; i < result.rows.length; ++i) {
      units_arr.push(result.rows.item(i).unit);
    }
    callback(units_arr);
  };
  // A failure callback to handle a case where the DB is not responsive and we need
  // to query the server directly.
  var failure_callback = function(transaction, error) {
    var request = new_request('GET', '/units');
    if (request) {
      request.onreadystatechange = function() {
        if (request.readyState === 4) {
          if (request.status === 200) {
            callback(JSON.parse(request.responseText));
          }
        }
      };
      request.send(null);
    }
  };
  database.transaction(function(transaction) {
    transaction.executeSql('SELECT unit FROM units ORDER BY unit ASC', [], 
                           bridge_callback, failure_callback);
  });
};
