/*
 * This file mocks the DB on the server side.
 *
 * Note: Since this is not a persistent solution, all the dynamic changes to
 *       the DB will be lost with every restart of the server.
 */

/* The different "tables" and "indexes". Will be populated in 'reset_db' method. */
var users = {};
var next_user_id = 0;
var recipes = {};
var next_recipe_id = 0;
var cuisines = [];
var ing_units = [];
var recipes_users = {};

/*
 * For exercise checking purposes, this method is exposed to allow the checker to
 * reset the data in the "DB" and have a clean start (like the server restarted).
 */
exports.reset_db = function(req, res) {
	/* The Users table */
	users = {
	  0: {
		id: 0,
		username: 'dor',
		email: 'dorgross@gmail.com',
		password: '123456'
	  },
	  1: { 
		id: 1,
		username: 'itamar',
		email: 'itamar.benady@gmail.com',
		password: 'qwerty'
	  },
	  2: {
		id: 2,
		username: 'checker',
		email: 'mobile.idc.2013@gmail.com',
		password: 'checkme'
	  }
	};
	/* Represents the ID for the next user (emulating the auto-increment feature of a real DB). */
	next_user_id = 3;

	/* The Recipes table (with 14 "example" recipes) */
	recipes = {
	  0: {
	    id: 0,
	    name: 'Ceaser Salad',
	    date: Date.parse('01/01/2000'), // MM/DD/YYYY
	    creator: 'Dor Gross',
	    ingredients: [[200, 'Grams', 'Chicken Breast'],
	                  [1, 'Units', 'Lettuce'],
	                  [2, 'Units', 'Tomato'],
	                  [50, 'Grams', 'Soup Croutons']],
	    types: ['Healthy','Meat'],
	    img: 'img/ceaser-salad.jpg',
	    instructions: 
	      'Grill the chicken breast. Chop the tomatos into small ' +
	      'pieces and wash the lettuce carefully. Slice the grilled ' +
	      'chicken breast into stripes and put everything in a bowl. ' +
	      'Spread the crutons on top everything and serve.'
	  },
	  1: {
	    id: 1,
	    name: 'Grilled Steak',
	    date: Date.parse('05/31/2012'), // MM/DD/YYYY
	    creator: 'Dor Gross',
	    ingredients: [[300, 'Grams', 'Entrecote'],
	                  [1, 'Teaspoons', 'Black Pepper']],
	    types: ['Meat'],
	    img: 'img/steak.jpg',
	    instructions: 
	      'Spread the black pepper over the steak and simply grill the staek...'
	  },
	  2: {
	    id: 2,
	    name: 'Strawberries and Cream',
	    date: Date.parse('04/05/1984'), // MM/DD/YYYY
	    creator: 'Dor Gross',
	    ingredients: [[100, 'CCs', 'Cream'],
	                  [1, 'Cups', 'Sugar'],
	                  [1, 'Packs', 'Strawberries']],
	    types: ['Dessert'],
	    img: 'img/straw-cream.jpg',
	    instructions: 
	      'Using a blander (or a kitchen aid), create whipped cream from the ' +
	      'cream and sugar. Then put the strawberries in the whipped cream or ' +
	      'on a plate next to it and serve.'
	  },
	  3: {
	    id: 3,
	    name: 'Omlette with Herbs',
	    date: Date.parse('08/09/2005'), // MM/DD/YYYY
	    creator: 'Itamar Benady',
	    ingredients: [[2, 'Units', 'Eggs'],
	                  [1, 'Bundles', 'Parsley'],
	                  [2, 'Units', 'Garlic Teeth']],
	    types: ['Healthy'],
	    img: 'img/omlette.jpg',
	    instructions: 
	      'Chop the parsley and the garlic teeth to small pieces. ' +
	      'Cook them on low fire for 3-4 minutes, then add the eggs ' +
	      'After scrambling them in a different bowl. Wait 5-6 minutes ' +
	      'and turn the omlette to the other side. Wait 5 more minutes ' +
	      'and it is ready.'
	  },
	  4: {
	    id: 4,
	    name: 'Fetuccini Alfredo',
	    date: Date.parse('05/05/2005'), // MM/DD/YYYY
	    creator: 'Itamar Benady',
	    ingredients: [[500, 'Grams', 'Fetuccini'],
	                  [200, 'CCs', 'Cooking Cream'],
	                  [50, 'Grams', 'Parmesan Cheese']],
	    types: ['Italian'],
	    img: 'img/alfredo.jpg',
	    instructions: 
	      'Put the fetuccini in bioling water (about 2 liters) and wait ' +
	      '8-10 minutes until the pasta is soft enough (but not too much!). ' +
	      'In a different pot, cook the cream until it starts boiling. ' +
	      'Spill the cream on the pasta, and grate the parmesan over it.'

	  },
	  5: {
	    id: 5,
	    name: 'Pot Thai-Style Rice Noodles',
	    date: Date.parse('05/05/2013'), // MM/DD/YYYY
	    creator: 'Erin Mist',
	    ingredients: [[2, 'Tablespoons', 'Cornstarch'],
	                  [1.5, 'Tablespoons', 'Water'],
	                  [6, 'Cups', 'Chicken Breast'],
	                  [2.5, 'Tablespoons', 'Soy Sauce'],
	                  [1, 'Tablespoons', 'Fish Sauce'],
	                  [1, 'Tablespoons', 'Rice Vinegar'],
	                  [1, 'Tablespoons', 'Garlic Sauce'],
	                  [2, 'Teaspoons', 'Vegetable Oil'],
	                  [2, 'Teaspoons', 'Minced Fresh Ginger Root'],
	                  [2, 'Units', 'Chopped Garlic'],
	                  [1, 'Teaspoons', 'Ground Coriander'],
	                  [1, 'Packages', 'Thick Rice Noodles'],
	                  [1, 'Cups', 'Sliced Zucchini'],
	                  [1, 'Cups', 'Sliced Red Bell Pepper'],
	                  [0.25, 'Cups', 'Crushed Peanuts'],
	                  [0.25, 'Cups', 'Chopped Fresh Cilantro']],
	    types: ['Asian', 'Healthy', 'Fish'],
	    img: 'img/pot-thai.jpg',
	    instructions:
	      'Stir cornstarch and water together in a small bowl until smooth. ' +
	      'Pour chicken broth into a large pot and stir cornstarch mixture, ' +
	      'soy sauce, fish sauce, rice vinegar, chile-garlic sauce, vegetable ' +
	      'oil, ginger, garlic, and coriander into broth. Cover and bring to a ' +
	      'boil. Place rice noodles in the boiling sauce, reduce heat to medium, ' +
	      'and simmer until noodles are tender, 5 to 10 minutes. Stir zucchini, ' +
	      'red bell pepper, and chicken into sauce. Bring back to a boil, cover, ' +
	      'and simmer until vegetables are just become tender, about 5 more minutes.' +
	      'Remove from heat and let stand, covered, for 5 minutes to thicken. ' +
	      'Serve garnished with crushed peanuts and cilantro.'
	  },
	  6: {
	    id: 6,
	    name: 'Citrus Ice Pops',
	    date: Date.parse('01/01/2013'), // MM/DD/YYYY
	    creator: 'Mew Tew',
	    ingredients: [[0.5, 'Cups', 'Grapefruit Juice'],
	                  [1, 'Cups', 'Orange Juice'],
	                  [0.25, 'Cups', 'Water'],
	                  [2, 'Tablespoons', 'Lemon Juice'],
	                  [1, 'Tablespoons', 'Sugar'],
	                  [200, 'CCs', 'Vanilla Yogurt'],
	                  [1, 'Cups', 'Chopped Strawberries']],
	    types: ['Dessert'],
	    img: 'img/citrus.jpg',
	    instructions:
	      'Place the grapefruit juice, orange juice, water, lemon juice, ' +
	      'and sugar in a saucepan over medium heat, and bring to a boil. ' +
	      'Cook, stirring, until the sugar has dissolved, about 5 minutes. ' +
	      'Remove from heat, and chill the mixture in the freezer until very ' +
	      'cold, about 10 minutes. Mix the vanilla yogurt with the ' +
	      'strawberries, and chill. Spoon a little of the cold juice mixture ' +
	      'into 6 ice pop molds, using about 1 1/2 tablespoon of mixture for ' +
	      'each mold. Place the molds into the freezer until the juice freezes, ' +
	      '30 minutes to 1 hour. Spoon about 1 tablespoon of the cold ' +
	      'strawberry yogurt into each ice pop, and top each pop with ' +
	      'the remaining citrus juice mixture. Freeze until fully hardened, ' +
	      '1 to 1 1/2 hours.'
	  },
	  7: {
	    id: 7,
	    name: 'Salmon with Dill',
	    date: Date.parse('10/17/2006'), // MM/DD/YYYY
	    creator: 'John Bragg',
	    ingredients: [[450, 'Grams', 'Salmon Fillets'],
	                  [0.25, 'Teaspoons', 'Salt'],
	                  [0.5, 'Teaspoons', 'Ground Black Pepper'],
	                  [1, 'Teaspoons', 'Onion Powder'],
	                  [1, 'Teaspoons', 'Dried Dill Weed'],
	                  [2, 'Tablespoons', 'Butter']],
	    types: ['Healthy', 'Fish'],
	    img: 'img/salmon.jpg',
	    instructions: 
	      'Preheat oven to 400 degrees F (200 degrees C). ' +
	      'Rinse salmon, and arrange in a 9x13 inch baking dish. Sprinkle ' +
	      'salt, pepper, onion powder, and dill over the fish. Place pieces ' +
	      'of butter evenly over the fish. Bake in preheated oven for 20 ' +
	      'to 25 minutes. Salmon is done when it flakes easily with a fork.'
	  },
	  8: {
	    id: 8,
	    name: 'Quick Gnocchi',
	    date: Date.parse('08/17/2005'), // MM/DD/YYYY
	    creator: 'Sandy Metzler',
	    ingredients: [[1, 'Cups', 'Dry Potato Flakes'],
	                  [1, 'Cups', 'Boiling Water'],
	                  [1, 'Units', 'Egg Beaten'],
	                  [1, 'Teaspoons', 'Salt'],
	                  [0.2, 'Teaspoons', 'Ground Black Pepper'],
	                  [1.5, 'Cups', 'All-Purpose Flour']],
	    types: ['Italian'],
	    img: 'img/gnocchi.jpg',
	    instructions: 
	      'Place potato flakes in a medium-size bowl. Pour in boiling ' +
	      'water; stir until blended. Let cool. Stir in egg, salt, and ' +
	      'pepper. Blend in enough flour to make a fairly stiff dough. Turn ' +
	      'dough out on a well floured board. Knead lightly. Divide dough ' +
	      'in half. Shape each half into a long thin roll, the thickness ' +
	      'of a breadstick. With a knife dipped in flour, cut into bite-size ' +
	      'pieces. Place a few gnocchi in boiling water. As the gnocchi rise ' +
	      'to the top of the pot, remove them with a slotted spoon. Repeat ' +
	      'until all are cooked.'
	  },
	  9: {
	    id: 9,
	    name: 'Wiener Schnitzel',
	    date: Date.parse('04/16/2005'), // MM/DD/YYYY
	    creator: 'Carolin',
	    ingredients: [[800, 'Grams', 'Veal'],
	                  [1, 'Cups', 'All-Purpose Flour'],
	                  [4, 'Units', 'Egg'],
	                  [1, 'Tablespoons', 'Vegetable Oil'],
	                  [4, 'Cups', 'Bread Crumbs'],
	                  [125, 'CCs', 'Oil for Frying']],
	    types: ['Meat'],
	    img: 'img/schnitzel.jpg',
	    instructions: 
	      'Cut the veal into steaks, about as thick as your finger. Dredge ' +
	      'in flour. In a shallow dish, beat the eggs with 1 tablespoon oil, ' +
	      'salt and pepper. Coat the veal with egg mixture, then with bread ' +
	      'crumbs. Heat 1/4 cup oil in a heavy skillet over medium heat. ' +
	      'Fry veal until golden brown, about 5 minutes on each side.'
	  },
	  10: {
	    id: 10,
	    name: 'Homemade Fish Fingers ',
	    date: Date.parse('04/10/2010'), // MM/DD/YYYY
	    creator: 'BBC Recipes',
	    ingredients: [[1, 'Units', 'Egg beaten'],
	                  [85, 'Grams', 'Bread Crumbs'],
	                  [1, 'Units', 'Lemon'],
	                  [1, 'Teaspoons', 'Dried Oregano'],
	                  [1, 'Tablespoons', 'Olive Oil'],
	                  [400, 'Grams', 'Sliced Skinless White Fish'],
	                  [4, 'Tablespoons', 'Mayonnaise'],
	                  [140, 'Grams', 'Cooked Peas'],
	                  [100, 'Grams', 'Spinach']],
	    types: ['Fish', 'Asian'],
	    img: 'img/fish-fingers.jpg',
	    instructions: 
	      'Heat oven to 200C/180C fan/gas 6. Pour the beaten egg into a ' +
	      'shallow dish. Tip the breadcrumbs onto a plate. Mix the lemon ' +
	      'zest into the breadcrumbs along with the oregano and some ' +
	      'salt and pepper. Brush a non-stick baking sheet with half the ' +
	      'oil. Dip the fish strips into the egg, then roll them in the ' +
	      'breadcrumbs. Transfer to the baking sheet and bake for 20 ' +
	      'mins until golden. Meanwhile, mix the mayo with a squeeze of ' +
	      'lemon juice. Toss the spinach leaves and peas with a squeeze ' +
	      'more lemon juice and the remaining oil. Serve the fish fingers ' +
	      'with the spinach and peas and a spoonful of the lemony mayo.'
	  },
	  11: {
	    id: 11,
	    name: 'Pizza with Olives',
	    date: Date.parse('01/01/1970'), // MM/DD/YYYY
	    creator: 'Don Giovanni',
	    ingredients: [[1, 'Units', 'Cell Phone']],
	    types: ['Italian'],
	    img: 'img/pizza-olive.jpg',
	    instructions: 
	      'Common.. That\'s pizza... Just pick up the phone and call some ' +
	      'delivery service...'
	  },
	  12: {
	    id: 12,
	    name: 'Capresa',
	    date: Date.parse('09/15/2011'), // MM/DD/YYYY
	    creator: 'Itamar Benady',
	    ingredients: [[6, 'Units', 'Basil Leaves'],
	                  [3, 'Units', 'Tomatos'],
	                  [200, 'Grams', 'Mozzarella Cheese'],
	                  [1, 'Tablespoons', 'Olive Oil']],
	    types: ['Italian', 'Healthy'],
	    img: 'img/capresa.jpg',
	    instructions: 
	      'Slice the tomatos and the cheeze to circles. On the serving plate, ' +
	      'put a piece of tomato, on top of it, put the cheese slice and on ' +
	      'top of it put a basil leaf and pour a little bit oil on it.'
	  },
	  13: {
	    id: 13,
	    name: 'Watermelon',
	    date: Date.parse('06/07/1986'), // MM/DD/YYYY
	    creator: 'Dor Gross',
	    ingredients: [[1, 'Units', 'Watermelon']],
	    types: ['Healthy', 'Dessert'],
	    img: 'img/watermelon.jpg',
	    instructions: 
	      'Slice the watermelon and serve on a warm day.'
	  },
	  14: {
	    id: 14,
	    name: 'Azriely Chocolate Cake',
	    date: Date.parse('07/30/2013'), // MM/DD/YYYY
	    creator: 'Google Tel Aviv Chef',
	    ingredients: [[1, 'Grams', 'Secret Ingredient'],
	                  [1, 'KGs', 'Tasty Chocolate'],
	                  [1, 'Units', 'Super creative chef']],
	    types: ['Dessert'],
	    img: 'img/azrieli-cake.jpg',
	    instructions: 
	      'Find the chef of Google Tel Aviv and ask him/her to make it for you...'
	  },
	  15: {
	    id: 15,
	    name: 'Itamar & Lee Wedding Cake',
	    date: Date.parse('05/05/2013'), // MM/DD/YYYY
	    creator: 'Disney',
	    ingredients: [[1, 'Units', 'Mini Mouse'],
	                  [1, 'Units', 'Mickey Mouse'],
	                  [1, 'Units', 'Chocolate Cake']],
	    types: ['Dessert'],
	    img: 'img/itamar-wedding-cake.jpg',
	    instructions: 
	      'Put Mickey on top of the cake, then put Mini in his arms. Serve cold after the Chupa.'
	  }
	};

	/* Represents the ID for the next recipe (emulating the auto-increment feature of a real DB). */
	next_recipe_id = 14;

	/* The Cuisines table */
	cuisines = ["Meat", "Healthy", "Italian", "Asian", "Dessert", "Fish"];

	/* The ingredients valid units */
	ing_units = [
	    /* Weight */ "Pounds", "Onces", "Grams", "KGs", 
	    /* Length */ "Feet", "Inches", "Millimeters", "Centimeters",
	    /* Volume */ "Cups", "CCs", "Gallons", "Liters", "Tablespoons", "Teaspoons",
	    /* Others */  "Pinch", "Drops", "Units", "Packages", "Bundles", "Packs"];
	ing_units.sort();

	/*
	 * The Recipes-Users table (binding a recipe to a user).
	 * Maps from the user's ID to all the recipes (IDs) the user has access to.
	 * Each access also defines whether the recipe is in the 'favorites' list of the
	 * the user (true) or not (false).
	 */
	recipes_users = {
		0: {0:  true,
			1:  true,
			2:  false,
			3:  true,
			5:  false,
			8:  true,
			9:  false,
			10: true,
			11: true,
			12: false,
			13: true,
			14: true,
			15: false },
		1: {0:  true,
		    1:  true,
			2:  false,
			3:  false,
			4:  true,
			5:  false,
			6:  true,
			7:  false,
			8:  true,
			13: true,
			14: true,
			15: true },
		2: {0:  false,
			1:  false,
			2:  true,
			3:  true,
			4:  false,
			5:  false,
			6:  true,
			8:  false,
			10: false,
			12: true,
			13: true,
			14: true,
			15: false }};
	if (res !== undefined) {
		// The reset came from an HTTP reqeust. Reply...
		res.send('Server DB was reset successfully!');
	}
}
// Make sure the "DB" starts with the data.
exports.reset_db();

