<!--@imports-->
import { TgEntityMasterTemplateBehavior, Polymer, html } from '/resources/master/tg-entity-master-template-behavior.js';

// <!-- TODO this import should be generated <link rel="import" href="/resources/master/actions/tg-action.html"> -->

const template = html`<!-- TODO layout vertical -->
    <style include="tg-entity-master-styles"></style> <!-- imported as part of tg-entity-master-template-behavior to reduce the size of resultant generated file -->
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
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
    </tg-entity-master>
`;

Polymer({
    _template: template,

    is: 'tg-@entity_type-master',

    behaviors: [TgEntityMasterTemplateBehavior],

    created: function () {
        const self = this;
        self.prefDim = @prefDim;
        self.noUI = @noUiValue;
        self.saveOnActivation = @saveOnActivationValue;
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        const self = this;
        //START OF GENERATED JS LOGIC
        //@ready-callback
        //END OF GENERATED JS LOGIC

        //@master-is-ready-custom-code

    },// end of ready

    attached: function () {
        const self = this;
        self.async(function () {
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
    }
});