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

The `<test-fixture>` element can simplify the exercise of consistently
resetting a test suite's DOM. To use it, wrap the test suite's DOM as a
template:

```html
<test-fixture id="SomeElementFixture">
  <template>
    <some-element id="SomeElementForTesting"></some-element>
  </template>
</test-fixture>
```

Now, the `<test-fixture>` element can be used to generate a copy of its
template:

```html
<script>
describe('<some-element>', function () {
  var someElement;

  beforeEach(function () {
    document.getElementById('SomeElementFixture').create();
    someElement = document.getElementById('SomeElementForTesting');
  });
});
</script>
```

Fixtured elements can be cleaned up by calling `restore` on the
`<test-fixture>`:

```javascript
  afterEach(function () {
    document.getElementById('SomeElementFixture').restore();
    // <some-element id='SomeElementForTesting'> has been removed
  });
```

`<test-fixture>` will create fixtures from all of its immediate `<template>`
children. The DOM structure of fixture templates can be as simple or as complex
as the situation calls for.

## Even simpler usage in Mocha

In Mocha, usage can be simplified even further. Include `test-fixture-mocha.js`
after Mocha in the `<head>` of your document and then fixture elements like so:

```html
<script>
describe('<some-element>', function () {
  var someElement;

  beforeEach(function () {
    someElement = fixture('SomeElementFixture');
  });
});
</script>
```

Fixtured elements will be automatically restored in the `afterEach` phase of the
current Mocha `Suite`.

## Data-bound templates

Data-binding systems are also supported, as long as your (custom) template
elements define a `stamp(model)` method that returns a document fragment. This
allows you to stamp out templates w/ custom models for your fixtures.

For example, using Polymer 0.8's `dom-template`:

```html
<test-fixture id="bound">
  <template is="dom-template">
    <span>{{greeting}}</span>
  </template>
</test-fixture>
```

You can pass an optional context argument to `create()` or `fixture()` to pass
the model:

```js
var bound = fixture('bound', {greeting: 'ohai thurr'});
```

## The problem being addressed

Consider the following `web-component-tester` test suite:

```html
<!doctype html>
<html>
<head>
  <title>some-element test suite</title>
</head>
<body>
  <some-element id="SomeElementForTesting"></some-element>
  <script type="module">
import '../some-element.js';

describe('<some-element>', function () {
  var someElement;

  beforeEach(function () {
    someElement = document.getElementById('SomeElementForTesting');
  });

  it('can receive property `foo`', function () {
    someElement.foo = 'bar';
    expect(someElement.foo).to.be.equal('bar');
  });

  it('has a default `foo` value of `undefined`', function () {
    expect(someElement.foo).to.be.equal(undefined);
  });
});
  </script>
</body>
</html>
```

In this contrived example, the suite will pass or fail depending on which order
the tests are run in. It is non-deterministic because `someElement` has
internal state that is not properly reset at the end of each test.

It would be trivial in the above example to simply reset `someElement.foo` to
the expected default value of `undefined` in an `afterEach` hook. However, for
non-contrived test suites that target complex elements, this can result in a
large quantity of ever-growing set-up and tear-down boilerplate.

@pseudoElement test-fixture
 */