/*
 * A utility function that extracts the user ID from HTTP request.
 * Returns -1 if the user ID is missing or illegal.
 */
var extract_user_id = function(req) {
  var user_id = req.headers['recipelab-user'];
  if (user_id === undefined) {
	return -1;
  }
  user_id = parseInt(user_id);
  if (isNaN(user_id)) {
	return -1;
  }
  return user_id;
};

/*
 * A utility function that helps extracting the user ID from the HTTP request.
 * If the user ID is valid, returns it. Otherwise, return 'undefined' and if the 
 * response object is defined, also sending '401 - unauthorized' error back.
 */
var validate_user_id = function(req, res) {
  var user_id = req.headers['recipelab-user'];
  if (user_id === undefined) {
	if (res !== undefined) {
	  res.statusCode = 401;
	  res.send('Unauthorized access - insufficient privilages.');
	}
	return undefined;
  }
  user_id = parseInt(user_id);
  if (isNaN(user_id)) {
	if (res !== undefined) {
	  res.statusCode = 401;
	  res.send('Unauthorized access - insufficient privilages.');
	}
	return undefined;
  }
  return user_id;
};

/*
 * A utility function that checks whether an element is in an array.
 */
var in_array = function(elem, arr) {
  for (var i = 0; i < arr.length; ++i) {
    if (arr[i] === elem) {
	  return true;
    }
  }
  return false;
};

