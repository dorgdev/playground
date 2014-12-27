var express = require('express');
var db = require('./mock-db');

// Some basic configurations.
var app = express();
app.configure(function () {
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
});

// Configure the wiring.
app.use('/public', express.static(__dirname + '/public'));
app.use('/', express.static(__dirname + '/public'));
// Login/Signup related operation.
app.get('/login', db.login);
app.post('/signup', db.signup);
// Request for ALL the data a client should hold.
app.get('/refresh_data', db.refresh_data);
// Get all the recipes/units.
app.get('/recipes', db.all_recipes);
app.get('/units', db.all_units);
// A single recipe oriented operation.
app.get('/recipe', db.recipe_by_id);
app.delete('/recipe', db.delete_recipe);
app.put('/recipe', db.update_recipe);
app.post('/recipe', db.insert_recipe);
// Cuisines related opearations.
app.get('/cuisines', db.all_cuisines);
app.get('/cuisine', db.all_by_cuisine);
// Favorite state operations.
app.get('/set_fav', db.set_fav);
app.get('/toggle_fav', db.toggle_fav);
app.get('/fav_states', db.fav_states);
app.get('/favs', db.all_favorites);
// Getting all the recipes based on some search criteria.
app.get('/search',db.search);

// Implementing a BACKDOOR for the checker which resets the data held in
// the server to its starting point (immitating a server restart).
app.get('/checker_reset', db.reset_db);

// The app runs in the cloud on a port which may be decided by the hosting
// environment. Make sure we follow its requested PORT number.
var port = process.env.VCAP_APP_PORT || 3000

// Start the app.
app.listen(port);