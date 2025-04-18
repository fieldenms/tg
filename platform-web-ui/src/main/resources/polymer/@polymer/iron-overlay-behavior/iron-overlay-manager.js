import '../polymer/polymer-legacy.js';
import './iron-overlay-backdrop.js';
import { IronA11yKeysBehavior } from '../iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { dom } from '../polymer/lib/legacy/polymer.dom.js';
import { addListener } from '../polymer/lib/utils/gestures.js';

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
 * @package
 */
class IronOverlayManagerClass {
  constructor() {
    /**
     * Used to keep track of the opened overlays.
     * @private {!Array<!Element>}
     */
    this._overlays = [];

    /**
     * iframes have a default z-index of 100,
     * so this default should be at least that.
     * @private {number}
     */
    this._minimumZ = 101;

    /**
     * Memoized backdrop element.
     * @private {Element|null}
     */
    this._backdropElement = null;

    // Enable document-wide tap recognizer.
    // NOTE: Use useCapture=true to avoid accidentally prevention of the closing
    // of an overlay via event.stopPropagation(). The only way to prevent
    // closing of an overlay should be through its APIs.
    // NOTE: enable tap on <html> to workaround Polymer/polymer#4459
    // Pass no-op function because MSEdge 15 doesn't handle null as 2nd argument
    // https://github.com/Microsoft/ChakraCore/issues/3863

    addListener(document.documentElement, 'tap', function () {});
    /*TG #2329*/
    const clickEvent = ('ontouchstart' in window) ? 'touchstart' : 'mousedown';
    document.addEventListener(clickEvent, this._onCaptureClick.bind(this), true);
    /*TG #2329*/
    document.addEventListener('focus', this._onCaptureFocus.bind(this), true);
    document.addEventListener(
        'keydown', this._onCaptureKeyDown.bind(this), true);
  }

  /**
   * The shared backdrop element.
   * @return {!Element} backdropElement
   */
  get backdropElement() {
    if (!this._backdropElement) {
      this._backdropElement = document.createElement('iron-overlay-backdrop');
    }
    return this._backdropElement;
  }

  /**
   * The deepest active element.
   * @return {!Element} activeElement the active element
   */
  get deepActiveElement() {
    var active = document.activeElement;
    // document.activeElement can be null
    // https://developer.mozilla.org/en-US/docs/Web/API/Document/activeElement
    // In IE 11, it can also be an object when operating in iframes.
    // In these cases, default it to document.body.
    if (!active || active instanceof Element === false) {
      active = document.body;
    }
    while (active.root && dom(active.root).activeElement) {
      active = dom(active.root).activeElement;
    }
    return active;
  }

  /**
   * Brings the overlay at the specified index to the front.
   * @param {number} i
   * @private
   */
  _bringOverlayAtIndexToFront(i) {
    /*TG #2329*/
    const overlay = this._overlays[i];

    if (!overlay) {
      return;
    }
    var lastI = this._overlays.length - 1;
    var currentOverlay = this._overlays[lastI];
    // Ensure always-on-top overlay stays on top.

    while (currentOverlay && this._shouldBeBehindOverlay(overlay, currentOverlay)) {
      lastI--;
      currentOverlay = this._overlays[lastI];
    }
    // If already the top element, return.
    if (i >= lastI) {
      return;
    }

    const minimumZ = Math.max(this._getZ(this._overlays[lastI]), this._minimumZ);

    // if (this._getZ(overlay) <= minimumZ) {
    //   this._applyOverlayZ(overlay, minimumZ);
    // } // Shift other overlays behind the new on top.

    // Get the smallest z-index to replace z-index for the next overlay in the list
    let lastZ = this._getZ(overlay);
    while (i < lastI) {
      // Shift overlay to left
      this._overlays[i] = this._overlays[i + 1];
      i++;
      // Replace z-index a for shifted overlay by that last smallest z-index. But before doing this capture z-index for the next overlay.
      let tempZ = this._getZ(this._overlays[i])
      this._setZ(this._overlays[i], lastZ);
      lastZ = tempZ;
    }
    this._overlays[lastI] = overlay;

    if (this._getZ(overlay) <= minimumZ) {
      this._setZ(overlay, minimumZ);
  }
    /*TG #2329*/
  }
  /**
   * Adds the overlay and updates its z-index if it's opened, or removes it if
   * it's closed. Also updates the backdrop z-index.
   * @param {!Element} overlay
   */
  addOrRemoveOverlay(overlay) {
    if (overlay.opened) {
      this.addOverlay(overlay);
    } else {
      this.removeOverlay(overlay);
    }
  }

  /**
   * Tracks overlays for z-index and focus management.
   * Ensures the last added overlay with always-on-top remains on top.
   * @param {!Element} overlay
   */
  addOverlay(overlay) {
    var i = this._overlays.indexOf(overlay);
    if (i >= 0) {
      this._bringOverlayAtIndexToFront(i);
      this.trackBackdrop();
      return;
    }
    var insertionIndex = this._overlays.length;
    var currentOverlay = this._overlays[insertionIndex - 1];
    var minimumZ = Math.max(this._getZ(currentOverlay), this._minimumZ);

    /*TG #2329*/
    var newZ = this._getZ(overlay);

    // Ensure always-on-top overlay stays on top.
    while (currentOverlay && this._shouldBeBehindOverlay(overlay, currentOverlay)) {
      // This bumps the z-index of +2.
      this._applyOverlayZ(currentOverlay, minimumZ);
      insertionIndex--;
      // Update minimumZ to match previous overlay's z-index.

      currentOverlay = this._overlays[insertionIndex - 1];
      minimumZ = Math.max(this._getZ(currentOverlay), this._minimumZ);
      /*TG #2329*/
    } // Update z-index and insert overlay.


    if (newZ <= minimumZ) {
      this._applyOverlayZ(overlay, minimumZ);
    }
    this._overlays.splice(insertionIndex, 0, overlay);

    this.trackBackdrop();
  }

