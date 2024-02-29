import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

export const TgBackButtonBehavior = {
    
    /**
     * Creates dynamically the 'dom-bind' template which holds back button.
     */
    createBackButton: function () {
        const domBind = document.createElement('dom-bind');
        domBind._back = this._back;
        
        const templateDom = document.createElement('template');
        templateDom.innerHTML = '<paper-icon-button id="backButton" icon="arrow-back" tooltip-text="Back" on-tap="_back"></paper-icon-button>';
        
        domBind.appendChild(templateDom);
        return domBind;
    },
    
    /**
     * Performs going back from this dialog, which means dialog closing if it is possible.
     */
    _back: function () {
        history.back();
    }
    
};