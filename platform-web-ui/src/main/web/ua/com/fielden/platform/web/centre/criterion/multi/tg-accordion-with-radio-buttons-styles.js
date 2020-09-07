import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

const styleElement = document.createElement('dom-module');
styleElement.innerHTML = `
    <template>
        <style>
            tg-accordion {
                margin-bottom: 20px;
                --tg-accordion-selected-heading-background-color: var(--paper-light-blue-700);
                --tg-accordion-selected-heading-color: white;
                --tg-accordion-selected-label-color: white;
            }
            paper-radio-button {
                margin: 10px;
                --paper-radio-button-checked-color: var(--paper-light-blue-700);
                --paper-radio-button-checked-ink-color: var(--paper-light-blue-700);
                font-family: 'Roboto', 'Noto', sans-serif;
            }
            paper-radio-button {
                --calculated-paper-radio-button-ink-size: 36px;
            }
        </style>
    </template>
`;
styleElement.register('tg-accordion-with-radio-buttons-styles');