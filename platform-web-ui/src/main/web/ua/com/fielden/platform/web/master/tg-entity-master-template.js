<!--@imports-->

import '/resources/actions/tg-ui-action.js';

import { TgEntityMasterTemplateBehavior, Polymer, html } from '/resources/master/tg-entity-master-template-behavior.js';
import { TgReflector } from '/app/tg-reflector.js';
import { getParentAnd } from '/resources/reflection/tg-polymer-utils.js'; // required by BindSavedPropertyPostActionSuccess/Error handlers

// <!-- TODO this import should be generated <link rel="import" href="/resources/master/actions/tg-action.html"> -->

const template = html`<!-- TODO layout vertical -->
    <style include="tg-entity-master-styles"></style> <!-- imported as part of tg-entity-master-template-behavior to reduce the size of resultant generated file -->
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-ui-action
        id="tgOpenMasterAction"
        ui-role='ICON'
        show-dialog='[[_showDialog]]'
        toaster='[[toaster]]'
        create-context-holder='[[_createContextHolder]]'
        dynamic-action
        attrs='[[_tgOpenMasterActionAttrs]]'
        require-selection-criteria='false'
        require-master-entity='true'
        hidden>
    </tg-ui-action>
    <tg-ui-action
        id="tgOpenHelpMasterAction"
        ui-role='ICON'
        component-uri = '/master_ui/ua.com.fielden.platform.entity.UserDefinableHelp'
        element-name = 'tg-UserDefinableHelp-master'
        show-dialog='[[_showHelpDialog]]'
        toaster='[[toaster]]'
        create-context-holder='[[_createContextHolder]]'
        attrs='[[_tgOpenHelpMasterActionAttrs]]'
        require-selection-criteria='false'
        require-selected-entities='ONE'
        require-master-entity='false'
        current-entity = '[[_currentEntityForHelp()]]'
        pre-action = '[[_preOpenHelpMasterAction]]'
        modify-functional-entity = '[[_modifyHelpEntity]]'
        post-action-success = '[[_postOpenHelpMasterAction]]'
        hidden>
    </tg-ui-action>
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
        _initiate-help-action="[[_helpMouseDownEventHandler]]"
        _run-help-action="[[_helpMouseUpEventHandler]]"
        _has-embeded-view="[[_hasEmbededView]]"
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
        self.keyEventTarget = self._getKeyEventTarget();
        if (self._shouldOverridePrefDim()) {
            self.keyEventTarget.prefDim = self.prefDim;
        }
        self.async(function () {

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
    _ownKeyBindings: {
        '@SHORTCUTS': '_shortcutPressed'
    }
});