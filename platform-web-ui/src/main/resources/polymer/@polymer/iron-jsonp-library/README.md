[![Published on NPM](https://img.shields.io/npm/v/@polymer/iron-jsonp-library.svg)](https://www.npmjs.com/package/@polymer/iron-jsonp-library)
[![Build status](https://travis-ci.org/PolymerElements/iron-jsonp-library.svg?branch=master)](https://travis-ci.org/PolymerElements/iron-jsonp-library)
[![Published on webcomponents.org](https://img.shields.io/badge/webcomponents.org-published-blue.svg)](https://webcomponents.org/element/@polymer/iron-jsonp-library)

## &lt;iron-jsonp-library&gt;
Loads specified jsonp library.

See: [Documentation](https://www.webcomponents.org/element/@polymer/iron-jsonp-library),
  [Demo](https://www.webcomponents.org/element/@polymer/iron-jsonp-library/demo/demo/index.html).

## Polymer.IronJsonpLibraryBehavior

`Polymer.IronJsonpLibraryBehavior` loads a jsonp library.
Multiple components can request same library, only one copy will load.

Some libraries require a specific global function be defined.
If this is the case, specify the `callbackName` property.

You should use an HTML Import to load library dependencies
when possible instead of using this element.

## Usage

### Installation
```
npm install --save @polymer/iron-jsonp-library
```

### In an html file
```html
<html>
  <head>
    <script type="module">
      import '@polymer/polymer/lib/elements/dom-bind.js';
      import '@polymer/iron-jsonp-library/iron-jsonp-library.js';
    </script>
  </head>
  <body>
    <dom-bind>
      <template>
        <iron-jsonp-library
            library-url="https://apis.google.com/js/plusone.js?onload=%%callback%%"
            notify-event="api-load"
            library-loaded="{{loaded}}">
        </iron-jsonp-library>
        <span>Library Loaded: [[loaded]]</span>
      </template>
    </dom-bind>
  </body>
</html>
```

### In a Polymer 3 element
```js
import {PolymerElement, html} from '@polymer/polymer';
import '@polymer/iron-jsonp-library/iron-jsonp-library.js';

class SampleElement extends PolymerElement {
  static get template() {
    return html`
      <iron-jsonp-library
            library-url="https://apis.google.com/js/plusone.js?onload=%%callback%%"
            notify-event="api-load"
            library-loaded="{{loaded}}">
        </iron-jsonp-library>
        <span>Library Loaded: [[loaded]]</span>
    `;
  }
}
customElements.define('sample-element', SampleElement);
```

## Contributing
If you want to send a PR to this element, here are
the instructions for running the tests and demo locally:

### Installation
```sh
git clone https://github.com/PolymerElements/iron-jsonp-library
cd iron-jsonp-library
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