/*
 * Saves a pair of (naem,value) to the local browser's memory using the
 * browser's cookies mechanism.
 */
function set_cookie(c_name, value) {
  var c_value = escape(value);
  document.cookie = c_name + "=" + c_value + ";";
}

/*
 * Tries to read the cookie named by given value from the local
 * browser's memory.
 */
function get_cookie(c_name) {
  var c_value = document.cookie;
  var c_start = c_value.indexOf(" " + c_name + "=");
  if (c_start === -1) {
    c_start = c_value.indexOf(c_name + "=");
  }
  if (c_start === -1) {
    c_value = "";
  } else {
    c_start = c_value.indexOf("=", c_start) + 1;
    var c_end = c_value.indexOf(";", c_start);
    if (c_end === -1)
    {
      c_end = c_value.length;
    }
    c_value = unescape(c_value.substring(c_start, c_end));
  }
  return c_value;
}

/*
 * Checks whether there's a logged in user (currently).
 */
function is_logged_in() {
  return (get_cookie("user") !== "");
}

/*
 * Performs the checkout mechanism (by removing the logged in
 * user's cookie from the local browser's memory).
 */
function log_out() {
  if (is_logged_in()) {
    remove_param('last_sync');
    remove_param('last_sync_user');
    set_cookie("user", "");
    clean_db(function() { window.location = "index.html"; });
  }
}

/*
 * Makes sure that the user can watch a certain page (logged
 * in already). If the user is not logged in, redirects them
 * to the login page.
 */
function validate_access() {
  if (!is_logged_in()) {
    window.location = "index.html";
  }
}

/*
 * We want that our logged in users will start from the home page,
 * so this function allows us to redirect logged in users to the
 * home page.
 */
function reroute_if_logged_in() {
  if (!is_logged_in()) {
    return false;
  }
  window.location = "home.html";
  return true;
}

/*
 * Navigates to a different page using JQM interface (for
 * a cleaner loading process).
 */
function navigate(dest) {
  $.mobile.changePage(dest);
}

/*
 * A helper function which do both of storing the given parameters (which
 * is a list of pairs), and navigating to the desired destaintion page.
 */
function navigate_with_params(dest, params) {
  for (var i = 0; i < params.length; ++i) {
    set_param(params[i][0], params[i][1]);
  }
  navigate(dest);
}

/*
 * Returns all the parameters (stored in the 'params' cookie).
 */
function get_params() {
  return get_cookie("params");
}

/*
 * Removes a certain parameters from the 'params' cookie.
 */
function remove_param(name) {
  var params = get_params();
  var index = params.search('&' + name + '=');
  if (index === -1) {
    return;
  }
  var start = index + ('&' + name + '=').length;
  var end_index = params.substr(start).search('&');
  if (end_index < 0) {
    params = params.substr(0, index);
  } else {
    params = params.substr(0, index) + params.substr(start + end_index);
  }
  set_cookie('params', params);
};

/*
 * Sets the given pair of (name,value) in the 'params' cookie.
 */
function set_param(name, value) {
  remove_param(name);
  var params = get_params();
  set_cookie('params', params + '&' + name + '=' + value);
};

/*
 * Tries to read a certain parameter from the 'params' cookie.
 * Returns an empty string in case the parameter does not exist.
 */
function get_param(name) {
  var params_str = get_params();
  if (params_str === "") {
    return "";
  }
  var params = params_str.split('&');
  for (var i = 0; i < params.length; ++i) {
    var param = params[i];
    var pair = param.split('=');
    if (pair.length !== 2) {
      continue;
    }
    if (pair[0] === name) {
      return decodeURIComponent(pair[1]);
    }
  }
  return "";
}

/*
 * Checks whether the content of the local DB should be synchronized, and if it does,
 * sync it against the server and replaces the new content with the old one.
 * 
 * It's optional to force the synching of the DB by passing force=true.
 */
