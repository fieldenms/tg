import '../../polymer/polymer-legacy.js';
import { Polymer } from '../../polymer/lib/legacy/polymer-fn.js';
import { NeonAnimationBehavior } from '../neon-animation-behavior.js';

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
`<cascaded-animation>` applies an animation on an array of elements with a delay
between each. the delay defaults to 50ms.

Configuration:
```
{
  name: 'cascaded-animation',
  animation: <animation-name>,
  nodes: <array-of-nodes>,
  nodeDelay: <node-delay-in-ms>,
  timing: <animation-timing>
}
```
*/
Polymer({
  is: 'cascaded-animation',

  behaviors: [NeonAnimationBehavior],

  /**
   * @param {{
   *   animation: string,
   *   nodes: !Array<!Element>,
   *   nodeDelay: (number|undefined),
   *   timing: (Object|undefined)
   *  }} config
   */
  configure: function(config) {
    this._animations = [];
    var nodes = config.nodes;
    var effects = [];
    var nodeDelay = config.nodeDelay || 50;

    config.timing = config.timing || {};
    config.timing.delay = config.timing.delay || 0;

    var oldDelay = config.timing.delay;
    var abortedConfigure;
    for (var node, index = 0; node = nodes[index]; index++) {
      config.timing.delay += nodeDelay;
      config.node = node;

      var animation = document.createElement(config.animation);
      if (animation.isNeonAnimation) {
        var effect = animation.configure(config);

        this._animations.push(animation);
        effects.push(effect);
      } else {
        console.warn(this.is + ':', config.animation, 'not found!');
        abortedConfigure = true;
        break;
      }
    }
    config.timing.delay = oldDelay;
    config.node = null;
    // if a bad animation was configured, abort config.
    if (abortedConfigure) {
      return;
    }

    this._effect = new GroupEffect(effects);
    return this._effect;
  },

  complete: function() {
    for (var animation, index = 0; animation = this._animations[index];
         index++) {
      animation.complete(animation.config);
    }
  }

});