/*
 * A utility function that performs a shallow copy of a recipe and adds the
 * favorite attribute to it. Used in order to return a list of recipes without
 * changing the actual stored values (in the mocked DB).
 */
var augment_recipe_with_fav = function(recipe, fav) {
  var new_recipe = {};
  new_recipe.id = recipe.id;
  new_recipe.name = recipe.name;
  new_recipe.date = recipe.date;
  new_recipe.creator = recipe.creator;
  new_recipe.ingredients = recipe.ingredients;
  new_recipe.types = recipe.types;
  new_recipe.img = recipe.img;
  new_recipe.img_bin = recipe.img_bin;
  new_recipe.instructions = recipe.instructions;
  new_recipe.fav = fav;
  return new_recipe;
};

/*
 * Performs the login of a user.
 * Recieves the login HTTP request and validates its data against the "DB".
 */
exports.login = function(req, res) {
  var user = req.param('user');
  var pass = req.param('pass');
  for (var id in users) {
	if (users[id].username === user && users[id].password === pass) {
	  return res.send(id);
	}
  };
  return res.send(-1);
};

/*
 * Performs the signup of a new user.
 * Recieves the signup HTTP request, validates it and insert the new data into the "DB".
 */
exports.signup = function(req, res) {
  var new_user_details = req.body;
  for (var user_id in users) {
    if (users[user_id].username === new_user_details.username) {
      return res.send(JSON.stringify(-1));
    } else if (users[user_id].email === new_user_details.email) {
      return res.send(JSON.stringify(-2));
	} else if (users[user_id].password === undefined) {
	  return res.send(JSON.stringify(-3));
	}
  }
  var new_user_id = next_user_id++;
  new_user_details.id = new_user_id;
  users[new_user_id] = new_user_details;
  // Let each user start with some guaranteed recipes.
  recipes_users[new_user_id] = {0: false, 1: false, 2: false, 3: false, 4: false, 5: false};

  return res.send(JSON.stringify(new_user_id));
};