var check_sync_db = function(force) {
  if (force === undefined) {
    force = false;
  }
  var user = get_cookie('user');
  // If the DB was not refreshed in awhile (15 minutes), make sure we also 
  // read the content again.
  var last_sync = get_param('last_sync');
  var last_sync_user = get_param('last_sync_user');
  last_sync = (last_sync === '') ? 0 : parseInt(last_sync);
  var now = (new Date()).getTime(); // Millis.
  if (force || last_sync_user !== user || (last_sync + 15 * 60 * 1000) < now) {
    set_param('last_sync', now);
    set_param('last_sync_user', user);
    // Should resync.
    refresh_db_info();
  }
};

/*
 * Makes the element with the given ID to toggle between views in a
 * nice way.
 */
var toggle_item_by_id = function(item_id) {
  $('#' + item_id).animate({height: 'toggle'}, 'fast');
};

/*
 * Adds a new recipe item to the given element containing a list of
 * recipe.  Used for presenting lists of recipes according to type,
 * favorites and search.
 */
var add_recipes_to_elem = function(elem_id, recipes, refresh) {
  if (refresh === undefined) {
    refresh = false;
  }
  for (var i = 0; i < recipes.length; ++i) {
    var recipe = recipes[i];
    var item_id = 'recipe-item-div-inner-' + i;
    var recipe_src = recipe.img;
    if (recipe.img_bin !== undefined && recipe.img_bin !== '') {
      recipe_src = 'data:image;base64,' + recipe.img_bin;
    }
    var div_obj =
        '<div class="recipe-item-div" id="recipe-item-div-' + i + '">' + 
        ' <div class="recipe-item-title">' +
        '  <label onclick="toggle_item_by_id(\'' + item_id + '\')">' +
        '  ' + recipe.name + 
        '  </label>' +
        '  <img id="recipe-star-on-' + recipe.id + '" ' +
        (recipe.fav ? '' : 'hidden=""') +
        '       onclick="do_toggle_fav(' + recipe.id + ',' + refresh + ');" ' +
        '       src="img/star-on.png">' +
        '  <img id="recipe-star-off-' + recipe.id + '" ' +
        (recipe.fav ? 'hidden=""' : '') +
        '       onclick="do_toggle_fav(' + recipe.id + ',' + refresh + ');" ' +
        '       src="img/star-off.png">' +
        ' </div>' +
        ' <div id="' + item_id + '" hidden="" class="recipe-item-div-inner">' +
        '  <img src="' + recipe_src + '" class="recipe-item-img">' +
        '  <a data-role="button" href="view.html"' +
        '     onclick="navigate_with_params(\'view.html\',[[\'id\',' + recipe.id +']]);">' +
        '   View' +
        '  </a>' +
        '  <a data-role="button" href="edit.html" ' +
        '     onclick="navigate_with_params(\'edit.html\',[[\'id\',' + recipe.id+ ']]);">' +
        '    Edit' +
        '  </a>' +
        ' </div>' +
        '</div>';

    $('#' + elem_id).append($(div_obj));
  }
  $('#' + elem_id).trigger('create');
};

/*
 * Toggles the recipe "favorite" state.
 * Switches between on/off favorites modes of the star.
 */
var do_toggle_fav = function(id, refresh) {
  toggle_fav(id, function(on) {
    if (refresh) {
      jQuery.mobile.changePage(window.location.href, {
          allowSamePageTransition: true,
          transition: 'none',
          reloadPage: true });
    } else {
      if (on) {
        $('#recipe-star-off-' + id).hide();
        $('#recipe-star-on-' + id).show();
      } else {
        $('#recipe-star-off-' + id).show();
        $('#recipe-star-on-' + id).hide();
      }
    }
  });
};


/**
 * Invoked as the callback of the search query.
 * The argument is an array holding all the matching recipes.
 */
var do_search_callback = function(recipes) {
  $('div.recipe-item-div').remove();
  add_recipes_to_elem('search-result', recipes);
};

/*
 * Tried to runs the search according to the query given in the
 * search's fields (search type and content).
 */
var do_search = function() {
  var s_type = $('#search-by').val();
  var value = $('#search-input').val();
  if (value === "") {
    all_recipes(do_search_callback);
  } else {
    $('#search-input').attr('value', value);
    $('#search-by').attr('value', s_type);
    if (s_type === 'name') {
      all_by_name(value, do_search_callback);
    } else if (s_type === 'creator') {
      all_by_creator(value, do_search_callback);
    } else { // Date
      var date = Date.parse(value);  // NaN or in ms since EPOCH.
      if (!isNaN(date)) {
        all_by_date(date, do_search_callback);
      }
    }
  }
};

