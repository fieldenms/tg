import '../../polymer/polymer-legacy.js';
import '../../iron-media-query/iron-media-query.js';
import { Polymer } from '../../polymer/lib/legacy/polymer-fn.js';
import { dom } from '../../polymer/lib/legacy/polymer.dom.js';
import { html } from '../../polymer/lib/utils/html-tag.js';
import { afterNextRender } from '../../polymer/lib/utils/render-status.js';
import { AppLayoutBehavior } from '../app-layout-behavior/app-layout-behavior.js';

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
app-drawer-layout is a wrapper element that positions an app-drawer and other
content. When the viewport width is smaller than `responsiveWidth`, this element
changes to narrow layout. In narrow layout, the drawer will be stacked on top of
the main content. The drawer will slide in/out to hide/reveal the main content.

By default the drawer is aligned to the start, which is left in LTR layouts:

```html
<app-drawer-layout>
  <app-drawer slot="drawer">
    drawer content
  </app-drawer>
  <div>
    main content
  </div>
</app-drawer-layout>
```

Align the drawer at the end:

```html
<app-drawer-layout>
  <app-drawer slot="drawer" align="end">
     drawer content
  </app-drawer>
  <div>
    main content
  </div>
</app-drawer-layout>
```

With an app-header-layout:

```html
<app-drawer-layout>
  <app-drawer slot="drawer">
    drawer-content
  </app-drawer>
  <app-header-layout>
    <app-header slot="header">
      <app-toolbar>
        <div main-title>App name</div>
      </app-toolbar>
    </app-header>

    main content

  </app-header-layout>
</app-drawer-layout>
```

Add the `drawer-toggle` attribute to elements inside `app-drawer-layout` that
toggle the drawer on click events:

```html
<app-drawer-layout>
  <app-drawer slot="drawer">
    drawer-content
  </app-drawer>
  <app-header-layout>
    <app-header slot="header">
      <app-toolbar>
        <paper-icon-button icon="menu" drawer-toggle></paper-icon-button>
        <div main-title>App name</div>
      </app-toolbar>
    </app-header>

    main content

  </app-header-layout>
</app-drawer-layout>
```

**NOTE:** With app-layout 2.0, the `drawer-toggle` element needs to be manually
hidden when app-drawer-layout is not in narrow layout. To add this, add the
following CSS rule where app-drawer-layout is used:

```css
app-drawer-layout:not([narrow]) [drawer-toggle] {
  display: none;
}
```

Add the `fullbleed` attribute to app-drawer-layout to make it fit the size of
its container:

```html
<app-drawer-layout fullbleed>
  <app-drawer slot="drawer">
     drawer content
  </app-drawer>
  <div>
    main content
  </div>
</app-drawer-layout>
```

### Styling

Custom property                          | Description                          | Default
-----------------------------------------|--------------------------------------|---------
`--app-drawer-width`                     | Width of the drawer                  | 256px
`--app-drawer-layout-content-transition` | Transition for the content container | none

**NOTE:** If you use <app-drawer> with <app-drawer-layout> and specify a value
for
`--app-drawer-width`, that value must be accessible by both elements. This can
be done by defining the value on the `:host` that contains <app-drawer-layout>
(or `html` if outside a shadow root):

```css
:host {
  --app-drawer-width: 300px;
}
```

@element app-drawer-layout
@demo app-drawer-layout/demo/index.html
*/
Polymer({
  /** @override */
  _template: html`
    <style>
      :host {
        display: block;
        /**
         * Force app-drawer-layout to have its own stacking context so that its parent can
         * control the stacking of it relative to other elements.
         */
        position: relative;
        z-index: 0;
      }

      :host ::slotted([slot=drawer]) {
        z-index: 1;
      }

      :host([fullbleed]) {
        @apply --layout-fit;
      }

      #contentContainer {
        /* Create a stacking context here so that all children appear below the header. */
        position: relative;
        z-index: 0;
        height: 100%;
        transition: var(--app-drawer-layout-content-transition, none);
      }

      #contentContainer[drawer-position=left] {
        margin-left: var(--app-drawer-width, 256px);
      }

      #contentContainer[drawer-position=right] {
        margin-right: var(--app-drawer-width, 256px);
      }
    </style>

    <slot id="drawerSlot" name="drawer"></slot>

    <div id="contentContainer" drawer-position\$="[[_drawerPosition]]">
      <slot></slot>
    </div>

    <iron-media-query query="[[_computeMediaQuery(forceNarrow, responsiveWidth)]]" on-query-matches-changed="_onQueryMatchesChanged"></iron-media-query>
`,

  is: 'app-drawer-layout',
  behaviors: [AppLayoutBehavior],

  properties: {
    /**
     * If true, ignore `responsiveWidth` setting and force the narrow layout.
     */
    forceNarrow: {type: Boolean, value: false},

    /**
     * If the viewport's width is smaller than this value, the panel will change
     * to narrow layout. In the mode the drawer will be closed.
     */
    responsiveWidth: {type: String, value: '640px'},

    /**
     * Returns true if it is in narrow layout. This is useful if you need to
     * show/hide elements based on the layout.
     */
    narrow:
        {type: Boolean, reflectToAttribute: true, readOnly: true, notify: true},

    /**
     * If true, the drawer will initially be opened when in narrow layout mode.
     */
    openedWhenNarrow: {type: Boolean, value: false},

    _drawerPosition: {type: String}
  },

  listeners: {'click': '_clickHandler'},
  observers: ['_narrowChanged(narrow)'],

  /**
   * A reference to the app-drawer element.
   *
   * @property drawer
   */
  get drawer() {
    return dom(this.$.drawerSlot).getDistributedNodes()[0];
  },

  /** @override */
  attached: function() {
    // Disable drawer transitions until after app-drawer-layout sets the initial
    // opened state.
    var drawer = this.drawer;
    if (drawer) {
      drawer.setAttribute('no-transition', '');
    }
  },

  _clickHandler: function(e) {
    var target = dom(e).localTarget;
    if (target && target.hasAttribute('drawer-toggle')) {
      var drawer = this.drawer;
      if (drawer && !drawer.persistent) {
        drawer.toggle();
      }
    }
  },

  _updateLayoutStates: function() {
    var drawer = this.drawer;
    if (!this.isAttached || !drawer) {
      return;
    }

    this._drawerPosition = this.narrow ? null : drawer.position;
    if (this._drawerNeedsReset) {
      if (this.narrow) {
        drawer.opened = this.openedWhenNarrow;
        drawer.persistent = false;
      } else {
        drawer.opened = drawer.persistent = true;
      }
      if (drawer.hasAttribute('no-transition')) {
        // Enable drawer transitions after app-drawer-layout sets the initial
        // opened state.
        afterNextRender(this, function() {
          drawer.removeAttribute('no-transition');
        });
      }
      this._drawerNeedsReset = false;
    }
  },

  _narrowChanged: function() {
    this._drawerNeedsReset = true;
    this.resetLayout();
  },

  _onQueryMatchesChanged: function(event) {
    this._setNarrow(event.detail.value);
  },

  _computeMediaQuery: function(forceNarrow, responsiveWidth) {
    return forceNarrow ? '(min-width: 0px)' :
                         '(max-width: ' + responsiveWidth + ')';
  }
});