/*
 * Internal helper functions. Returns the list of users allowed to view a certain recipe.
 */
var allowed_users = function(recipe_id) {
  allowed = [];
  for (var user_id in recipes_users) {
	if (recipes_users[user_id][recipe_id] !== undefined) {
		allowed.push(parseInt(user_id));
	}
  }
  return allowed;
};

/*
 * Returns all the valid cuisines.
 */
exports.all_cuisines = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  return res.send(JSON.stringify(cuisines));
};

/*
 * Returns the requested recipe (assuming existing and authorized for the user).
 */
exports.recipe_by_id = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  var id = parseInt(req.param('id'));
  var recipe_ids = recipes_users[user];
  if (recipe_ids === undefined) {
	return res.send('');
  }
  var allowed = false;
  if (recipe_ids[id] === undefined) {
	return res.send('');
  }
  var recipe = augment_recipe_with_fav(recipes[id], recipes_users[user][id]);
  return res.send(JSON.stringify([recipe]));
};

/*
 * Internal helper for retrieving all the recipe objects (not just IDs) of the requesting user.
 * Returned value is an array of recipe objects.
 */
var all_recipes_internal = function(req) {
  if ((user = validate_user_id(req, undefined)) === undefined) {
	return [];
  }
  if (recipes_users[user] === undefined) {
	return [];
  }
  var recipes_id = Object.keys(recipes_users[user]);
  if (recipes_id === undefined) {
	return [];
  }
  var ret = [];
  for (var i = 0; i < recipes_id.length; ++i) {
	var id = recipes_id[i];
	var orig_recipe = recipes[id];
    var recipe = augment_recipe_with_fav(orig_recipe, recipes_users[user][id]);
    ret.push(recipe);
  }
  return ret;
};

