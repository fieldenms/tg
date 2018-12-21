[![Published on NPM](https://img.shields.io/npm/v/@polymer/iron-iconset.svg)](https://www.npmjs.com/package/@polymer/iron-iconset)
[![Build status](https://travis-ci.org/PolymerElements/iron-iconset.svg?branch=master)](https://travis-ci.org/PolymerElements/iron-iconset)
[![Published on webcomponents.org](https://img.shields.io/badge/webcomponents.org-published-blue.svg)](https://webcomponents.org/element/@polymer/iron-iconset)

## &lt;iron-iconset&gt;

The `iron-iconset` element allows users to define their own icon sets.

See: [Documentation](https://www.webcomponents.org/element/@polymer/iron-iconset),
  [Demo](https://www.webcomponents.org/element/@polymer/iron-iconset/demo/demo/index.html).

## Usage

### Installation

```
npm install --save @polymer/iron-iconset
```

### In an HTML file

```html
<html>
  <head>
    <script type="module">
      import '@polymer/iron-iconset/iron-iconset.js';
    </script>
  </head>
  <body>
    <iron-iconset id="my-icons" src="my-icons.png" width="96" size="24"
        icons="location place starta stopb bus car train walk">
    </iron-iconset>
  </body>
</html>
```

### In a Polymer 3 element

```js
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

import '@polymer/iron-iconset/iron-iconset.js';

class ExampleElement extends PolymerElement {
  static get template() {
    return html`
      <iron-iconset id="my-icons" src="my-icons.png" width="96" size="24"
          icons="location place starta stopb bus car train walk">
      </iron-iconset>
    `;
  }
}

customElements.define('example-element', ExampleElement);
```

## Contributing

If you want to send a PR to this element, here are the instructions for running
the tests and demo locally:

### Installation

```sh
git clone https://github.com/PolymerElements/iron-iconset
cd iron-iconset
npm install
npm install -g polymer-cli
```

### Running the demo locally

```sh
polymer serve --npm
open http://127.0.0.1:<port>/demo/
```

### Running the tests

```sh
polymer test --npm
```
