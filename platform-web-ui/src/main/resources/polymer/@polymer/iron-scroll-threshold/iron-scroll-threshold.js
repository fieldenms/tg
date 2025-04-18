import '../polymer/polymer-legacy.js';
import { IronScrollTargetBehavior } from '../iron-scroll-target-behavior/iron-scroll-target-behavior.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { html } from '../polymer/lib/utils/html-tag.js';

/**
@license
Copyright (c) 2016 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at
http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
part of the polymer project is also subject to an additional IP rights grant
found at http://polymer.github.io/PATENTS.txt
*/

/**
`iron-scroll-threshold` is a utility element that listens for `scroll` events
from a scrollable region and fires events to indicate when the scroller has
reached a pre-defined limit, specified in pixels from the upper and lower bounds
of the scrollable region. This element may wrap a scrollable region and will
listen for `scroll` events bubbling through it from its children.  In this case,
care should be taken that only one scrollable region with the same orientation
as this element is contained within. Alternatively, the `scrollTarget` property
can be set/bound to a non-child scrollable region, from which it will listen for
events.

Once a threshold has been reached, a `lower-threshold` or `upper-threshold`
event will be fired, at which point the user may perform actions such as
lazily-loading more data to be displayed. After any work is done, the user must
then clear the threshold by calling the `clearTriggers` method on this element,
after which it will begin listening again for the scroll position to reach the
threshold again assuming the content in the scrollable region has grown. If the
user no longer wishes to receive events (e.g. all data has been exhausted), the
threshold property in question (e.g. `lowerThreshold`) may be set to a falsy
value to disable events and clear the associated triggered property.

### Example

```html
<iron-scroll-threshold on-lower-threshold="loadMoreData">
  <div>content</div>
</iron-scroll-threshold>
```

```js
  loadMoreData: function() {
    // load async stuff. e.g. XHR
    asyncStuff(function done() {
      ironScrollTheshold.clearTriggers();
    });
  }
```

### Using dom-repeat

```html
<iron-scroll-threshold on-lower-threshold="loadMoreData">
  <template is="dom-repeat" items="[[items]]">
    <div>[[index]]</div>
  </template>
</iron-scroll-threshold>
```

### Using iron-list

```html
<iron-scroll-threshold on-lower-threshold="loadMoreData" id="threshold">
  <iron-list scroll-target="threshold" items="[[items]]">
    <template>
      <div>[[index]]</div>
    </template>
  </iron-list>
</iron-scroll-threshold>
```

@group Iron Element
@element iron-scroll-threshold
@demo demo/scrolling-region.html Scrolling Region
@demo demo/document.html Document Element
*/
Polymer({
  _template: html`
    <style>
      :host {
        display: block;
      }
    </style>

    <slot></slot>
`,

  is: 'iron-scroll-threshold',

  properties: {

    /**
     * Distance from the top (or left, for horizontal) bound of the scroller
     * where the "upper trigger" will fire.
     */
    upperThreshold: {type: Number, value: 100},

    /**
     * Distance from the bottom (or right, for horizontal) bound of the scroller
     * where the "lower trigger" will fire.
     */
    lowerThreshold: {type: Number, value: 100},

    /**
     * Read-only value that tracks the triggered state of the upper threshold.
     */
    upperTriggered: {type: Boolean, value: false, notify: true, readOnly: true},

    /**
     * Read-only value that tracks the triggered state of the lower threshold.
     */
    lowerTriggered: {type: Boolean, value: false, notify: true, readOnly: true},

    /**
     * True if the orientation of the scroller is horizontal.
     */
    horizontal: {type: Boolean, value: false}
  },

  behaviors: [IronScrollTargetBehavior],

  observers:
      ['_setOverflow(scrollTarget)', '_initCheck(horizontal, isAttached)'],

  get _defaultScrollTarget() {
    return this;
  },

  _setOverflow: function(scrollTarget) {
    this.style.overflow = scrollTarget === this ? 'auto' : '';
    this.style.webkitOverflowScrolling = scrollTarget === this ? 'touch' : '';
  },

  _scrollHandler: function() {
    // throttle the work on the scroll event
    var THROTTLE_THRESHOLD = 200;
    if (!this.isDebouncerActive('_checkTheshold')) {
      this.debounce('_checkTheshold', function() {
        this.checkScrollThresholds();
      }, THROTTLE_THRESHOLD);
    }
  },

  _initCheck: function(horizontal, isAttached) {
    if (isAttached) {
      this.debounce('_init', function() {
        this.clearTriggers();
        this.checkScrollThresholds();
      });
    }
  },

  /**
   * Checks the scroll thresholds.
   * This method is automatically called by iron-scroll-threshold.
   *
   * @method checkScrollThresholds
   */
  checkScrollThresholds: function() {
    if (!this.scrollTarget || (this.lowerTriggered && this.upperTriggered)) {
      return;
    }
    var upperScrollValue = this.horizontal ? this._scrollLeft : this._scrollTop;
    var lowerScrollValue = this.horizontal ? this.scrollTarget.scrollWidth -
            this._scrollTargetWidth - this._scrollLeft :
                                             this.scrollTarget.scrollHeight -
            this._scrollTargetHeight - this._scrollTop;

    // Detect upper threshold
    if (upperScrollValue <= this.upperThreshold && !this.upperTriggered) {
      this._setUpperTriggered(true);
      this.fire('upper-threshold');
    }
    // Detect lower threshold
    if (lowerScrollValue <= this.lowerThreshold && !this.lowerTriggered) {
      this._setLowerTriggered(true);
      this.fire('lower-threshold');
    }
  },

  checkScrollThesholds: function() {
    // iron-scroll-threshold/issues/16
    this.checkScrollThresholds();
  },

  /**
   * Clear the upper and lower threshold states.
   *
   * @method clearTriggers
   */
  clearTriggers: function() {
    this._setUpperTriggered(false);
    this._setLowerTriggered(false);
  }

  /**
   * Fires when the lower threshold has been reached.
   *
   * @event lower-threshold
   */

  /**
   * Fires when the upper threshold has been reached.
   *
   * @event upper-threshold
   */
});