/*
 * Returns all the reipces associated with the requesting user.
 */
exports.all_recipes = function(req, res) {
  return res.send(JSON.stringify(all_recipes_internal(req)));
};

/*
 * Returns all the reipces of a certain cuisine associated with the requesting user.
 */
exports.all_by_cuisine = function(req, res) {
  var value = req.param('cuisine');
  var context = { cuisine: value };
  var matcher = function(recipe) {
	for (var i = 0; i < recipe.types.length; ++i) {
		if (recipe.types[i] === this.cuisine) {
			return true;
		}
	}
	return false;
  };
  return res.send(JSON.stringify(all_recipes_internal(req).filter(matcher, context)));
};

/*
 * Returns all the favorite reipces of the requesting user.
 */
exports.all_favorites = function(req, res) {
  var matcher = function(recipe) {
    return recipe.fav;
  };
  return res.send(JSON.stringify(all_recipes_internal(req).filter(matcher)));
};

/*
 * Internal helper for filterring recipes by their name.
 * Assumes the context contains a 'value' variable.
 */
var filter_by_name = function(recipe) {
  return recipe.name.toLowerCase().search(this.value) >= 0;
};

/*
 * Internal helper for filterring recipes by their creator.
 * Assumes the context contains a 'value' variable.
 */
var filter_by_creator = function(recipe) {
  return recipe.creator.toLowerCase().search(this.value) >= 0;
};

