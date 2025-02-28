import '../polymer/polymer-legacy.js';
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

/*
`iron-overlay-backdrop` is a backdrop used by `Polymer.IronOverlayBehavior`. It
should be a singleton.

### Styling

The following custom properties and mixins are available for styling.

Custom property | Description | Default
-------------------------------------------|------------------------|---------
`--iron-overlay-backdrop-background-color` | Backdrop background color | #000
`--iron-overlay-backdrop-opacity`          | Backdrop opacity | 0.6
`--iron-overlay-backdrop`                  | Mixin applied to `iron-overlay-backdrop`.                      | {}
`--iron-overlay-backdrop-opened`           | Mixin applied to `iron-overlay-backdrop` when it is displayed | {}
*/
Polymer({
  /** @override */
  _template: html`
    <style>
      :host {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: var(--iron-overlay-backdrop-background-color, #000);
        opacity: 0;
        transition: opacity 0.2s;
        pointer-events: none;
        @apply --iron-overlay-backdrop;
      }

      :host(.opened) {
        opacity: var(--iron-overlay-backdrop-opacity, 0.6);
        pointer-events: auto;
        @apply --iron-overlay-backdrop-opened;
      }
    </style>

    <slot></slot>
`,

  is: 'iron-overlay-backdrop',

  properties: {

    /**
     * Returns true if the backdrop is opened.
     */
    opened: {
      reflectToAttribute: true,
      type: Boolean,
      value: false,
      observer: '_openedChanged',
    }

  },

  listeners: {
    'transitionend': '_onTransitionend',
  },

  /** @override */
  created: function() {
    // Used to cancel previous requestAnimationFrame calls when opened changes.
    this.__openedRaf = null;
  },

  /** @override */
  attached: function() {
    this.opened && this._openedChanged(this.opened);
  },

  /**
   * Appends the backdrop to document body if needed.
   */
  prepare: function() {
    if (this.opened && !this.parentNode) {
      dom(document.body).appendChild(this);
    }
  },

  /**
   * Shows the backdrop.
   */
  open: function() {
    this.opened = true;
  },

  /**
   * Hides the backdrop.
   */
  close: function() {
    this.opened = false;
  },

  /**
   * Removes the backdrop from document body if needed.
   */
  complete: function() {
    if (!this.opened && this.parentNode === document.body) {
      dom(this.parentNode).removeChild(this);
    }
  },

  _onTransitionend: function(event) {
    if (event && event.target === this) {
      this.complete();
    }
  },

  /**
   * @param {boolean} opened
   * @private
   */
  _openedChanged: function(opened) {
    if (opened) {
      // Auto-attach.
      this.prepare();
    } else {
      // Animation might be disabled via the mixin or opacity custom property.
      // If it is disabled in other ways, it's up to the user to call complete.
      var cs = window.getComputedStyle(this);
      if (cs.transitionDuration === '0s' || cs.opacity == 0) {
        this.complete();
      }
    }

    if (!this.isAttached) {
      return;
    }

    // Always cancel previous requestAnimationFrame.
    if (this.__openedRaf) {
      window.cancelAnimationFrame(this.__openedRaf);
      this.__openedRaf = null;
    }
    // Force relayout to ensure proper transitions.
    this.scrollTop = this.scrollTop;
    this.__openedRaf = window.requestAnimationFrame(function() {
      this.__openedRaf = null;
      this.toggleClass('opened', this.opened);
    }.bind(this));
  }
});
