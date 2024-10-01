import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';
import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { TgViewWithHelpBehavior } from '/resources/components/tg-view-with-help-behavior.js';

import '/resources/master/tg-entity-master-styles.js';

const template = html`
    <style include="tg-entity-master-styles"></style> <!-- imported as part of tg-entity-master-template-behavior to reduce the size of resultant generated file -->
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
        _initiate-help-action="[[_helpMouseDownEventHandler]]"
        _run-help-action="[[_helpMouseUpEventHandler]]"
        _has-embeded-view="[[_hasEmbededView]]"
        _saver-loading="{{_saverLoading}}">
        <tg-flex-layout when-desktop='[[_desktopLayout_editors]]' when-tablet='[[_tabletLayout_editors]]' when-mobile='[[_mobileLayout_editors]]' slot='property-editors' context='[[_currEntity]]'>
            <tg-rich-text-editor id='editor_4_richTextProp' entity='{{_currBindingEntity}}' original-entity='{{_originalBindingEntity}}' previous-modified-properties-holder='[[_previousModifiedPropertiesHolder]]' property-name='[[propName]]' validation-callback='[[validate]]' prop-title='[[propTitle]]' prop-desc='[[propDesc]]' current-state='[[currentState]]' toaster='[[toaster]]' height='350px' entity-type='[[entityType]]'></tg-rich-text-editor>
        </tg-flex-layout>
        <tg-flex-layout when-desktop='[[_desktopLayout_actions]]' slot='action-bar'>
            <tg-action entity-type='[[_actions.REFRESH.entityType]]' entity='[[_currEntity]]' enabled-states='[[_actions.REFRESH.enabledStates]]' short-desc='[[_actions.REFRESH.shortDesc]]' long-desc='[[_actions.REFRESH.longDesc]]' close-after-execution current-state='[[currentState]]' shortcut='ctrl+x meta+x ctrl+shift+x meta+shift+x ctrl+alt+x meta+alt+x' role='refresh' event-channel='[[centreUuid]]' action='[[_actions.REFRESH.action]]' new-action='[[_actions.REFRESH.newAction]]' post-action='{{_postRetrievedDefault}}' post-action-error='{{_postRetrievedDefaultError}}'></tg-action>
            <tg-action entity-type='[[_actions.SAVE.entityType]]' entity='[[_currEntity]]' enabled-states='[[_actions.SAVE.enabledStates]]' short-desc='[[_actions.SAVE.shortDesc]]' long-desc='[[_actions.SAVE.longDesc]]' close-after-execution current-state='[[currentState]]' shortcut='ctrl+s meta+s ctrl+shift+s meta+shift+s ctrl+alt+s meta+alt+s' id='_saveAction' role='save' focusing-callback='[[focusViewBound]]' event-channel='[[centreUuid]]' action='[[_actions.SAVE.action]]' new-action='[[_actions.SAVE.newAction]]' post-action='{{_postSavedDefault}}' post-action-error='{{_postSavedDefaultError}}'></tg-action>
        </tg-flex-layout>
    </tg-entity-master>
    <tg-ui-action
        id="tgOpenHelpMasterAction"
        ui-role='ICON'
        component-uri = '/master_ui/ua.com.fielden.platform.entity.UserDefinableHelp'
        element-name = 'tg-UserDefinableHelp-master'
        short-desc="Help"
        show-dialog='[[_showHelpDialog]]'
        toaster='[[toaster]]'
        create-context-holder='[[_createContextHolder]]'
        attrs='[[_tgOpenHelpMasterActionAttrs]]'
        require-selection-criteria='false'
        require-selected-entities='ONE'
        require-master-entity='false'
        current-entity = '[[_currentEntityForHelp]]'
        modify-functional-entity = '[[_modifyHelpEntity]]'
        post-action-success = '[[_postOpenHelpMasterAction]]'
        hidden>
    </tg-ui-action>`;

template.setAttribute('strip-whitespace', '');


Polymer({
    _template: template,

    is: 'tg-rich-text-prop-edit-master',

    properties: {
        
    },

    behaviors: [TgEntityMasterBehavior, TgViewWithHelpBehavior],
    
    /**
     * Initialisation block. It has all children web components already initialised.
     */
    created: function () {
        this.uuid = this.is + '/' + generateUUID();
        this.prefDim = {'width': function() {return '50%'}, 'height': function() {return '70%'}, 'widthUnit': '', 'heightUnit': ''};
        this.noUI = false;
        this.saveOnActivation = false;

        this._desktopLayout_editors = ["height:100%", "box-sizing:border-box", "min-height:fit-content", "padding:20px", [["flex"]]];
        this._tabletLayout_editors = ["height:100%", "box-sizing:border-box", "min-height:fit-content", "padding:20px", [["flex"]]];
        this._mobileLayout_editors = ["height:100%", "box-sizing:border-box", "min-height:fit-content", "padding:20px", [["flex"]]];

        this._desktopLayout_actions = ['horizontal', 'padding: 10px', 'wrap', 'justify-content: center',,['margin: 10px', 'width: 80px'],['margin: 10px', 'width: 80px']];
    },

    ready: function() {
        this._currentEntityForHelp = () => {
            return () => this._currEntity;
        };
        
    },

    //Entity master related functions

    _masterDom: function () {
        return this.$.masterDom;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._masterDom()._ajaxRetriever();
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this._masterDom()._ajaxSaver();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._masterDom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._masterDom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._masterDom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._masterDom()._toastGreeting();
    },

    getOpenHelpMasterAction: function () {
        return this.$.tgOpenHelpMasterAction;
    },
});
    