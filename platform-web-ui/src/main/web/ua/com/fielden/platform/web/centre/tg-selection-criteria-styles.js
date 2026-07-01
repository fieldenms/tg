import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';

const styleElement = document.createElement('dom-module');
styleElement.innerHTML = `
    <template>
        <style>
            .tg-layout * {
                min-width: 48px;
            }
            .tg-layout * .criterion-editors{
                min-width: 0;
            }
        </style>
    </template>
`;
styleElement.register('tg-selection-criteria-styles');