import '../polymer/polymer-legacy.js';
import { NeonAnimationBehavior } from './neon-animation-behavior.js';

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
 * Use `NeonSharedElementAnimationBehavior` to implement shared element
 * animations.
 * @polymerBehavior NeonSharedElementAnimationBehavior
 */
const NeonSharedElementAnimationBehaviorImpl = {

  properties: {

    /**
     * Cached copy of shared elements.
     */
    sharedElements: {type: Object}

  },

  /**
   * Finds shared elements based on `config`.
   */
  findSharedElements: function(config) {
    var fromPage = config.fromPage;
    var toPage = config.toPage;
    if (!fromPage || !toPage) {
      console.warn(
          this.is + ':', !fromPage ? 'fromPage' : 'toPage', 'is undefined!');
      return null;
    }
    if (!fromPage.sharedElements || !toPage.sharedElements) {
      console.warn(
          this.is + ':',
          'sharedElements are undefined for',
          !fromPage.sharedElements ? fromPage : toPage);
      return null;
    }
    var from = fromPage.sharedElements[config.id];
    var to = toPage.sharedElements[config.id];

    if (!from || !to) {
      console.warn(
          this.is + ':',
          'sharedElement with id',
          config.id,
          'not found in',
          !from ? fromPage : toPage);
      return null;
    }

    this.sharedElements = {from: from, to: to};
    return this.sharedElements;
  }

};

/** @polymerBehavior */
const NeonSharedElementAnimationBehavior =
    [NeonAnimationBehavior, NeonSharedElementAnimationBehaviorImpl];

export { NeonSharedElementAnimationBehavior, NeonSharedElementAnimationBehaviorImpl };
