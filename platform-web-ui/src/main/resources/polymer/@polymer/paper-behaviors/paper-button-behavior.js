import '../polymer/polymer-legacy.js';
import { IronButtonStateImpl, IronButtonState } from '../iron-behaviors/iron-button-state.js';
import { IronControlState } from '../iron-behaviors/iron-control-state.js';
import { PaperRippleBehavior } from './paper-ripple-behavior.js';

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

/** @polymerBehavior PaperButtonBehavior */
const PaperButtonBehaviorImpl = {
  properties: {
    /**
     * The z-depth of this element, from 0-5. Setting to 0 will remove the
     * shadow, and each increasing number greater than 0 will be "deeper"
     * than the last.
     *
     * @attribute elevation
     * @type number
     * @default 1
     */
    elevation: {type: Number, reflectToAttribute: true, readOnly: true}
  },

  observers: [
    '_calculateElevation(focused, disabled, active, pressed, receivedFocusFromKeyboard)',
    '_computeKeyboardClass(receivedFocusFromKeyboard)'
  ],

  hostAttributes: {role: 'button', tabindex: '0', animated: true},

  _calculateElevation: function() {
    var e = 1;
    if (this.disabled) {
      e = 0;
    } else if (this.active || this.pressed) {
      e = 4;
    } else if (this.receivedFocusFromKeyboard) {
      e = 3;
    }
    this._setElevation(e);
  },

  _computeKeyboardClass: function(receivedFocusFromKeyboard) {
    this.toggleClass('keyboard-focus', receivedFocusFromKeyboard);
  },

  /**
   * In addition to `IronButtonState` behavior, when space key goes down,
   * create a ripple down effect.
   *
   * @param {!KeyboardEvent} event .
   */
  _spaceKeyDownHandler: function(event) {
    IronButtonStateImpl._spaceKeyDownHandler.call(this, event);
    // Ensure that there is at most one ripple when the space key is held down.
    if (this.hasRipple() && this.getRipple().ripples.length < 1) {
      this._ripple.uiDownAction();
    }
  },

  /**
   * In addition to `IronButtonState` behavior, when space key goes up,
   * create a ripple up effect.
   *
   * @param {!KeyboardEvent} event .
   */
  _spaceKeyUpHandler: function(event) {
    IronButtonStateImpl._spaceKeyUpHandler.call(this, event);
    if (this.hasRipple()) {
      this._ripple.uiUpAction();
    }
  }
};

/** @polymerBehavior */
const PaperButtonBehavior = [
  IronButtonState,
  IronControlState,
  PaperRippleBehavior,
  PaperButtonBehaviorImpl
];

export { PaperButtonBehavior, PaperButtonBehaviorImpl };
