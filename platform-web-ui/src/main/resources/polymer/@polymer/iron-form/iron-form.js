import '../polymer/polymer-legacy.js';
import '../iron-ajax/iron-ajax.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { dom } from '../polymer/lib/legacy/polymer.dom.js';
import { html } from '../polymer/lib/utils/html-tag.js';

/**
@license
Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at
http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
part of the polymer project is also subject to an additional IP rights grant
found at http://polymer.github.io/PATENTS.txt
*/

/**
`<iron-form>` is a wrapper around the HTML `<form>` element, that can
validate and submit both custom and native HTML elements. Note that this
is a breaking change from iron-form 1.0, which was a type extension.

It has two modes: if `allow-redirect` is true, then after the form submission
you will be redirected to the server response. Otherwise, if it is false, it
will use an `iron-ajax` element to submit the form contents to the server.

  Example:

    <iron-form>
      <form method="get" action="/form/handler">
        <input type="text" name="name" value="Batman">
        <input type="checkbox" name="donuts" checked> I like donuts<br>
        <paper-checkbox name="cheese" value="yes" checked></paper-checkbox>
      </form>
    </iron-form>

By default, a native `<button>` element will submit this form. However, if you
want to submit it from a custom element's click handler, you need to explicitly
call the `iron-form`'s `submit` method.

  Example (using `<paper-button>` for the form `<iron-form id="myForm">`):

    <paper-button raised on-click="submitForm">Submit</paper-button>

    function submitForm() {
      this.$.myForm.submit();
    }

  or (using `<button>` for a form in the same document):

    <button raised onclick="submitForm()">Submit</button>

    function submitForm() {
      document.getElementById('myForm').submit();
    }

If you are not using the `allow-redirect` mode, then you also have the option of
customizing the request sent to the server. To do so, you can listen to the
`iron-form-presubmit` event, and modify the form's
[`iron-ajax`](https://elements.polymer-project.org/elements/iron-ajax) object.
However, If you want to not use `iron-ajax` at all, you can cancel the event and
do your own custom submission:

  Example of modifying the request, but still using the build-in form
submission:

    form.addEventListener('iron-form-presubmit', function() {
      this.request.method = 'put';
      this.request.params['extraParam'] = 'someValue';
    });

  Example of bypassing the build-in form submission:

    form.addEventListener('iron-form-presubmit', function(event) {
      event.preventDefault();
      var firebase = new Firebase(form.getAttribute('action'));
      firebase.set(form.serializeForm());
    });

Note that if you're dynamically creating this element, it's mandatory that you
first create the contained `<form>` element and all its children, and only then
attach it to the `<iron-form>`:

  var wrapper = document.createElement('iron-form');
  var form = document.createElement('form');
  var input = document.createElement('input');
  form.appendChild(input);
  document.body.appendChild(wrapper);
  wrapper.appendChild(form);

@element iron-form
@hero hero.svg
@demo demo/index.html
*/
Polymer({
  _template: html`
    <style>
      :host {
        display: block;
      }
    </style>

    <!-- This form is used to collect the elements that should be submitted -->
    <slot></slot>

    <!-- This form is used for submission -->
    <form id="helper" action\$="[[action]]" method\$="[[method]]" enctype\$="[[enctype]]"></form>
`,

  is: 'iron-form',

  properties: {
    /*
     * Set this to true if you don't want the form to be submitted through an
     * ajax request, and you want the page to redirect to the action URL
     * after the form has been submitted.
     */
    allowRedirect: {type: Boolean, value: false},
    /**
     * HTTP request headers to send. See PolymerElements/iron-ajax for
     * more details. Only works when `allowRedirect` is false.
     */
    headers: {
      type: Object,
      value: function() {
        return {};
      }
    },
    /**
     * Set the `withCredentials` flag on the request. See
     * PolymerElements/iron-ajax for more details. Only works when
     * `allowRedirect` is false.
     */
    withCredentials: {type: Boolean, value: false},
  },

  /**
   * Fired if the form cannot be submitted because it's invalid.
   *
   * @event iron-form-invalid
   */

  /**
   * Fired after the form is submitted.
   *
   * @event iron-form-submit
   */

  /**
   * Fired before the form is submitted.
   *
   * @event iron-form-presubmit
   */

  /**
   * Fired after the form is reset.
   *
   * @event iron-form-reset
   */

  /**
   * Fired after the form is submitted and a response is received. An
   * IronRequestElement is included as the event.detail object.
   *
   * @event iron-form-response
   */

  /**
   * Fired after the form is submitted and an error is received. An
   * error message is included in event.detail.error and an
   * IronRequestElement is included in event.detail.request.
   *
   * @event iron-form-error
   */

  /**
   * @return {void}
   */
  attached: function() {
    // We might have been detached then re-attached.
    // Avoid searching again for the <form> if we already found it.
    if (this._form) {
      return;
    }
    // Search for the `<form>`, if we don't find it, observe for
    // mutations.
    this._form = dom(this).querySelector('form');
    if (this._form) {
      this._init();
      // Since some elements might not be upgraded yet at this time,
      // we won't be able to look into their shadowRoots for submittables.
      // We wait a tick and check again for any missing submittable default
      // values.
      this.async(this._saveInitialValues.bind(this), 1);
    } else {
      this._nodeObserver = dom(this).observeNodes(function(mutations) {
        for (var i = 0; i < mutations.addedNodes.length; i++) {
          if (mutations.addedNodes[i].tagName === 'FORM') {
            this._form = mutations.addedNodes[i];
            // At this point in time, all custom elements are expected
            // to be upgraded, hence we'll be able to traverse their
            // shadowRoots.
            this._init();
            dom(this).unobserveNodes(this._nodeObserver);
            this._nodeObserver = null;
          }
        }
      }.bind(this));
    }
  },

  /**
   * @return {void}
   */
  detached: function() {
    if (this._nodeObserver) {
      dom(this).unobserveNodes(this._nodeObserver);
      this._nodeObserver = null;
    }
  },

  _init: function() {
    this._form.addEventListener('submit', this.submit.bind(this));
    this._form.addEventListener('reset', this.reset.bind(this));

    // Save the initial values.
    this._defaults = this._defaults || new WeakMap();
    this._saveInitialValues();
  },

  /**
   * Saves the values of all form elements that will be used when resetting
   * the form. Initially called asynchronously on attach. Any time you
   * call this function, the previously saved values for a form element will
   * be overwritten.
   *
   * This function is useful if you are dynamically adding elements to
   * the form, or if your elements are asynchronously setting their values.
   * @return {void}
   */
  saveResetValues: function() {
    this._saveInitialValues(true);
  },

  /**
   * @param {boolean=} overwriteValues
   * @return {void}
   */
  _saveInitialValues: function(overwriteValues) {
    var nodes = this._getValidatableElements();
    for (var i = 0; i < nodes.length; i++) {
      var node = nodes[i];
      if (!this._defaults.has(node) || overwriteValues) {
        // Submittables are expected to have `value` property,
        // that's what gets serialized.
        var defaults = {value: node.value};
        if ('checked' in node) {
          defaults.checked = node.checked;
        }
        // In 1.x iron-form would reset `invalid`, so
        // keep it here for backwards compat.
        if ('invalid' in node) {
          defaults.invalid = node.invalid;
        }
        this._defaults.set(node, defaults);
      }
    }
  },

  /**
   * Validates all the required elements (custom and native) in the form.
   * @return {boolean} True if all the elements are valid.
   */
  validate: function() {
    // If you've called this before distribution happened, bail out.
    if (!this._form) {
      return false;
    }

    if (this._form.getAttribute('novalidate') === '')
      return true;

    // Start by making the form check the native elements it knows about.
    var valid = this._form.checkValidity();
    var elements = this._getValidatableElements();

    // Go through all the elements, and validate the custom ones.
    for (var el, i = 0; el = elements[i], i < elements.length; i++) {
      // This is weird to appease the compiler. We assume the custom element
      // has a validate() method, otherwise we can't check it.
      var validatable = /** @type {{validate: (function() : boolean)}} */ (el);
      if (validatable.validate) {
        valid = !!validatable.validate() && valid;
      }
    }
    return valid;
  },

  /**
   * Submits the form.
   *
   * @param {Event=} event
   * @return {void}
   */
  submit: function(event) {
    // We are not using this form for submission, so always cancel its event.
    if (event) {
      event.preventDefault();
    }

    // If you've called this before distribution happened, bail out.
    if (!this._form) {
      return;
    }

    if (!this.validate()) {
      this.fire('iron-form-invalid');
      return;
    }

    // Remove any existing children in the submission form (from a previous
    // submit).
    this.$.helper.textContent = '';

    var json = this.serializeForm();

    // If we want a redirect, submit the form natively.
    if (this.allowRedirect) {
      // If we're submitting the form natively, then create a hidden element for
      // each of the values.
      for (var element in json) {
        this.$.helper.appendChild(
            this._createHiddenElement(element, json[element]));
      }

      // Copy the original form attributes.
      this.$.helper.action = this._form.getAttribute('action');
      this.$.helper.method = this._form.getAttribute('method') || 'GET';
      this.$.helper.contentType = this._form.getAttribute('enctype') ||
          'application/x-www-form-urlencoded';

      this.$.helper.submit();
      this.fire('iron-form-submit');
    } else {
      this._makeAjaxRequest(json);
    }
  },

  /**
   * Resets the form to the default values.
   *
   * @param {Event=} event
   * @return {void}
   */
  reset: function(event) {
    // We are not using this form for submission, so always cancel its event.
    if (event)
      event.preventDefault();

    // If you've called this before distribution happened, bail out.
    if (!this._form) {
      return;
    }

    // Ensure the native form fired the `reset` event.
    // User might have bound `<button on-click="_resetIronForm">`, or directly
    // called `ironForm.reset()`. In these cases we want to first reset the
    // native form.
    if (!event || event.type !== 'reset' || event.target !== this._form) {
      this._form.reset();
      return;
    }

    // Load the initial values.
    var nodes = this._getValidatableElements();
    for (var i = 0; i < nodes.length; i++) {
      var node = nodes[i];
      if (this._defaults.has(node)) {
        var defaults = this._defaults.get(node);
        for (var propName in defaults) {
          node[propName] = defaults[propName];
        }
      }
    }

    this.fire('iron-form-reset');
  },

  /**
   * Serializes the form as will be used in submission. Note that `serialize`
   * is a Polymer reserved keyword, so calling `someIronForm`.serialize()`
   * will give you unexpected results.
   * @return {!Object<string, *>} An object containing name-value pairs for elements that
   *                  would be submitted.
   */
  serializeForm: function() {
    // Only elements that have a `name` and are not disabled are submittable.
    var elements = this._getSubmittableElements();
    var json = {};
    for (var i = 0; i < elements.length; i++) {
      var values = this._serializeElementValues(elements[i]);
      for (var v = 0; v < values.length; v++) {
        this._addSerializedElement(json, elements[i].name, values[v]);
      }
    }
    return json;
  },

  _handleFormResponse: function(event) {
    this.fire('iron-form-response', event.detail);
  },

  _handleFormError: function(event) {
    this.fire('iron-form-error', event.detail);
  },

  _makeAjaxRequest: function(json) {
    // Initialize the iron-ajax element if we haven't already.
    if (!this.request) {
      this.request = document.createElement('iron-ajax');
      this.request.addEventListener(
          'response', this._handleFormResponse.bind(this));
      this.request.addEventListener('error', this._handleFormError.bind(this));
    }

    // Native forms can also index elements magically by their name (can't make
    // this up if I tried) so we need to get the correct attributes, not the
    // elements with those names.
    this.request.url = this._form.getAttribute('action');
    this.request.method = this._form.getAttribute('method') || 'GET';
    this.request.contentType = this._form.getAttribute('enctype') ||
        'application/x-www-form-urlencoded';
    this.request.withCredentials = this.withCredentials;
    this.request.headers = this.headers;

    if (this._form.method.toUpperCase() === 'POST') {
      this.request.body = json;
    } else {
      this.request.params = json;
    }

    // Allow for a presubmit hook
    var event = this.fire('iron-form-presubmit', {}, {cancelable: true});
    if (!event.defaultPrevented) {
      this.request.generateRequest();
      this.fire('iron-form-submit', json);
    }
  },

  _getValidatableElements: function() {
    return this._findElements(
        this._form, true /* ignoreName */, false /* skipSlots */);
  },

  _getSubmittableElements: function() {
    return this._findElements(
        this._form, false /* ignoreName */, false /* skipSlots */);
  },

  /**
   * Traverse the parent element to find and add all submittable nodes to
   * `submittable`.
   * @param  {!Node} parent The parent node
   * @param  {!boolean} ignoreName  Whether the name of the submittable nodes should be disregarded
   * @param  {!boolean} skipSlots  Whether to skip traversing of slot elements
   * @param  {!Array<!Node>=} submittable Reference to the array of submittables
   * @return {!Array<!Node>}
   * @private
   */
  _findElements: function(parent, ignoreName, skipSlots, submittable) {
    submittable = submittable || [];
    var nodes = dom(parent).querySelectorAll('*');
    for (var i = 0; i < nodes.length; i++) {
      // An element is submittable if it is not disabled, and if it has a
      // name attribute.
      if (!skipSlots &&
          (nodes[i].localName === 'slot' || nodes[i].localName === 'content')) {
        this._searchSubmittableInSlot(submittable, nodes[i], ignoreName);
      } else {
        this._searchSubmittable(submittable, nodes[i], ignoreName);
      }
    }
    return submittable;
  },

  /**
   * Traverse the distributed nodes of a slot or content element
   * and add all submittable nodes to `submittable`.
   * @param  {!Array<!Node>} submittable Reference to the array of submittables
   * @param  {!Node} node The slot or content node
   * @param  {!boolean} ignoreName  Whether the name of the submittable nodes should be disregarded
   * @return {void}
   * @private
   */
  _searchSubmittableInSlot: function(submittable, node, ignoreName) {
    var assignedNodes = dom(node).getDistributedNodes();

    for (var i = 0; i < assignedNodes.length; i++) {
      if (assignedNodes[i].nodeType === Node.TEXT_NODE) {
        continue;
      }

      // Note: assignedNodes does not contain <slot> or <content> because
      // getDistributedNodes flattens the tree.
      this._searchSubmittable(submittable, assignedNodes[i], ignoreName);
      var nestedAssignedNodes = dom(assignedNodes[i]).querySelectorAll('*');
      for (var j = 0; j < nestedAssignedNodes.length; j++) {
        this._searchSubmittable(
            submittable, nestedAssignedNodes[j], ignoreName);
      }
    }
  },

  /**
   * Traverse the distributed nodes of a slot or content element
   * and add all submittable nodes to `submittable`.
   * @param  {!Array<!Node>} submittable Reference to the array of submittables
   * @param  {!Node} node The node to be
   * @param  {!boolean} ignoreName  Whether the name of the submittable nodes should be disregarded
   * @return {void}
   * @private
   */
  _searchSubmittable: function(submittable, node, ignoreName) {
    if (this._isSubmittable(node, ignoreName)) {
      submittable.push(node);
    } else if (node.root) {
      this._findElements(
          node.root, ignoreName, true /* skipSlots */, submittable);
    }
  },

  /**
   * An element is submittable if it is not disabled, and if it has a
   * 'name' attribute. If we ignore the name, check if is validatable.
   * This allows `_findElements` to decide if to explore an element's shadowRoot
   * or not: an element implementing `validate()` is considered validatable, and
   * we don't search for validatables in its shadowRoot.
   * @param {!Node} node
   * @param {!boolean} ignoreName
   * @return {boolean}
   * @private
   */
  _isSubmittable: function(node, ignoreName) {
    return (
        !node.disabled &&
        (ignoreName ? node.name || typeof node.validate === 'function' :
                      node.name));
  },

  _serializeElementValues: function(element) {
    // We will assume that every custom element that needs to be serialized
    // has a `value` property, and it contains the correct value.
    // The only weird one is an element that implements
    // IronCheckedElementBehaviour, in which case like the native checkbox/radio
    // button, it's only used when checked. For native elements, from
    // https://www.w3.org/TR/html5/forms.html#the-form-element. Native
    // submittable elements: button, input, keygen, object, select, textarea;
    // 1. We will skip `keygen and `object` for this iteration, and deal with
    // them if they're actually required.
    // 2. <button> and <textarea> have a `value` property, so they behave like
    //    the custom elements.
    // 3. <select> can have multiple options selected, in which case its
    //    `value` is incorrect, and we must use the values of each of its
    //    `selectedOptions`
    // 4. <input> can have a whole bunch of behaviours, so it's handled
    // separately.
    // 5. Buttons are hard. The button that was clicked to submit the form
    //    is the one who's name/value gets sent to the server.
    var tag = element.tagName.toLowerCase();
    if (tag === 'button' ||
        (tag === 'input' &&
         (element.type === 'submit' || element.type === 'reset'))) {
      return [];
    }

    if (tag === 'select') {
      return this._serializeSelectValues(element);
    } else if (tag === 'input') {
      return this._serializeInputValues(element);
    } else {
      if (element['_hasIronCheckedElementBehavior'] && !element.checked)
        return [];
      return [element.value];
    }
  },

  _serializeSelectValues: function(element) {
    var values = [];

    // A <select multiple> has an array of options, some of which can be
    // selected.
    for (var i = 0; i < element.options.length; i++) {
      if (element.options[i].selected) {
        values.push(element.options[i].value);
      }
    }
    return values;
  },

  _serializeInputValues: function(element) {
    // Most of the inputs use their 'value' attribute, with the exception
    // of radio buttons, checkboxes and file.
    var type = element.type.toLowerCase();

    // Don't do anything for unchecked checkboxes/radio buttons.
    // Don't do anything for file, since that requires a different request.
    if (((type === 'checkbox' || type === 'radio') && !element.checked) ||
        type === 'file') {
      return [];
    }
    return [element.value];
  },

  _createHiddenElement: function(name, value) {
    var input = document.createElement('input');
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', name);
    input.setAttribute('value', value);
    return input;
  },

  _addSerializedElement: function(json, name, value) {
    // If the name doesn't exist, add it. Otherwise, serialize it to
    // an array,
    if (json[name] === undefined) {
      json[name] = value;
    } else {
      if (!Array.isArray(json[name])) {
        json[name] = [json[name]];
      }
      json[name].push(value);
    }
  }
});