var TestFixturePrototype = Object.create(HTMLElement.prototype);
var TestFixtureExtension = {
  _fixtureTemplates: null,

  _elementsFixtured: false,

  get elementsFixtured() {
    return this._elementsFixtured;
  },

  get fixtureTemplates() {
    if (!this._fixtureTemplates) {
      // Copy fixtures to a true Array for Safari 7. This prevents their
      // `content` property from being improperly garbage collected.
      this._fixtureTemplates =
          Array.prototype.slice.apply(this.querySelectorAll('template'));
    }

    return this._fixtureTemplates;
  },

  create: function(model) {
    var generatedDoms = [];

    this.restore();

    this.removeElements(this.fixtureTemplates);

    this.forElements(this.fixtureTemplates, function(fixtureTemplate) {
      generatedDoms.push(this.createFrom(fixtureTemplate, model));
    }, this);

    this.forcePolyfillAttachedStateSynchrony();

    if (generatedDoms.length < 2) {
      return generatedDoms[0];
    }

    return generatedDoms;
  },

  createFrom: function(fixtureTemplate, model) {
    var fixturedFragment;
    var fixturedElements;

    if (!(fixtureTemplate && fixtureTemplate.tagName === 'TEMPLATE')) {
      return;
    }

    try {
      fixturedFragment = this.stamp(fixtureTemplate, model);
    } catch (error) {
      console.error('Error stamping', fixtureTemplate, error);
      throw error;
    }

    fixturedElements = this.collectElementChildren(fixturedFragment);

    this.appendChild(fixturedFragment);
    this._elementsFixtured = true;

    if (fixturedElements.length < 2) {
      return fixturedElements[0];
    }

    return fixturedElements;
  },

  restore: function() {
    if (!this._elementsFixtured) {
      return;
    }

    this.removeElements(this.children);

    this.forElements(this.fixtureTemplates, function(fixtureTemplate) {
      this.appendChild(fixtureTemplate);
    }, this);

    this.generatedDomStack = [];

    this._elementsFixtured = false;

    this.forcePolyfillAttachedStateSynchrony();
  },

  forcePolyfillAttachedStateSynchrony: function() {
    // Force synchrony in attachedCallback and detachedCallback where
    // implemented, in the event that we are dealing with one of these async
    // polyfills:
    // 1. Web Components CustomElements polyfill (v1 or v0).
    if (window.customElements && window.customElements.flush) {
      window.customElements.flush();
    } else if (window.CustomElements && window.CustomElements.takeRecords) {
      window.CustomElements.takeRecords();
    }
    // 2. ShadyDOM polyfill.
    if (window.ShadyDOM && window.ShadyDOM.flush) {
      window.ShadyDOM.flush();
    }
  },

  collectElementChildren: function(parent) {
    // Note: Safari 7.1 does not support `firstElementChild` or
    // `nextElementSibling`, so we do things the old-fashioned way:
    var elements = [];
    var child = parent.firstChild;

    while (child) {
      if (child.nodeType === Node.ELEMENT_NODE) {
        elements.push(child);
      }

      child = child.nextSibling;
    }

    return elements;
  },

  removeElements: function(elements) {
    this.forElements(elements, function(element) {
      this.removeChild(element);
    }, this);
  },

  forElements: function(elements, iterator, context) {
    Array.prototype.slice.call(elements).forEach(iterator, context);
  },

  stamp: function(fixtureTemplate, model) {
    var stamped;
    // Check if we are dealing with a "stampable" `<template>`. This is a
    // vaguely defined special case of a `<template>` that is a custom
    // element with a public `stamp` method that implements some manner of
    // data binding.
    if (fixtureTemplate.stamp) {
      stamped = fixtureTemplate.stamp(model);
      // We leak Polymer specifics a little; if there is an element `root`, we
      // want that to be returned.
      stamped = stamped.root || stamped;

      // Otherwise, we fall back to standard HTML templates, which do not have
      // any sort of binding support.
    } else {
      if (model) {
        console.warn(
            this,
            'was given a model to stamp, but the template is not of a bindable type');
      }

      stamped = document.importNode(fixtureTemplate.content, true);

      // Immediately upgrade the subtree if we are dealing with async
      // Web Components polyfill.
      // https://github.com/Polymer/polymer/blob/0.8-preview/src/features/mini/template.html#L52
      if (window.CustomElements && window.CustomElements.upgradeSubtree) {
        window.CustomElements.upgradeSubtree(stamped);
      }
    }

    return stamped;
  }
};

Object.getOwnPropertyNames(TestFixtureExtension).forEach(function(property) {
  Object.defineProperty(
      TestFixturePrototype,
      property,
      Object.getOwnPropertyDescriptor(TestFixtureExtension, property));
});

try {
  if (window.customElements) {
    function TestFixture() {
      return ((window.Reflect && Reflect.construct) ?
                  Reflect.construct(HTMLElement, [], TestFixture) :
                  HTMLElement.call(this)) ||
          this;
    }
    TestFixture.prototype = TestFixturePrototype;
    // `constructor` is not writable on Safari 9, but is configurable.
    Object.defineProperty(TestFixture.prototype, 'constructor', {
      configurable: true,
      enumerable: true,
      writable: true,
      value: TestFixture,
    });
    window.customElements.define('test-fixture', TestFixture);
  } else {
    document.registerElement('test-fixture', {prototype: TestFixturePrototype});
  }
} catch (e) {
  if (window.WCT) {
    console.warn(
        'if you are using WCT, you do not need to manually import test-fixture.html');
  } else {
    console.warn('test-fixture has already been registered!');
  }
}
