import '../polymer/polymer-legacy.js';
import { IronButtonState } from '../iron-behaviors/iron-button-state.js';
import { IronControlState } from '../iron-behaviors/iron-control-state.js';

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
`PaperItemBehavior` is a convenience behavior shared by <paper-item> and
<paper-icon-item> that manages the shared control states and attributes of
the items.
*/
/** @polymerBehavior PaperItemBehavior */
const PaperItemBehaviorImpl = {
  hostAttributes: {role: 'option', tabindex: '0'}
};

/** @polymerBehavior */
const PaperItemBehavior =
    [IronButtonState, IronControlState, PaperItemBehaviorImpl];

export { PaperItemBehavior, PaperItemBehaviorImpl };
