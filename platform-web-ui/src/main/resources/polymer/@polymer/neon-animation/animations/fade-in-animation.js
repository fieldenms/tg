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
`<fade-in-animation>` animates the opacity of an element from 0 to 1.

Configuration:
```
{
  name: 'fade-in-animation',
  node: <node>
  timing: <animation-timing>
}
```
*/
Polymer({

  is: 'fade-in-animation',

  behaviors: [NeonAnimationBehavior],

  configure: function(config) {
    var node = config.node;
    this._effect = new KeyframeEffect(
        node,
        [{'opacity': '0'}, {'opacity': '1'}],
        this.timingFromConfig(config));
    return this._effect;
  }

});
