/**
 * @license
 * Copyright (c) 2018 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */
/**
 * Registers an extension that extends the global `Mocha` implementation
 * with new helper methods. These helper methods will be added to the `window`
 * when tests run for both BDD and TDD interfaces.
 */
export declare function extendInterfaces(helperName: string, helperFactory: (context: {}, teardown: (cb: () => void) => void, interfaceName: 'tdd' | 'bdd') => void): void;
/**
 * Applies any registered interface extensions. The extensions will be applied
 * as many times as this function is called, so don't call it more than once.
 */
export declare function applyExtensions(): void;
