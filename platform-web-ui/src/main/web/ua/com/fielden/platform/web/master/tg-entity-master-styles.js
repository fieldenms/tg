import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';

const styleElement = document.createElement('dom-module');
styleElement.innerHTML = `
    <template>
        <style>
             tg-entity-master[with-dimensions]::shadow #masterContainer{
                position: absolute;
                top: 0;
                bottom: 0;
                right: 0;
                left: 0;
            }
            .property-action-icon {
                --tg-ui-action-icon-button-width: 24px;
                --tg-ui-action-icon-button-height: 24px;
                --tg-ui-action-icon-button-padding: 4px;
                --tg-ui-action-spinner-width: 20px; 
                --tg-ui-action-spinner-height: 20px; 
                --tg-ui-action-spinner-min-width: 20px; 
                --tg-ui-action-spinner-min-height: 20px; 
                --tg-ui-action-spinner-max-width: 20px; 
                --tg-ui-action-spinner-max-height: 20px; 
                --tg-ui-action-spinner-padding: 0px;
                --tg-ui-action-spinner-margin-left: 0;
            }
        </style>
    </template>
`;
styleElement.register('tg-entity-master-styles');