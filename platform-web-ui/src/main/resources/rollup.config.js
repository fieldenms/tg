import { nodeResolve } from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import json from '@rollup/plugin-json';
import css from 'rollup-plugin-import-css';

const unprocessedFiles = [
    // Separate testing dependency -- we also tree shake it because placed under '@polymer/' umbrella.
    'node_modules/@polymer/test-fixture/test-fixture.js',

    // Separate production files, that are used through <script src='/resources/polymer/ ... /*.js'></script>.
    'node_modules/@webcomponents/webcomponentsjs/webcomponents-bundle.js',
    'node_modules/web-animations-js/web-animations-next-lite.min.js'
];

export default {
    input: [
        // Polymer, its elements and Google elements.
        'build-polymer.js',

        // Our other libraries.
        'node_modules/lib/antlr-lib.js',
        'node_modules/lib/fullcalendar-lib.js',
        'node_modules/lib/moment-lib.js',
        'node_modules/lib/toastui-editor-lib.js',
        'node_modules/lib/html5-qrcode-lib.js',

        // Polymer import files, that we are using through function imports, are listed here.
        // They can not be added to 'build-polymer.js' because we would need to somehow use those functions there for them to be not tree-shaken.
        'node_modules/@polymer/polymer/lib/legacy/class.js', // 'mixinBehaviors' is required (and not used transitively)
        'node_modules/@polymer/polymer/lib/mixins/gesture-event-listeners.js', // (used transitively)
        'node_modules/@polymer/polymer/lib/utils/flush.js', // (used transitively)
        'node_modules/@polymer/polymer/lib/utils/render-status.js', // 'beforeNextRender' is required (and not used transitively)
        'node_modules/@polymer/polymer/lib/utils/async.js', // (used transitively)
        'node_modules/@polymer/polymer/lib/utils/flattened-nodes-observer.js', // (used transitively)
        'node_modules/@polymer/polymer/lib/utils/html-tag.js', // (used transitively)
        'node_modules/@polymer/polymer/lib/legacy/polymer-fn.js', // (used transitively)
        'node_modules/@polymer/polymer/polymer-element.js', // (used transitively)

        // These two files just perform imports with side effects and that's why they are tree-shaken if placed in 'build-polymer.js'.
        'node_modules/@polymer/paper-styles/paper-styles.js',
        'node_modules/@polymer/paper-styles/paper-styles-classes.js',

        ...unprocessedFiles
    ],
    output: {
        dir: 'build',
        format: 'es',
        preserveModules: true
    },
    plugins: [
        nodeResolve({ // plugin to resolve node-like imports (without /, ./ and ../); default 'extensions': ['.mjs', '.js', '.json', '.node']
            browser: true // required for resolving browser-related file in libs like antlr4 (see 'exports' section in 'package.json' in https://www.npmjs.com/package/antlr4?activeTab=code)
        }),
        commonjs({ // plugin to convert CommonJS modules to ES modules (see 'moment' and 'moment-timezone')
            exclude: [ ...unprocessedFiles ]
        }),
        css(), // plugin to be able to import CSS files (see '@toast-ui/editor')
        json() // plugin to be able to import JSON files (see 'moment-timezone')
    ]
};
