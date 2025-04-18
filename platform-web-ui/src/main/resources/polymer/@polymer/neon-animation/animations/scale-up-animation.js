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
`<scale-up-animation>` animates the scale transform of an element from 0 to 1.
By default it scales in both the x and y axes.

Configuration:
```
{
  name: 'scale-up-animation',
  node: <node>,
  axis: 'x' | 'y' | '',
  transformOrigin: <transform-origin>,
  timing: <animation-timing>
}
```
*/
Polymer({

  is: 'scale-up-animation',

  behaviors: [NeonAnimationBehavior],

  configure: function(config) {
    var node = config.node;

    var scaleProperty = 'scale(0)';
    if (config.axis === 'x') {
      scaleProperty = 'scale(0, 1)';
    } else if (config.axis === 'y') {
      scaleProperty = 'scale(1, 0)';
    }

    this._effect = new KeyframeEffect(
        node,
        [{'transform': scaleProperty}, {'transform': 'scale(1, 1)'}],
        this.timingFromConfig(config));

    if (config.transformOrigin) {
      this.setPrefixedProperty(node, 'transformOrigin', config.transformOrigin);
    }

    return this._effect;
  }

});
