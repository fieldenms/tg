import '../polymer/polymer-legacy.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { IronMultiSelectableBehavior } from './iron-multi-selectable.js';

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
`iron-selector` is an element which can be used to manage a list of elements
that can be selected.  Tapping on the item will make the item selected.  The
`selected` indicates which item is being selected.  The default is to use the
index of the item.

Example:

    <iron-selector selected="0">
      <div>Item 1</div>
      <div>Item 2</div>
      <div>Item 3</div>
    </iron-selector>

If you want to use the attribute value of an element for `selected` instead of
the index, set `attrForSelected` to the name of the attribute.  For example, if
you want to select item by `name`, set `attrForSelected` to `name`.

Example:

    <iron-selector attr-for-selected="name" selected="foo">
      <div name="foo">Foo</div>
      <div name="bar">Bar</div>
      <div name="zot">Zot</div>
    </iron-selector>

You can specify a default fallback with `fallbackSelection` in case the
`selected` attribute does not match the `attrForSelected` attribute of any
elements.

Example:

      <iron-selector attr-for-selected="name" selected="non-existing"
                     fallback-selection="default">
        <div name="foo">Foo</div>
        <div name="bar">Bar</div>
        <div name="default">Default</div>
      </iron-selector>

Note: When the selector is multi, the selection will set to `fallbackSelection`
iff the number of matching elements is zero.

`iron-selector` is not styled. Use the `iron-selected` CSS class to style the
selected element.

Example:

    <style>
      .iron-selected {
        background: #eee;
      }
    </style>

    ...

    <iron-selector selected="0">
      <div>Item 1</div>
      <div>Item 2</div>
      <div>Item 3</div>
    </iron-selector>

@demo demo/index.html
*/

Polymer({

  is: 'iron-selector',

  behaviors: [IronMultiSelectableBehavior]

});
