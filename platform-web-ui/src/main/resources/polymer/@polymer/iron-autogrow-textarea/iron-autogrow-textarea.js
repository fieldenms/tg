import '../polymer/polymer-legacy.js';
import '../iron-flex-layout/iron-flex-layout.js';
import { IronControlState } from '../iron-behaviors/iron-control-state.js';
import { IronValidatableBehavior } from '../iron-validatable-behavior/iron-validatable-behavior.js';
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
`iron-autogrow-textarea` is an element containing a textarea that grows in
height as more lines of input are entered. Unless an explicit height or the
`maxRows` property is set, it will never scroll.

Example:

    <iron-autogrow-textarea></iron-autogrow-textarea>

### Styling

The following custom properties and mixins are available for styling:

Custom property | Description | Default
----------------|-------------|----------
`--iron-autogrow-textarea` | Mixin applied to the textarea | `{}`
`--iron-autogrow-textarea-placeholder` | Mixin applied to the textarea placeholder | `{}`

@demo demo/index.html
*/
Polymer({
  /** @override */
  _template: html`
    <style>
      :host {
        display: inline-block;
        position: relative;
        width: 400px;
        border: 1px solid;
        padding: 2px;
        -moz-appearance: textarea;
        -webkit-appearance: textarea;
        overflow: hidden;
      }

      .mirror-text {
        visibility: hidden;
        word-wrap: break-word;
        @apply --iron-autogrow-textarea;
      }

      .fit {
        @apply --layout-fit;
      }

      textarea {
        position: relative;
        outline: none;
        border: none;
        resize: none;
        background: inherit;
        color: inherit;
        /* see comments in template */
        width: 100%;
        height: 100%;
        font-size: inherit;
        font-family: inherit;
        line-height: inherit;
        text-align: inherit;
        @apply --iron-autogrow-textarea;
      }

      textarea::-webkit-input-placeholder {
        @apply --iron-autogrow-textarea-placeholder;
      }

      textarea:-moz-placeholder {
        @apply --iron-autogrow-textarea-placeholder;
      }

      textarea::-moz-placeholder {
        @apply --iron-autogrow-textarea-placeholder;
      }

      textarea:-ms-input-placeholder {
        @apply --iron-autogrow-textarea-placeholder;
      }
    </style>

    <!-- the mirror sizes the input/textarea so it grows with typing -->
    <!-- use &#160; instead &nbsp; of to allow this element to be used in XHTML -->
    <div id="mirror" class="mirror-text" aria-hidden="true">&nbsp;</div>

    <!-- size the input/textarea with a div, because the textarea has intrinsic size in ff -->
    <div class="textarea-container fit">
      <textarea id="textarea" name$="[[name]]" aria-label$="[[label]]" autocomplete$="[[autocomplete]]" autofocus$="[[autofocus]]" autocapitalize$="[[autocapitalize]]" inputmode$="[[inputmode]]" placeholder$="[[placeholder]]" readonly$="[[readonly]]" required$="[[required]]" disabled$="[[disabled]]" rows$="[[rows]]" minlength$="[[minlength]]" maxlength$="[[maxlength]]"></textarea>
    </div>
`,

  is: 'iron-autogrow-textarea',
  behaviors: [IronValidatableBehavior, IronControlState],

  properties: {
    /**
     * Use this property instead of `bind-value` for two-way data binding.
     * @type {string|number}
     */
    value: {observer: '_valueChanged', type: String, notify: true},

    /**
     * This property is deprecated, and just mirrors `value`. Use `value`
     * instead.
     * @type {string|number}
     */
    bindValue: {observer: '_bindValueChanged', type: String, notify: true},

    /**
     * The initial number of rows.
     *
     * @attribute rows
     * @type number
     * @default 1
     */
    rows: {type: Number, value: 1, observer: '_updateCached'},

    /**
     * The maximum number of rows this element can grow to until it
     * scrolls. 0 means no maximum.
     *
     * @attribute maxRows
     * @type number
     * @default 0
     */
    maxRows: {type: Number, value: 0, observer: '_updateCached'},

    /**
     * Bound to the textarea's `autocomplete` attribute.
     */
    autocomplete: {type: String, value: 'off'},

    /**
     * Bound to the textarea's `autofocus` attribute.
     *
     * @type {!boolean}
     */
    autofocus: {type: Boolean, value: false},

    /**
     * Bound to the textarea's `autocapitalize` attribute.
     */
    autocapitalize: {type: String, value: 'none'},

    /**
     * Bound to the textarea's `inputmode` attribute.
     */
    inputmode: {type: String},

    /**
     * Bound to the textarea's `placeholder` attribute.
     */
    placeholder: {type: String},

    /**
     * Bound to the textarea's `readonly` attribute.
     */
    readonly: {type: String},

    /**
     * Set to true to mark the textarea as required.
     */
    required: {type: Boolean},

    /**
     * The minimum length of the input value.
     */
    minlength: {type: Number},

    /**
     * The maximum length of the input value.
     */
    maxlength: {type: Number},

    /**
     * Bound to the textarea's `aria-label` attribute.
     */
    label: {type: String}

  },

  listeners: {'input': '_onInput'},

  /**
   * Returns the underlying textarea.
   * @return {!HTMLTextAreaElement}
   */
  get textarea() {
    return /** @type {!HTMLTextAreaElement} */ (this.$.textarea);
  },

  /**
   * Returns textarea's selection start.
   * @return {number}
   */
  get selectionStart() {
    return this.$.textarea.selectionStart;
  },

  /**
   * Returns textarea's selection end.
   * @return {number}
   */
  get selectionEnd() {
    return this.$.textarea.selectionEnd;
  },

  /**
   * Sets the textarea's selection start.
   */
  set selectionStart(value) {
    this.$.textarea.selectionStart = value;
  },

  /**
   * Sets the textarea's selection end.
   */
  set selectionEnd(value) {
    this.$.textarea.selectionEnd = value;
  },

  /** @override */
  attached: function() {
    /* iOS has an arbitrary left margin of 3px that isn't present
     * in any other browser, and means that the paper-textarea's cursor
     * overlaps the label.
     * See https://github.com/PolymerElements/paper-input/issues/468.
     */
    var IS_IOS = navigator.userAgent.match(/iP(?:[oa]d|hone)/) &&
        !navigator.userAgent.match(/OS 1[3456789]/);
    if (IS_IOS) {
      this.$.textarea.style.marginLeft = '-3px';
    }
  },

  /**
   * Returns true if `value` is valid. The validator provided in `validator`
   * will be used first, if it exists; otherwise, the `textarea`'s validity
   * is used.
   * @return {boolean} True if the value is valid.
   */
  validate: function() {
    // Use the nested input's native validity.
    var valid = this.$.textarea.validity.valid;

    // Only do extra checking if the browser thought this was valid.
    if (valid) {
      // Empty, required input is invalid
      if (this.required && this.value === '') {
        valid = false;
      } else if (this.hasValidator()) {
        valid = IronValidatableBehavior.validate.call(this, this.value);
      }
    }

    this.invalid = !valid;
    this.fire('iron-input-validate');
    return valid;
  },

  _bindValueChanged: function(bindValue) {
    this.value = bindValue;
  },

  _valueChanged: function(value) {
    var textarea = this.textarea;
    if (!textarea) {
      return;
    }

    // If the bindValue changed manually, then we need to also update
    // the underlying textarea's value. Otherwise this change was probably
    // generated from the _onInput handler, and the two values are already
    // the same.
    if (textarea.value !== value) {
      textarea.value = !(value || value === 0) ? '' : value;
    }

    this.bindValue = value;
    this.$.mirror.innerHTML = this._valueForMirror();

    // Manually notify because we don't want to notify until after setting
    // value.
    this.fire('bind-value-changed', {value: this.bindValue});
  },

  _onInput: function(event) {
    var eventPath = dom(event).path;
    this.value = eventPath ? eventPath[0].value : event.target.value;
  },

  _constrain: function(tokens) {
    var _tokens;
    tokens = tokens || [''];
    // Enforce the min and max heights for a multiline input to avoid
    // measurement
    if (this.maxRows > 0 && tokens.length > this.maxRows) {
      _tokens = tokens.slice(0, this.maxRows);
    } else {
      _tokens = tokens.slice(0);
    }
    while (this.rows > 0 && _tokens.length < this.rows) {
      _tokens.push('');
    }
    // Use &#160; instead &nbsp; of to allow this element to be used in XHTML.
    return _tokens.join('<br/>') + '&#160;';
  },

  _valueForMirror: function() {
    var input = this.textarea;
    if (!input) {
      return;
    }
    this.tokens = (input && input.value) ? input.value.replace(/&/gm, '&amp;')
                                               .replace(/"/gm, '&quot;')
                                               .replace(/'/gm, '&#39;')
                                               .replace(/</gm, '&lt;')
                                               .replace(/>/gm, '&gt;')
                                               .split('\n') :
                                           [''];
    return this._constrain(this.tokens);
  },

  _updateCached: function() {
    this.$.mirror.innerHTML = this._constrain(this.tokens);
  }
});