/*
 * Internal helper for filterring recipes by their date.
 * Assumes the context contains a 'value' variable.
 */
var filter_by_date = function(recipe) {
  var val = parseInt(this.value);
  if (val === undefined) { 
    return undefined;
  }
  return recipe.date > val;
};

/*
 * Returns all the reipces associated with the requesting user which match the
 * given search creterias (as the parameters of the request).
 */
exports.search = function(req, res) {
  var type = req.param('type');
  var val = req.param('val');
  if (type === undefined || val === undefined) {
	return res.send('');
  }
  var filter_func = undefined;
  if (type === 'name') {
	filter_func = filter_by_name;
  } else if (type === 'date') {
	filter_func = filter_by_date;
  } else if (type === 'creator') {
	filter_func = filter_by_creator;
  }
  if (filter_func === undefined) {
	return res.send('');
  }
  var context = { value: val };
  return res.send(JSON.stringify(all_recipes_internal(req).filter(filter_func, context)));
};

/*
 * Deletes the requested recipes from the user associated recipes.
 * If the user is the only one with permissions for the deleted recipe, also deleting
 * the recipe from the "DB".
 */
exports.delete_recipe = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  var id = parseInt(req.param('id'));
  var allowed = allowed_users(id);
  if (!in_array(user, allowed)) {
    res.statusCode = 401;
    return res.send('Unauthorized access - insufficient privilages.');
  }
  // Check if we need to remove the whole recipe.
  // We only remove the recipe if there are no more users that are allowed to see it,
  // and if its ID is greater than 5 (to allow every register user to start with some
  // sample recipes).
  if (allowed.length === 1 && id > 5) {
    delete recipes[id];
  }
  // Remove the recipe from the user's list.
  delete recipes_users[user][id];

  return res.send(JSON.stringify('OK'));
};

