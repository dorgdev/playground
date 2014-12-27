/*
 * Checks whether a certain field, which requires to be filled,
 * is currently empty. Alerts the user and focus the cursor on
 * the field if the field is empty.
 * 
 * Checks the field according to the given ID, and alerts using the
 * given name (assuming ID is not necessarily readable).
 */
function valid_not_empty(id, name) {
  if ($("#" + id).val() === "") {
    alert("Missing " + name + ".");
    $("#" + id).focus().select().css("border-color", "red");
    return false;
  }
  return true;
};

/*
 * Validates tbe given value to make sure it's a valid email address.
 */
function validEmail(email){	
    var pattern = /^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\.([a-zA-Z])+([a-zA-Z])+/;
    return pattern.test(email);
}

/*
 * Used to determine whether a user's password given matches the
 * actual password. Invokes the callback with the ID of the logged in user,
 * or -1 in case of a filure.
 */
function check_login(user, password, callback) {
  var request = new XMLHttpRequest();
  if (request) {
		request.open('GET','/login?user=' + user + '&pass=' + password, true);
    request.onreadystatechange = function() {
      if (request.readyState === 4) {
        if (request.status === 200) {
            var id = parseInt(request.responseText);
            callback(id);
        } else {
          var msg = 'Login failed (status=' + request.status + ').';
          alert(msg);
          console.log(msg);
        }
      }
		};
    request.send(null);
  } else {
    var msg = 'XMLHttpRequest not supprted.';
    alert(msg);
    console.log(msg);
  }
};

/*
 * Performs the actual signup against the server. If the operation is successful, set
 * the new user id and automatically loggin into the system.
 */
function perform_signup(user, email, password, callback) {
  var request = new XMLHttpRequest();
  if (request) {
		request.open('POST','/signup', true);
    signup_details = {
      username: user,
      email: email,
      password: password
    };
    request.onreadystatechange = function() {
      if (request.readyState === 4) {
        if (request.status === 200) {
            var id = parseInt(request.responseText);
            callback(id);
        } else {
          var msg = 'Singup failed (status=' + request.status + ').';
          alert(msg);
          console.log(msg);
        }
      }
		};
    request.setRequestHeader("Content-Type", "application/json");
    request.send(JSON.stringify(signup_details));
  } else {
    var msg = 'XMLHttpRequest not supprted.';
    alert(msg);
    console.log(msg);
  }
};

/*
 * Called when the 'login' button is pressed, i.e., the user
 * tries to log in into the application.
 * Checks the given cradentials and alerts in case of mismatching
 * user name or password.
 */
function login_pressed() {
  // First, validate the input entered in the text fields.
  if (!valid_not_empty("username", "username") ||
      !valid_not_empty("pwd", "password")) {
    return;
  }
  var username = $("#username").val();
  var password = $("#pwd").val();
  // Check whether the user's cradentials are correct.
  check_login(username, password, function(id) {
    if (id >= 0) {
      set_cookie('user', id);
      reroute_if_logged_in();
    } else {
      alert("Invalid username or password!");
      $("#username").focus().select();
    }
  });
};

/*
 * Called when the user submits the signup form.
 * Checks that all the fields are filled with legal values and
 * that all constraints hold, and perform the signup operation
 */
function submit_pressed() {
  // First, validate the input fields.
  if (!valid_not_empty("username", "username") ||
      !valid_not_empty("email", "e-mail") ||
      !valid_not_empty("con_email", "e-mail confirmation") ||
      !valid_not_empty("pwd", "password") ||
      !valid_not_empty("con_pwd", "password confirmation")) {
    return;
  }
  var username = $("#username").val();
  var email = $("#email").val();
  var confirm_email = $("#con_email").val();
  var password = $("#pwd").val();
  var confirm_password = $("#con_pwd").val();
  if (email !== confirm_email) {
    alert("E-mail fields don't match.");
    $("#email").focus().select();
    return;
  }
  if (!validEmail(email)) {
    alert("Inavlid E-mail format.");
    $("#email").focus().select();
    return;
  }
  if (password.length < 6) {
    alert("Password too short, please use 6 characters or more.");
    $("#pwd").focus().select();
    return;
  }
  if (password !== confirm_password) {
    alert("Password fields don't match.");
    $("#pwd").focus().select();
    return;
  }
  if (!$("#terms:checked").val()) {
    alert("Please accept the terms of service before proceeding.");
    return;
  }
  // We can proceed. Sign the user in the DB.
  perform_signup(username, email, password, function(id) {
    if (id >= 0) {
      alert("Welcome to RecipeLab!!!");
      set_cookie('user', id);
      reroute_if_logged_in();
    } else if (id === -1) {
      alert("Username already taken. Please choose a different one.");
      $("#username").focus().select();
    } else if (id === -2) {
      alert("A user with this email already taken. Please choose a different email.");
      $("#email").focus().select();
    } else {
      alert("Invalid password. Please enter a non-empty password.");
      $("#password").focus().select();
    }
  });
};

/*
 * Applies a certain mode for the login/submit pages. Valid values are:
 *  1. "login":  shows the login buttons and fields.
 *  2. "signup": shows the sign up buttons and fields.
 */
function apply_mode(mode) {
  if (mode === "login") {
    $(".only_login").show();
    $(".only_signup").hide();
  } else if (mode === "signup") {
    $(".only_signup").show();
    $(".only_login").hide();
  } else {
    alert("Unknown form mode!");
  }
  $(".input_field").css("border-color", "#99bbee").val("");
};

/*
 * Sets the different behaviors once the login page finishes loading.
 */
function login_page_loaded() {
  $("#login_btn").click(login_pressed);
  $("#submit").click(submit_pressed);
  // Set the default non-emptiness behavior of the text fields.
  var border_reset = function(field) {
    $(field).change(function() {
      $(field).css("border-color", "#99bbee");
    });
  };
  border_reset("#username");
  border_reset("#email");
  border_reset("#con_email");
  border_reset("#pwd");
  border_reset("#con_pwd");
  // Set the signup and cancel (signup) buttons behavior.
  $("#signup").click(function() {
    apply_mode("signup");
  });
  $("#cancel").click(function() {
    apply_mode("login");
  });
  // Make the whole term's item clickable.
  $("#terms_item").click(function() {
    $("#terms").val(!$("#terms").val());
  });
  apply_mode("login");
};