import '../polymer/polymer-legacy.js';
import { IronA11yKeysBehavior } from '../iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronMultiSelectableBehavior, IronMultiSelectableBehaviorImpl } from '../iron-selector/iron-multi-selectable.js';
import { IronSelectableBehavior } from '../iron-selector/iron-selectable.js';
import { dom } from '../polymer/lib/legacy/polymer.dom.js';

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
 * `IronMenuBehavior` implements accessible menu behavior.
 *
 * @demo demo/index.html
 * @polymerBehavior IronMenuBehavior
 */
const IronMenuBehaviorImpl = {

  properties: {

    /**
     * Returns the currently focused item.
     * @type {?Object}
     */
    focusedItem:
        {observer: '_focusedItemChanged', readOnly: true, type: Object},

    /**
     * The attribute to use on menu items to look up the item title. Typing the
     * first letter of an item when the menu is open focuses that item. If
     * unset, `textContent` will be used.
     */
    attrForItemTitle: {type: String},

    /**
     * @type {boolean}
     */
    disabled: {
      type: Boolean,
      value: false,
      observer: '_disabledChanged',
    },
  },

  /**
   * The list of keys has been taken from
   * https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/getModifierState
   * @private
   */
  _MODIFIER_KEYS: [
    'Alt',
    'AltGraph',
    'CapsLock',
    'Control',
    'Fn',
    'FnLock',
    'Hyper',
    'Meta',
    'NumLock',
    'OS',
    'ScrollLock',
    'Shift',
    'Super',
    'Symbol',
    'SymbolLock'
  ],

  /** @private */
  _SEARCH_RESET_TIMEOUT_MS: 1000,

  /** @private */
  _previousTabIndex: 0,

  hostAttributes: {
    'role': 'menu',
  },

  observers: ['_updateMultiselectable(multi)'],

  listeners: {
    'focus': '_onFocus',
    'keydown': '_onKeydown',
    'iron-items-changed': '_onIronItemsChanged'
  },

  /**
   * @type {!Object}
   */
  keyBindings: {
    'up': '_onUpKey',
    'down': '_onDownKey',
    'esc': '_onEscKey',
    'shift+tab:keydown': '_onShiftTabDown'
  },

  attached: function() {
    this._resetTabindices();
  },

  /**
   * Selects the given value. If the `multi` property is true, then the selected
   * state of the `value` will be toggled; otherwise the `value` will be
   * selected.
   *
   * @param {string|number} value the value to select.
   */
  select: function(value) {
    // Cancel automatically focusing a default item if the menu received focus
    // through a user action selecting a particular item.
    if (this._defaultFocusAsync) {
      this.cancelAsync(this._defaultFocusAsync);
      this._defaultFocusAsync = null;
    }
    var item = this._valueToItem(value);
    if (item && item.hasAttribute('disabled'))
      return;
    this._setFocusedItem(item);
    IronMultiSelectableBehaviorImpl.select.apply(this, arguments);
  },

  /**
   * Resets all tabindex attributes to the appropriate value based on the
   * current selection state. The appropriate value is `0` (focusable) for
   * the default selected item, and `-1` (not keyboard focusable) for all
   * other items. Also sets the correct initial values for aria-selected
   * attribute, true for default selected item and false for others.
   */
  _resetTabindices: function() {
    var firstSelectedItem = this.multi ?
        (this.selectedItems && this.selectedItems[0]) :
        this.selectedItem;

    this.items.forEach(function(item) {
      item.setAttribute('tabindex', item === firstSelectedItem ? '0' : '-1');
      item.setAttribute('aria-selected', this._selection.isSelected(item));
    }, this);
  },

  /**
   * Sets appropriate ARIA based on whether or not the menu is meant to be
   * multi-selectable.
   *
   * @param {boolean} multi True if the menu should be multi-selectable.
   */
  _updateMultiselectable: function(multi) {
    if (multi) {
      this.setAttribute('aria-multiselectable', 'true');
    } else {
      this.removeAttribute('aria-multiselectable');
    }
  },

  /**
   * Given a KeyboardEvent, this method will focus the appropriate item in the
   * menu (if there is a relevant item, and it is possible to focus it).
   *
   * @param {KeyboardEvent} event A KeyboardEvent.
   */
  _focusWithKeyboardEvent: function(event) {
    // Make sure that the key pressed is not a modifier key.
    // getModifierState is not being used, as it is not available in Safari
    // earlier than 10.0.2 (https://trac.webkit.org/changeset/206725/webkit)
    if (this._MODIFIER_KEYS.indexOf(event.key) !== -1)
      return;

    this.cancelDebouncer('_clearSearchText');

    var searchText = this._searchText || '';
    var key = event.key && event.key.length == 1 ?
        event.key :
        String.fromCharCode(event.keyCode);
    searchText += key.toLocaleLowerCase();

    var searchLength = searchText.length;

    for (var i = 0, item; item = this.items[i]; i++) {
      if (item.hasAttribute('disabled')) {
        continue;
      }

      var attr = this.attrForItemTitle || 'textContent';
      var title = (item[attr] || item.getAttribute(attr) || '').trim();

      if (title.length < searchLength) {
        continue;
      }

      if (title.slice(0, searchLength).toLocaleLowerCase() == searchText) {
        this._setFocusedItem(item);
        break;
      }
    }

    this._searchText = searchText;
    this.debounce(
        '_clearSearchText',
        this._clearSearchText,
        this._SEARCH_RESET_TIMEOUT_MS);
  },

  _clearSearchText: function() {
    this._searchText = '';
  },

  /**
   * Focuses the previous item (relative to the currently focused item) in the
   * menu, disabled items will be skipped.
   * Loop until length + 1 to handle case of single item in menu.
   */
  _focusPrevious: function() {
    var length = this.items.length;
    var curFocusIndex = Number(this.indexOf(this.focusedItem));

    for (var i = 1; i < length + 1; i++) {
      var item = this.items[(curFocusIndex - i + length) % length];
      if (!item.hasAttribute('disabled')) {
        var owner = dom(item).getOwnerRoot() || document;
        this._setFocusedItem(item);

        // Focus might not have worked, if the element was hidden or not
        // focusable. In that case, try again.
        if (dom(owner).activeElement == item) {
          return;
        }
      }
    }
  },

  /**
   * Focuses the next item (relative to the currently focused item) in the
   * menu, disabled items will be skipped.
   * Loop until length + 1 to handle case of single item in menu.
   */
  _focusNext: function() {
    var length = this.items.length;
    var curFocusIndex = Number(this.indexOf(this.focusedItem));

    for (var i = 1; i < length + 1; i++) {
      var item = this.items[(curFocusIndex + i) % length];
      if (!item.hasAttribute('disabled')) {
        var owner = dom(item).getOwnerRoot() || document;
        this._setFocusedItem(item);

        // Focus might not have worked, if the element was hidden or not
        // focusable. In that case, try again.
        if (dom(owner).activeElement == item) {
          return;
        }
      }
    }
  },

  /**
   * Mutates items in the menu based on provided selection details, so that
   * all items correctly reflect selection state.
   *
   * @param {Element} item An item in the menu.
   * @param {boolean} isSelected True if the item should be shown in a
   * selected state, otherwise false.
   */
  _applySelection: function(item, isSelected) {
    if (isSelected) {
      item.setAttribute('aria-selected', 'true');
    } else {
      item.setAttribute('aria-selected', 'false');
    }
    IronSelectableBehavior._applySelection.apply(this, arguments);
  },

  /**
   * Discretely updates tabindex values among menu items as the focused item
   * changes.
   *
   * @param {Element} focusedItem The element that is currently focused.
   * @param {?Element} old The last element that was considered focused, if
   * applicable.
   */
  _focusedItemChanged: function(focusedItem, old) {
    old && old.setAttribute('tabindex', '-1');
    if (focusedItem && !focusedItem.hasAttribute('disabled') &&
        !this.disabled) {
      focusedItem.setAttribute('tabindex', '0');
      focusedItem.focus();
    }
  },

  /**
   * A handler that responds to mutation changes related to the list of items
   * in the menu.
   *
   * @param {CustomEvent} event An event containing mutation records as its
   * detail.
   */
  _onIronItemsChanged: function(event) {
    if (event.detail.addedNodes.length) {
      this._resetTabindices();
    }
  },

  /**
   * Handler that is called when a shift+tab keypress is detected by the menu.
   *
   * @param {CustomEvent} event A key combination event.
   */
  _onShiftTabDown: function(event) {
    var oldTabIndex = this.getAttribute('tabindex');

    IronMenuBehaviorImpl._shiftTabPressed = true;

    this._setFocusedItem(null);

    this.setAttribute('tabindex', '-1');

    this.async(function() {
      this.setAttribute('tabindex', oldTabIndex);
      IronMenuBehaviorImpl._shiftTabPressed = false;
      // NOTE(cdata): polymer/polymer#1305
    }, 1);
  },

  /**
   * Handler that is called when the menu receives focus.
   *
   * @param {FocusEvent} event A focus event.
   */
  _onFocus: function(event) {
    if (IronMenuBehaviorImpl._shiftTabPressed) {
      // do not focus the menu itself
      return;
    }

    // Do not focus the selected tab if the deepest target is part of the
    // menu element's local DOM and is focusable.
    var rootTarget =
        /** @type {?HTMLElement} */ (dom(event).rootTarget);
    if (rootTarget !== this && typeof rootTarget.tabIndex !== 'undefined' &&
        !this.isLightDescendant(rootTarget)) {
      return;
    }

    // clear the cached focus item
    this._defaultFocusAsync = this.async(function() {
      // focus the selected item when the menu receives focus, or the first item
      // if no item is selected
      var firstSelectedItem = this.multi ?
          (this.selectedItems && this.selectedItems[0]) :
          this.selectedItem;

      this._setFocusedItem(null);

      if (firstSelectedItem) {
        this._setFocusedItem(firstSelectedItem);
      } else if (this.items[0]) {
        // We find the first none-disabled item (if one exists)
        this._focusNext();
      }
    });
  },

  /**
   * Handler that is called when the up key is pressed.
   *
   * @param {CustomEvent} event A key combination event.
   */
  _onUpKey: function(event) {
    // up and down arrows moves the focus
    this._focusPrevious();
    event.detail.keyboardEvent.preventDefault();
  },

  /**
   * Handler that is called when the down key is pressed.
   *
   * @param {CustomEvent} event A key combination event.
   */
  _onDownKey: function(event) {
    this._focusNext();
    event.detail.keyboardEvent.preventDefault();
  },

  /**
   * Handler that is called when the esc key is pressed.
   *
   * @param {CustomEvent} event A key combination event.
   */
  _onEscKey: function(event) {
    var focusedItem = this.focusedItem;
    if (focusedItem) {
      focusedItem.blur();
    }
  },

  /**
   * Handler that is called when a keydown event is detected.
   *
   * @param {KeyboardEvent} event A keyboard event.
   */
  _onKeydown: function(event) {
    if (!this.keyboardEventMatchesKeys(event, 'up down esc')) {
      // all other keys focus the menu item starting with that character
      this._focusWithKeyboardEvent(event);
    }
    event.stopPropagation();
  },

  // override _activateHandler
  _activateHandler: function(event) {
    IronSelectableBehavior._activateHandler.call(this, event);
    event.stopPropagation();
  },

  /**
   * Updates this element's tab index when it's enabled/disabled.
   * @param {boolean} disabled
   */
  _disabledChanged: function(disabled) {
    if (disabled) {
      this._previousTabIndex =
          this.hasAttribute('tabindex') ? this.tabIndex : 0;
      this.removeAttribute(
          'tabindex');  // No tabindex means not tab-able or select-able.
    } else if (!this.hasAttribute('tabindex')) {
      this.setAttribute('tabindex', this._previousTabIndex);
    }
  }
};

IronMenuBehaviorImpl._shiftTabPressed = false;

/** @polymerBehavior */
const IronMenuBehavior =
    [IronMultiSelectableBehavior, IronA11yKeysBehavior, IronMenuBehaviorImpl];

export { IronMenuBehavior, IronMenuBehaviorImpl };