/*
 * Updates the requested recipes.
 */
exports.update_recipe = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send(-1);
  }
  var recipe = req.body;
  var allowed = allowed_users(recipe.id);
  if (!in_array(user, allowed)) {
    res.statusCode = 401;
    return res.send('Unauthorized access - insufficient privilages.');
  }
  // Keep the old image if the current recipe doesn't have one.
  if ((recipe.img === undefined || recipe.img === '') &&
      (recipe.img_bin === undefined || recipe.img_bin === '')) {
    recipe.img = recipes[recipe.id].img;
    recipe.img_bin = recipes[recipe.id].img_bin;
  }
  // Check if we can modify the recipe in-place or need to create a new 
  // version of it (for the current user). We don't allow one user to 
  // affect the recipes of the others (and since evert new users starts
  // with the basic 6 recipes, we don't allow editing them as well).
  if (allowed.length === 1 && recipe.id > 5) {
	// This user is the only seeing this recipe. Replace the old with the new.
	recipes[recipe.id] = recipe;
	return res.send(JSON.stringify(recipe.id));
  }
  // We need to create a new version. First, remove the recipe from the
  // list of allowed recipes for the user.
  var fav = recipes_users[user][recipe.id];
  delete recipes_users[user][recipe.id];
  // Now, create a new recipe-id, and put our new recipe in its place.
  var new_recipe_id = next_recipe_id++;
  recipe.id = new_recipe_id;
  recipes[new_recipe_id] = recipe;
  recipes_users[user][new_recipe_id] = fav;

  return res.send(JSON.stringify(new_recipe_id));
};

