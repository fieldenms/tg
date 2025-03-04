import '../polymer/polymer-legacy.js';
import '../paper-styles/default-theme.js';
import '../paper-styles/typography.js';
import '../iron-flex-layout/iron-flex-layout.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { dom } from '../polymer/lib/legacy/polymer.dom.js';
import { html } from '../polymer/lib/utils/html-tag.js';
import { flush } from '../polymer/lib/utils/flush.js';

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
**This element has been deprecated in favor of
[app-layout](https://github.com/PolymerElements/app-layout).**

Material design:
[Toolbars](https://www.google.com/design/spec/components/toolbars.html)

`paper-toolbar` is a horizontal bar containing items that can be used for
label, navigation, search and actions.  The items placed inside the
`paper-toolbar` are projected into a `class="horizontal center layout"`
container inside of `paper-toolbar`'s Shadow DOM.  You can use flex attributes
to control the items' sizing.

Example:

```html
<paper-toolbar>
  <paper-icon-button slot="top" icon="menu"
on-tap="menuAction"></paper-icon-button> <div slot="top"
class="title">Title</div> <paper-icon-button slot="top" icon="more-vert"
on-tap="moreAction"></paper-icon-button>
</paper-toolbar>
```

`paper-toolbar` has a standard height, but can made be taller by setting `tall`
class on the `paper-toolbar`. This will make the toolbar 3x the normal height.

```html
<paper-toolbar class="tall">
  <paper-icon-button slot="top" icon="menu"></paper-icon-button>
</paper-toolbar>
```

Apply `medium-tall` class to make the toolbar medium tall.  This will make the
toolbar 2x the normal height.

```html
<paper-toolbar class="medium-tall">
  <paper-icon-button slot="top" icon="menu"></paper-icon-button>
</paper-toolbar>
```

When `tall`, items can pin to either the top (default), middle or bottom. Use
`middle` slot for middle content and `bottom` slot for bottom content.

```html
<paper-toolbar class="tall">
  <paper-icon-button slot="top" icon="menu"></paper-icon-button>
  <div slot="middle" class="title">Middle Title</div>
  <div slot="bottom" class="title">Bottom Title</div>
</paper-toolbar>
```

For `medium-tall` toolbar, the middle and bottom contents overlap and are
pinned to the bottom. But `middleJustify` and `bottomJustify` attributes are
still honored separately.

To make an element completely fit at the bottom of the toolbar, use `fit` along
with `bottom`.

```html
<paper-toolbar class="tall">
  <div id="progressBar" slot="bottom" class="fit"></div>
</paper-toolbar>
```

When inside a `paper-header-panel` element with `mode="waterfall-tall"`,
the class `.animate` is toggled to animate the height change in the toolbar.

### Styling

The following custom properties and mixins are available for styling:

Custom property | Description | Default
----------------|-------------|----------
`--paper-toolbar-title`      | Mixin applied to the title of the toolbar | `{}`
`--paper-toolbar-background` | Toolbar background color     | `--primary-color`
`--paper-toolbar-color`      | Toolbar foreground color     | `--dark-theme-text-color`
`--paper-toolbar-height`     | Custom height for toolbar    | `64px`
`--paper-toolbar-sm-height`  | Custom height for small screen toolbar | `56px`
`--paper-toolbar`            | Mixin applied to the toolbar | `{}`
`--paper-toolbar-content`    | Mixin applied to the content section of the toolbar | `{}`
`--paper-toolbar-medium`     | Mixin applied to medium height toolbar | `{}`
`--paper-toolbar-tall`       | Mixin applied to tall height toolbar | `{}`
`--paper-toolbar-transition` | Transition applied to the `.animate` class | `height 0.18s ease-in`

### Accessibility

`<paper-toolbar>` has `role="toolbar"` by default. Any elements with the class
`title` will be used as the label of the toolbar via `aria-labelledby`.

### Breaking change in 2.0

* In Polymer 1.x, default content used to be distribuited automatically to the
top toolbar. In v2, the you must set `slot="top"` on the default content to
distribuite the content to the top toolbar.

@demo demo/index.html
*/
Polymer({
  _template: html`
    <style>
      :host {
        --calculated-paper-toolbar-height: var(--paper-toolbar-height, 64px);
        --calculated-paper-toolbar-sm-height: var(--paper-toolbar-sm-height, 56px);
        display: block;
        position: relative;
        box-sizing: border-box;
        -moz-box-sizing: border-box;
        height: var(--calculated-paper-toolbar-height);
        background: var(--paper-toolbar-background, var(--primary-color));
        color: var(--paper-toolbar-color, var(--dark-theme-text-color));
        @apply --paper-toolbar;
      }

      :host(.animate) {
        transition: var(--paper-toolbar-transition, height 0.18s ease-in);
      }

      :host(.medium-tall) {
        height: calc(var(--calculated-paper-toolbar-height) * 2);
        @apply --paper-toolbar-medium;
      }

      :host(.tall) {
        height: calc(var(--calculated-paper-toolbar-height) * 3);
        @apply --paper-toolbar-tall;
      }

      .toolbar-tools {
        position: relative;
        height: var(--calculated-paper-toolbar-height);
        padding: 0 16px;
        pointer-events: none;
        @apply --layout-horizontal;
        @apply --layout-center;
        @apply --paper-toolbar-content;
      }

      /*
       * TODO: Where should media query breakpoints live so they can be shared between elements?
       */

      @media (max-width: 600px) {
        :host {
          height: var(--calculated-paper-toolbar-sm-height);
        }

        :host(.medium-tall) {
          height: calc(var(--calculated-paper-toolbar-sm-height) * 2);
        }

        :host(.tall) {
          height: calc(var(--calculated-paper-toolbar-sm-height) * 3);
        }

        .toolbar-tools {
          height: var(--calculated-paper-toolbar-sm-height);
        }
      }

      #topBar {
        position: relative;
      }

      /* middle bar */
      #middleBar {
        position: absolute;
        top: 0;
        right: 0;
        left: 0;
      }

      :host(.tall) #middleBar,
      :host(.medium-tall) #middleBar {
        -webkit-transform: translateY(100%);
        transform: translateY(100%);
      }

      /* bottom bar */
      #bottomBar {
        position: absolute;
        right: 0;
        bottom: 0;
        left: 0;
      }

      /*
       * make elements (e.g. buttons) respond to mouse/touch events
       *
       * \`.toolbar-tools\` disables touch events so multiple toolbars can stack and not
       * absorb events. All children must have pointer events re-enabled to work as
       * expected.
       */
      .toolbar-tools > ::slotted(*:not([disabled])) {
        pointer-events: auto;
      }

      .toolbar-tools > ::slotted(.title) {
        @apply --paper-font-common-base;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 20px;
        font-weight: 400;
        line-height: 1;
        pointer-events: none;
        @apply --layout-flex;
      }

      .toolbar-tools > ::slotted(.title) {
        margin-left: 56px;
      }

      .toolbar-tools > ::slotted(paper-icon-button + .title) {
        margin-left: 0;
      }

      /**
       * The --paper-toolbar-title mixin is applied here instead of above to
       * fix the issue with margin-left being ignored due to css ordering.
       */
      .toolbar-tools > ::slotted(.title) {
        @apply --paper-toolbar-title;
      }

      .toolbar-tools > ::slotted(paper-icon-button[icon=menu]) {
        margin-right: 24px;
      }

      .toolbar-tools > ::slotted(.fit) {
        position: absolute;
        top: auto;
        right: 0;
        bottom: 0;
        left: 0;
        width: auto;
        margin: 0;
      }

      /* TODO(noms): Until we have a better solution for classes that don't use
       * /deep/ create our own.
       */
      .start-justified {
        @apply --layout-start-justified;
      }

      .center-justified {
        @apply --layout-center-justified;
      }

      .end-justified {
        @apply --layout-end-justified;
      }

      .around-justified {
        @apply --layout-around-justified;
      }

      .justified {
        @apply --layout-justified;
      }
    </style>

    <div id="topBar" class\$="toolbar-tools [[_computeBarExtraClasses(justify)]]">
      <slot name="top"></slot>
    </div>

    <div id="middleBar" class\$="toolbar-tools [[_computeBarExtraClasses(middleJustify)]]">
      <slot name="middle"></slot>
    </div>

    <div id="bottomBar" class\$="toolbar-tools [[_computeBarExtraClasses(bottomJustify)]]">
      <slot name="bottom"></slot>
    </div>
`,

  is: 'paper-toolbar',
  hostAttributes: {'role': 'toolbar'},

  properties: {
    /**
     * Controls how the items are aligned horizontally when they are placed
     * at the bottom.
     * Options are `start`, `center`, `end`, `justified` and `around`.
     */
    bottomJustify: {type: String, value: ''},

    /**
     * Controls how the items are aligned horizontally.
     * Options are `start`, `center`, `end`, `justified` and `around`.
     */
    justify: {type: String, value: ''},

    /**
     * Controls how the items are aligned horizontally when they are placed
     * in the middle.
     * Options are `start`, `center`, `end`, `justified` and `around`.
     */
    middleJustify: {type: String, value: ''}

  },

  ready: function() {
    console.warn(this.is, 'is deprecated. Please use app-layout instead!');
  },

  attached: function() {
    this._observer = this._observe(this);
    this._updateAriaLabelledBy();
  },

  detached: function() {
    if (this._observer) {
      this._observer.disconnect();
    }
  },

  _observe: function(node) {
    var observer = new MutationObserver(function() {
      this._updateAriaLabelledBy();
    }.bind(this));
    observer.observe(node, {childList: true, subtree: true});
    return observer;
  },

  _updateAriaLabelledBy: function() {
    flush();
    var labelledBy = [];
    var contents =
        Array.prototype.slice.call(dom(this.root).querySelectorAll('slot'))
            .concat(Array.prototype.slice.call(
                dom(this.root).querySelectorAll('content')));

    for (var content, index = 0; content = contents[index]; index++) {
      var nodes = dom(content).getDistributedNodes();
      for (var node, jndex = 0; node = nodes[jndex]; jndex++) {
        if (node.classList && node.classList.contains('title')) {
          if (node.id) {
            labelledBy.push(node.id);
          } else {
            var id = 'paper-toolbar-label-' + Math.floor(Math.random() * 10000);
            node.id = id;
            labelledBy.push(id);
          }
        }
      }
    }
    if (labelledBy.length > 0) {
      this.setAttribute('aria-labelledby', labelledBy.join(' '));
    }
  },

  _computeBarExtraClasses: function(barJustify) {
    if (!barJustify)
      return '';
    return barJustify + (barJustify === 'justified' ? '' : '-justified');
  }
});