/*
 * Invoked when the search criteria changes.
 */
var search_criteria_changed = function() {
  $('#search-input').attr('value', '');
  var s_type = $('#search-by').val();
  var placeholder = '';
  switch (s_type) {
    case 'name': {
      placeholder = 'Name';
      break;
    }
    case 'creator': {
      placeholder = 'Creator';
      break;
    }
    case 'date': {
      placeholder = 'Date (MM/DD/YYYY)';
      break;
    }
  }
  $('#search-input').attr('placeholder', placeholder);
};

/*
 * Creates the checkboxes with the recipe's types, under the element 
 * with the given ID. Also controls whether the checkboxes are readonly 
 * or not (for edit / view modes).
 * 
 * When done, invokes the given callback.
 */
var create_types_checkboxes = function(parent_id, readonly, callback) {
  read_cuisines(function(cuisines) {
    var objs = '';
    for (var i = 0; i < cuisines.length; ++i) {
      var type = cuisines[i];
      var class_type = (i === 0) ? 'ui-first-child' : ((i === cuisines.length - 1) ? 'ui-last-child' : 'custom');
      var obj_id = parent_id + '-checkbox-' + type;
      var obj =
              '<input type="checkbox" name="' + obj_id + '" ' +
              (readonly ? 'onclick="this.checked = !this.checked;"' : '') +
              '       id="' + obj_id + '"/>' +
              '<label for="' + obj_id + '" class="' + class_type  + '">' + type + '</label>';
      objs += obj;
    }
    $('#' + parent_id).attr('data-role', 'controlgroup');
    $('#' + parent_id).append($(objs));

    callback();
  });
};

/*
 * Fills the recipe's values in the view page.
 */
var fill_recipe_values_for_view = function() {
  recipe_by_id(get_param('id'), function(recipe) {
    $('#recipe-name').attr('value', recipe.name);
    if (recipe.img_bin !== undefined && recipe.img_bin !== '') {
	  $('#recipe-img').attr('src', 'data:image;base64,' + recipe.img_bin);
	} else if (recipe.img !== undefined && recipe.img !== '') {
	  $('#recipe-img').attr('src', recipe.img);
	} else {
	  $('#recipe-img').attr('src', 'img/no_photo.jpg');
	}
    $('#recipe-date').attr('value', new Date(recipe.date).toDateString());
    $('#recipe-creator').attr('value', recipe.creator);
    for (var i = 0; i < recipe.types.length; ++i) {
      $('#recipe-types-fieldset-checkbox-' + recipe.types[i]).attr('checked', 'checked');
    }
    for (var i = 0; i < recipe.ingredients.length; ++i) {
      var ing = recipe.ingredients[i];
      $('#recipe-ingredients-list')
          .append($('<div class="ui-block-a">' + ing[0] + ' ' + ing[1] + '</div>'))
          .append($('<div class="ui-block-b">' + ing[2] + '</div>'));
    }
    $('#recipe-instructions').attr('value', recipe.instructions);
    $('#recipe-types-fieldset').trigger('create');
  });
};

/*
 * Fills the data of the chosen recipe in the edit.html input fields.
 */
var fill_edit_options = function() {
  recipe_by_id(get_param('id'), function(recipe) {
    all_ing_units(function(ing_units) {
      $('#edit-name').attr('value', recipe.name);
      $('#edit-recipe-image').on('click', function() {
        if (confirm('Would you like to change the image of this recipe?')) {
	      // Click on the camera capturing button.
		  $('#edit-image-capture').click();
	    }
	  });
	  $('#edit-image-capture-div').css('visibility', 'hidden');  // We must override JQM default behavior.
	  if (recipe.img !== undefined) {
	    $('#edit-image-url').attr('value', recipe.img);
      }
      $('#edit-date').attr('value', new Date(recipe.date).toDateString());
      $('#edit-creator').attr('value', recipe.creator);
      for (var i = 0; i < recipe.types.length; ++i) {
        $('#edit-types-fieldset-checkbox-' + recipe.types[i]).attr('checked', '');
      }
      add_ingredient.count = 0;
      for (var i = 0; i < recipe.ingredients.length; ++i) {
        var ingredient = recipe.ingredients[i];
        add_ingredient(ingredient, ing_units);
      }
      $('#edit-instructions').attr('value', recipe.instructions);
      $('#edit-types-fieldset').trigger('create');
    });
  });
};
/*
 * Removes an igredient from the list of ingredients in the 
 * edit recipe mode.
 */