/*
 * Inserts a new recipe into the "DB" and associating it with the user.
 */
exports.insert_recipe = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send(-1);
  }
  var recipe = req.body;
  var new_recipe_id = next_recipe_id++;
  recipe.id = new_recipe_id;
  // If the recipe doesn't have any image linked to it, use the 'no_photo' one.
  if ((recipe.img === undefined || recipe.img === '') &&
      (recipe.img_bin === undefined || recipe.img_bin === '')) {
    recipe.img = 'img/no_photo.jpg';
  }
  recipes[new_recipe_id] = recipe;
  recipes_users[user][new_recipe_id] = false;

  return res.send(JSON.stringify(new_recipe_id));
};

/*
 * Setting a specific value for the faovrite flag of the recipe associated with the user.
 * Returns (for sanity) the set value.
 */
exports.set_fav = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send(JSON.stringify(-1));
  }
  // Make sure the user has access to this recipe.
  var id = parseInt(req.param('id'));
  var fav = parseInt(req.param('fav'));
  if (recipes_users[user][id] === undefined) {
    return res.send(JSON.stringify(-2));
  }
  recipes_users[user][id] = (fav === 1);
  return res.send(JSON.stringify(fav));
};

/*
 * Toggles the value of the favorite value of the recipe, and returns the
 * new value.
 */
exports.toggle_fav = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send(JSON.stringify(-1));
  }
  // Make sure the user has access to this recipe.
  var id = parseInt(req.param('id'));
  if (recipes_users[user][id] === undefined) {
    return res.send(JSON.stringify(-2));
  }
  recipes_users[user][id] = !recipes_users[user][id];
  return res.send(JSON.stringify(recipes_users[user][id] ? 1 : 0));
};

/*
 * Returns all the ingredient units available in the system.
 */
exports.all_units = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  return res.send(JSON.stringify(ing_units));	
};

/*
 * Returns the favorite state (true/false) of all the recipes associated with the user.
 */
exports.fav_states = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  return res.send(JSON.stringify(recipes_users[user]));
};

/*
 * Returns an object holding all the user current data.  Used especially for refreshing the
 * cahced data stored locally in the mobile DB.
 * The returned object will have the following format:
 *  {
 *    cuisines: [ cuisine1, cuisine2, ... ]
 *    ing_units: [ unti1, unti2, ... ]
 *    recipes: {
 *      id1: { ... }
 *      id2: { ... }
 *      ...
 *    }
 *  }
 */
exports.refresh_data = function(req, res) {
  if ((user = validate_user_id(req, res)) === undefined) {
	return res.send('');
  }
  response = {
	cuisines_types: cuisines,
	ing_unit_types: ing_units,
	recipes: all_recipes_internal(req)
  };
  return res.send(JSON.stringify(response));
};