  /**
   * @param {!Element} overlay
   */
  removeOverlay(overlay) {
    var i = this._overlays.indexOf(overlay);
    if (i === -1) {
      return;
    }
    this._overlays.splice(i, 1);

    this.trackBackdrop();
  }

  /**
   * Returns the current overlay.
   * @return {!Element|undefined}
   */
  currentOverlay() {
    var i = this._overlays.length - 1;
    return this._overlays[i];
  }

  /**
   * Returns the current overlay z-index.
   * @return {number}
   */
  currentOverlayZ() {
    return this._getZ(this.currentOverlay());
  }

  /**
   * Ensures that the minimum z-index of new overlays is at least `minimumZ`.
   * This does not effect the z-index of any existing overlays.
   * @param {number} minimumZ
   */
  ensureMinimumZ(minimumZ) {
    this._minimumZ = Math.max(this._minimumZ, minimumZ);
  }

  focusOverlay() {
    var current = /** @type {?} */ (this.currentOverlay());
    if (current) {
      current._applyFocus();
    }
  }

  /**
   * Updates the backdrop z-index.
   */
  trackBackdrop() {
    var overlay = this._overlayWithBackdrop();
    // Avoid creating the backdrop if there is no overlay with backdrop.
    if (!overlay && !this._backdropElement) {
      return;
    }
    this.backdropElement.style.zIndex = this._getZ(overlay) - 1;
    this.backdropElement.opened = !!overlay;
    // Property observers are not fired until element is attached
    // in Polymer 2.x, so we ensure element is attached if needed.
    // https://github.com/Polymer/polymer/issues/4526
    this.backdropElement.prepare();
  }

  /**
   * @return {!Array<!Element>}
   */
  getBackdrops() {
    var backdrops = [];
    for (var i = 0; i < this._overlays.length; i++) {
      if (this._overlays[i].withBackdrop) {
        backdrops.push(this._overlays[i]);
      }
    }
    return backdrops;
  }

  /**
   * Returns the z-index for the backdrop.
   * @return {number}
   */
  backdropZ() {
    return this._getZ(this._overlayWithBackdrop()) - 1;
  }

  /**
   * Returns the top opened overlay that has a backdrop.
   * @return {!Element|undefined}
   * @private
   */
  _overlayWithBackdrop() {
    for (var i = this._overlays.length - 1; i >= 0; i--) {
      if (this._overlays[i].withBackdrop) {
        return this._overlays[i];
      }
    }
  }

  /**
   * Calculates the minimum z-index for the overlay.
   * @param {Element=} overlay
   * @private
   */
  _getZ(overlay) {
    var z = this._minimumZ;
    if (overlay) {
      var z1 = Number(
          overlay.style.zIndex || window.getComputedStyle(overlay).zIndex);
      // Check if is a number
      // Number.isNaN not supported in IE 10+
      if (z1 === z1) {
        z = z1;
      }
    }
    return z;
  }

  /**
   * @param {!Element} element
   * @param {number|string} z
   * @private
   */
  _setZ(element, z) {
    element.style.zIndex = z;
  }

  /**
   * @param {!Element} overlay
   * @param {number} aboveZ
   * @private
   */
  _applyOverlayZ(overlay, aboveZ) {
    this._setZ(overlay, aboveZ + 2);
  }

  /**
   * Returns the deepest overlay in the path.
   * @param {!Array<!Element>=} path
   * @return {!Element|undefined}
   * @suppress {missingProperties}
   * @private
   */
  _overlayInPath(path) {
    path = path || [];
    for (var i = 0; i < path.length; i++) {
      if (path[i]._manager === this) {
        return path[i];
      }
    }
  }

  /**
   * Ensures the click event is delegated to the right overlay.
   * @param {!Event} event
   * @private
   */
  _onCaptureClick(event) {
    var i = this._overlays.length - 1;
    if (i === -1)
      return;
    var path = /** @type {!Array<!EventTarget>} */ (dom(event).path);
    var overlay;
    // Check if clicked outside of overlay.
    while ((overlay = /** @type {?} */ (this._overlays[i])) &&
           this._overlayInPath(path) !== overlay) {
      overlay._onCaptureClick(event);
      if (overlay.allowClickThrough) {
        i--;
      } else {
        break;
      }
    }
  }

  /**
   * Ensures the focus event is delegated to the right overlay.
   * @param {!Event} event
   * @private
   */
  _onCaptureFocus(event) {
    var overlay = /** @type {?} */ (this.currentOverlay());
    if (overlay) {
      overlay._onCaptureFocus(event);
    }
  }

  /**
   * Ensures TAB and ESC keyboard events are delegated to the right overlay.
   * @param {!Event} event
   * @private
   */
  _onCaptureKeyDown(event) {
    var overlay = /** @type {?} */ (this.currentOverlay());
    if (overlay) {
      if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'esc')) {
        overlay._onCaptureEsc(event);
      } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'tab')) {
        overlay._onCaptureTab(event);
      }
    }
  }

  /**
   * Returns if the overlay1 should be behind overlay2.
   * @param {!Element} overlay1
   * @param {!Element} overlay2
   * @return {boolean}
   * @suppress {missingProperties}
   * @private
   */
  _shouldBeBehindOverlay(overlay1, overlay2) {
    return !overlay1.alwaysOnTop && overlay2.alwaysOnTop;
  }
}
const IronOverlayManager = new IronOverlayManagerClass();

export { IronOverlayManager, IronOverlayManagerClass };