var remove_ingredient = function(id) {
  if (!confirm("Remove the ingredient?")) {
    return;
  }
  $('#' + id).remove();
};

/*
 * Adds new line of ingredients.
 * Recieves the ingredient's values to add (in a form of a trio-tuple [amount, type, name]).
 * Should also receive all the valid ingredients unit (for creating the drop-down list).
 */
var add_ingredient = function(ingredient, ing_units) {
  // Make sure the list of ingredients is available.
  if (ing_units === undefined) {
    // We should read the unit types and call this method again with the result.
    all_ing_units(function(units) {
      add_ingredient(ingredient, units);      
    });
    return;
  }
  var preload = ingredient.length > 0;
  if (!preload) {
    ingredient = [0, '', ''];
  }
  var last_count = add_ingredient.count++;
  var ing_row_class = 'ingredients-row-' + last_count;
  var obj =
      '<tr id=\"' + ing_row_class + '\" class="ingredient-row">' +
      '  <td class="col-a">' +
      '    <button data-icon="delete" data-iconpos="notext" title="Delete"' +
      '            data-role="button"' +
      '            onclick="remove_ingredient(\'' + ing_row_class + '\');"></button>' +
      '  </td>' +
      '  <td class="col-b">' +
      '    <input type="number" min="0" value="' + ingredient[0] + '"/>' +
      '  </td>' +
      '  <td class="col-c">' +
      '    <select name="ingridient-type-' + last_count + '">' +
      '      <option value=""></option>';
  for (var i = 0; i < ing_units.length; ++i) {
    var ing_unit = ing_units[i];
    if (ingredient[1] === ing_unit) {
      obj += '<option value="' + ing_unit + '" selected="">' + ing_unit + '</option>';
    } else {
      obj += '<option value="' + ing_unit + '">' + ing_unit + '</option>';
    }
  }
  obj +=
      '    </select>' +
      '  </td>' +
      '  <td class="col-d">' +
      '      <input type="text" value="' + ingredient[2] + '" placeholder="Ingredient name"/>' +
      '  </td>' +
      '</tr>';

  $(obj).insertBefore($('#add_ingredient_button')).trigger('create');
};

/*
 * Fills the default values in the "new recipe" page.
 */
var fill_default_add_values = function() {
  $('#add-recipe-image').on('click', function() {
    if (confirm('Would you like to change the image of this recipe?')) {
	  // Click on the camera capturing button.
		$('#add-image-capture').click();
    }
  });
  $('#add-image-capture-div').css('visibility', 'hidden');  // We must override JQM default behavior.
  var type = get_param('type');
  if (type !== '') {
    $('#add-types-fieldset-checkbox-' + type).attr('checked', '');
  }
  $('#add-date').attr('value', new Date().toDateString());
  $('#add-types-fieldset').trigger('create');
};

var delete_recipe = function() {
  if (!confirm('Are you sure you want to delete the recipe?')) {
    return;
  }
  send_delete_request(get_param('id'), function(result) {
    if (result === 'OK') {
      navigate('type.html');
    } else {
      alert('Error - failed deleting: ' + result);
    }
  });
};

/*
 * Reads the details in the different fields of the edit/add recipe page,
 * calls the callback with a 'recipe' object containing all the data.
 * If any field contains an illegal value, warn about it, focus the cursor on
 * the improper field and return an 'undefined' value.
 */
