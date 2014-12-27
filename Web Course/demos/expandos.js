

// make sure we've got namespace
if (typeof wd == "undefined") {
  var wd = {};
}

/**
 * Courtesy of some dude with a blog named ddiaz.
 *
 */
wd.getElementsByClass = function(searchClass,node,tag) {
	var classElements = new Array();
	if ( node == null )
		node = document;
	if ( tag == null )
		tag = '*';
	var els = node.getElementsByTagName(tag);
	var elsLen = els.length;
	var pattern = new RegExp("(^|\\\\s)"+searchClass+"(\\\\s|$)");
	for (i = 0, j = 0; i < elsLen; i++) {
		if ( pattern.test(els[i].className) ) {
			classElements[j] = els[i];
			j++;
		}
	}
	return classElements;
};

/**
 * Typical cross-browser add event listener code.
 * @param {Element} el
 * @param {string} type
 * @param {Function} fn
 */
wd.addEventListener = function(el, type, fn) { 
  if (el.addEventListener) { 
    el.addEventListener(type, fn, false); 
    return true; 
  } else if (el.attachEvent) { 
    var r = el.attachEvent("on" + type, fn); 
    return r; 
  } else { 
    return false; 
  } 
};

// Add/remove/has class functions from http://snipplr.com/view/3561/addclass-removeclass-hasclass/

/**
 * Returns true if the element has the given class, false otherwise.
 * @param {Element} ele
 * @param {string} cls
 * @return {Boolean}
 */
wd.hasClass = function(ele,cls) {
  return ele.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'));
}

/**
 * Adds a class name to the element.
 * @param {Element} ele
 * @param {string} cls
 */
wd.addClass = function(ele,cls) {
  if (!wd.hasClass(ele,cls)) ele.className += " "+cls;
}

/**
 * Completely removes a class name from the element.
 * @param {Element} ele
 * @param {string} cls
 */
wd.removeClass = function(ele,cls) {
  if (wd.hasClass(ele,cls)) {
    var reg = new RegExp('(\\s|^)'+cls+'(\\s|$)');
    ele.className=ele.className.replace(reg,' ');
  }
}

/**
 * function scope bind
 */
Function.prototype.bind = function(obj) {
  var method = this, temp = function() {
    return method.apply(obj, arguments);
  };
  return temp;
}; 

/**
 * Expando class
 * @param {Element} el
 * @constructor
 */
wd.expando = function(el) {
  
  /**
   * @type {Element}
   * @private
   */
  this.el_ = el;
  
  /**
   * @type {Element}
   * @private
   */
  this.title_ = el.getElementsByTagName('h2')[0];
  
  /**
   * @type {Element}
   * @private
   */
  this.content_ = el.getElementsByTagName('ol')[0];
  
  // Initializes element with a class for cursor: pointer since it's now
  // an 'active' widget. Note that this is done by the javascript since
  // without javascript you could not get the expando effect - having the
  // cursor set to be a pointer when it wouldn't do anything would be
  // a bad user experience.
  wd.addClass(this.el_, 'wd-expando-on');
  
  // Prepends an element into the title to use indicate toggle state
  var titleText = this.title_.innerHTML;
  this.title_.innerHTML = '';
  this.state_ = document.createElement('div');
  this.title_.appendChild(this.state_);
  this.title_.appendChild(document.createTextNode(titleText));
  
  // Adds the event listener for title clicks
  wd.addEventListener(this.title_, 'click', this.clickHandler_.bind(this));
};

/**
 * Handles clicks on the expando title, toggling content display.
 * @param {Event} e
 */
wd.expando.prototype.clickHandler_ = function(e) {
  if (wd.hasClass(this.el_, 'wd-expando-on')) {
    wd.removeClass(this.el_, 'wd-expando-on');
    wd.addClass(this.el_, 'wd-expando-off');
  } else {
    wd.removeClass(this.el_, 'wd-expando-off');
    wd.addClass(this.el_, 'wd-expando-on');
  }
};

/**
 * Functional code to create the expandos.
 */
wd.expandos = wd.getElementsByClass('wd-expando');
for (var i = 0, expandoEl; expandoEl = wd.expandos[i]; i++) {
  new wd.expando(expandoEl);
}

