import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

<!--@imports-->
import {TgChartDeckerTemplateBehavior} from '/resources/components/chart-decker/tg-chart-decker-template-behavior.js'
import '/resources/master/tg-entity-master-styles.js';

const template = html`
    <style include="tg-entity-master-styles"></style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-entity-master
        id="masterDom"
        entity-type="[[entityType]]" 
        entity-id="[[entityId]]"
        _post-validated-default="[[_postValidatedDefault]]"
        _post-validated-default-error="[[_postValidatedDefaultError]]"
        _process-response="[[_processResponse]]"
        _process-error="[[_processError]]"
        _process-retriever-response="[[_processRetrieverResponse]]"
        _process-retriever-error="[[_processRetrieverError]]"
        _process-saver-response="[[_processSaverResponse]]"
        _process-saver-error="[[_processSaverError]]"
        _saver-loading="{{_saverLoading}}">
        <!--START OF GENERATED TG-ENTITY-MASTER DOM CONTENT-->
        <!--@tg-entity-master-content-->
        <!--END OF GENERATED TG-ENTITY-MASTER DOM CONTENT-->
    </tg-entity-master>`;


Polymer({
    _template: template,

    is: 'tg-@entity_type-master',
    
    behaviors: [TgChartDeckerTemplateBehavior],
    
    /**
     * Initialisation block. It has all children web components already initialised.
     */
    created: function () {
        var self = this;
        self.prefDim = @prefDim;
        self.noUI = @noUiValue;
        self.saveOnActivation = @saveOnActivationValue;
    },

    ready: function () {
        const self = this;
        
        //START OF GENERATED JS LOGIC
        //@ready-callback
        //END OF GENERATED JS LOGIC
        
        //@master-is-ready-custom-code
        
    },// end of ready
    
    attached: function () {
        var self = this;
        self.async(function() {
            self.keyEventTarget = self._getKeyEventTarget();
            if (self._shouldOverridePrefDim()) {
                self.keyEventTarget.prefDim = self.prefDim;
            }
            
            self.primaryAction = [
                //generatedPrimaryActions
            ];
            // TODO may not be needed due to self.primaryAction collection all the custom actions
            self.propActions = [
                //generatedPropActions
            ];
        }, 1);
        
        //@attached-callback
        
        //@master-has-been-attached-custom-code
        
    },
    
    /**
     * Generated shortcut bindings to single function _shortcutPressed, which locates appropriate tg-action / tg-ui-action and invokes its function 'shortcutPressed'.
     */
    keyBindings: {
        '@SHORTCUTS': '_shortcutPressed'
    },
    
});
    