var read_recipe_details = function(prefix, callback) {
  var recipe = {};
  // Read from the simple data fields.
  recipe.id = (prefix === 'edit' ? parseInt(get_param('id')) : -1);
  recipe.name = $('#' + prefix + '-name').val();
  if (recipe.name === undefined || recipe.name === '') {
    alert('Invalid name. Please insert a non-empty name for the recipe.');
    $('#' + prefix + '-name').focus();
    return undefined;
  }
  recipe.creator = $('#' + prefix + '-creator').val();
  if (recipe.creator === undefined || recipe.creator === '') {
    alert('Invalid creator. Please insert a non-empty creator for the recipe.');
    $('#' + prefix + '-creator').focus();
    return undefined;
  }
  recipe.instructions = $('#' + prefix + '-instructions').val();
  if (recipe.instructions === undefined || recipe.instructions === '') {
    alert('Invalid instructions. Please insert non-empty instructions for the recipe.');
    $('#' + prefix + '-instructions').focus();
    return undefined;
  }
  // Read and parse the date.
  var input_date = $('#' + prefix + '-date').val();
  recipe.date = Date.parse(input_date);
  if (isNaN(recipe.date)) {
    alert('Invalid date value: ' + input_date + '.\nPlease use MM/DD/YYYY format.');
    $('#' + prefix + '-date').focus();
    return undefined;
  }
  // Read the different cuisine types.
  recipe.types = [];
  var types_checkbox_list = $('#' + prefix + '-types-fieldset input');
  for (var i = 0; i < types_checkbox_list.length; ++i) {
    if (types_checkbox_list[i].checked) {
      recipe.types.push(types_checkbox_list[i].id.split('-')[4]);
    }
  }
  // Read the ingredients.
  recipe.ingredients = [];
  var stopped = false;
  $('.ingredient-row').each(function() {
    if (stopped) return;
    var ing_row = $(this);
    var amount = parseInt(ing_row.find('td:nth-child(2) input').val());
    if (isNaN(amount)) {
      alert('Invalid amount set.\nPlease use numbers only.');
      ing_row.find('td:nth-child(2) input').focus();
      stopped = true;
      return;
    }
    var ing_type = ing_row.find('td:nth-child(3) select').val();
    if (ing_type === null || ing_type === '') {
      alert('Invalid ingredient type.\nPlease select a type.');
      ing_row.find('td:nth-child(2) select').focus();
      stopped = true;
      return;
    }
    var ing_name = ing_row.find('td:nth-child(4) input').val();
    recipe.ingredients.push([parseInt(amount), ing_type, ing_name]);
  });
  if (stopped) {
    return undefined;
  }
  // Now, before proceeding, if the image was set using a local file,
  // we need to read the file first.
  file = undefined;
  if ($('#' + prefix + '-image-capture').val() == '' ||
	  document.getElementById(prefix + '-image-capture').files.length > 0) {
    file = document.getElementById(prefix + '-image-capture').files[0];
  }
  if (file !== undefined) {
	alert("Uploading the new image may take a few seconds..");
    fr = new FileReader();
    fr.onload = function(oFREvent) {
	  var result = oFREvent.target.result;
      recipe.img_bin = btoa(result);
	  recipe.img = undefined;
      callback(recipe);
    };
    fr.readAsBinaryString(file);
  } else {
	recipe.img = $('#' + prefix + '-image-url').val();
	console.log('IMAGE: ' + recipe.img);
	recipe.img_bin = undefined;
    callback(recipe);
  }
};

/*
 * Save the values of an edited recipe in "edit.html" page.
 * Alerts in case some fields are missing or have illegal values.
 */
var save_recipe = function() {
  read_recipe_details('edit', function (recipe) {
    if (recipe === undefined) {
      return;
	}
	update_recipe(recipe, function(new_id) {
	  if (new_id >= 0) {
	    set_param('id', new_id);
	    navigate('/view.html');
	  }
	});
  });
};

/*
 * Creates a new recipe from the fields of the "add.html" page.
 * Alerts in case some fields are missing or have illegal values.
 */
var add_recipe = function() {
  read_recipe_details('add', function(recipe) {
	if (recipe === undefined) {
	  return;
	}
	insert_recipe(recipe, function(new_id) {
	  if (new_id >= 0) {
	    set_param('id', new_id);
	    navigate('/view.html');
      }
    });
  });
};