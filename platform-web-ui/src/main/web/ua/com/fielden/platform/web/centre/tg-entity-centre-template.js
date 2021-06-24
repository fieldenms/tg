import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

<!--@imports-->

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-icons/hardware-icons.js';
import '/resources/polymer/@polymer/iron-icons/image-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-entity-grid-inspector.js';
import '/resources/centre/tg-centre-view-switch.js';
import '/resources/master/actions/tg-action.js';
import {TgEgiMasterBehavior} from '/resources/egi/tg-egi-master-behavior.js';
import '/resources/centre/tg-selection-criteria.js';
import { TgSelectionCriteriaTemplateBehavior } from '/resources/centre/tg-selection-criteria-template-behavior.js';
import '/resources/centre/tg-entity-centre.js';
import '/resources/centre/tg-entity-centre-styles.js';
import '/resources/centre/tg-selection-criteria-styles.js';
import { TgEntityCentreTemplateBehavior } from '/resources/centre/tg-entity-centre-template-behavior.js';
import '/resources/centre/tg-entity-centre-insertion-point.js';
import { TgReflector } from '/app/tg-reflector.js';

const selectionCritTemplate = html`
    <style include="tg-selection-criteria-styles"></style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-selection-criteria id="masterDom" mi-type="[[miType]]" save-as-name="[[saveAsName]]" query-part="[[queryPart]]" config-uuid="[[configUuid]]" is-running="{{isRunning}}" _post-validated-default="[[_postValidatedDefault]]" _post-validated-default-error="[[_postValidatedDefaultError]]" _process-response="[[_processResponse]]" _process-error="[[_processError]]" _process-retriever-response="[[_processRetrieverResponse]]" _process-retriever-error="[[_processRetrieverError]]" _process-runner-response="[[_processRunnerResponse]]" _process-runner-error="[[_processRunnerError]]">
        <!--CRITERIA EDITORS DOM (GENERATED)-->
        <!--@criteria_editors-->
    </tg-selection-criteria>
`;

Polymer({
    _template: selectionCritTemplate,

    is: 'tg-@mi_type-selection-criteria',

    behaviors: [ TgSelectionCriteriaTemplateBehavior ],

    ready: function () {
        // LAYOUT CONFIG (GENERATED)
        //@layoutConfig
    }
});

const entityCentreTemplate = html`
    <style include="tg-entity-centre-styles"></style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <style>
        /*toolbarStyles*/
    </style>
    <tg-entity-centre id="dom" _selected-view="[[_selectedView]]" _previous-view="[[_previousView]]" _url="[[_url]]" _centre-dirty-or-edited="[[_centreDirtyOrEdited]]" _action-in-progress="{{_actionInProgress}}" _bind-centre-info="[[_bindCentreInfo]]" _process-discarder-response="[[_processDiscarderResponse]]" _process-discarder-error="[[_processDiscarderError]]" _button-disabled="[[_buttonDisabled]]" _viewer-disabled="[[_viewerDisabled]]" save="[[save]]" discard="[[discard]]" run="[[run]]" _activate-result-set-view="[[_activateResultSetView]]" stale-criteria-message="[[staleCriteriaMessage]]" _show-dialog="[[_showDialog]]" save-as-name="{{saveAsName}}" _create-context-holder="[[_createContextHolder]]" uuid="[[uuid]]">
        <tg-@mi_type-selection-criteria id="selection_criteria" slot="custom-selection-criteria" _centre-dirty-or-edited="{{_centreDirtyOrEdited}}" _was-run="{{_wasRun}}" _criteria-loaded="{{_criteriaLoaded}}" uuid="[[uuid]]" mi-type="[[miType]]" save-as-name="{{saveAsName}}" config-uuid="{{configUuid}}" preferred-view="{{loadedPreferredView}}" query-part="[[queryPart]]" post-run="[[_postRun]]" get-selected-entities="[[_getSelectedEntities]]" get-master-entity="[[getMasterEntity]]" post-retrieved="[[postRetrieved]]" page-number="{{pageNumber}}" page-count="{{pageCount}}" page-number-updated="{{pageNumberUpdated}}" page-count-updated="{{pageCountUpdated}}" is-running="{{isRunning}}" stale-criteria-message="{{staleCriteriaMessage}}" @queryEnhancerContextConfig></tg-@mi_type-selection-criteria>
        
        <!--@custom-front-actions-->
        
        <!--@custom-share-actions-->
        
        <tg-entity-grid-inspector id="egi" slot="custom-egi" class="entity-grid-inspector" centre-selection="[[centreSelection]]" column-properties-mapper="{{columnPropertiesMapper}}" custom-shortcuts="@customShortcuts" constant-height="@egiHeight" row-height="@egiRowHeight" @hidden @fitToHeight @canDragFrom @toolbarVisible @checkboxVisible @dragAnchorFixed @checkboxesFixed @checkboxesWithPrimaryActionsFixed num-of-fixed-cols="@numOfFixedCols" @secondaryActionsFixed @headerFixed @summaryFixed @gridLayout>
            <!-- EGI COLUMNS DOM (GENERATED) -->
            
            <!--@egi_columns-->

            <tg-egi-@mi_type-master slot="egi-master" centre-uuid="[[uuid]]"></tg-egi-@mi_type-master>
            
            <!--@toolbar-->
            <tg-ui-action
                slot="defaultPropertyAction"
                ui-role='ICON'
                show-dialog='[[_showDialog]]'
                toaster='[[toaster]]'
                create-context-holder='[[_createContextHolder]]'
                dynamic-action
                attrs='[[_defaultPropertyActionAttrs]]'
                pre-action='[[navigationPreAction]]'
                require-selection-criteria='false'
                require-selected-entities='ONE'
                require-master-entity='false'
                hidden>
            </tg-ui-action>
            <!--@primary_action-->
            <!--@secondary_actions-->
            <!--@insertion_point_actions-->
        </tg-entity-grid-inspector>

        <div slot="left-insertion-point" class="left-insertion-point">
            <!--@left_insertion_points-->
        </div>
        <div slot="right-insertion-point" class="right-insertion-point">
            <!--@right_insertion_points-->
        </div>
        <div slot="bottom-insertion-point" class="bottom-insertion-point">
            <!--@bottom_insertion_points-->
        </div>
        <div slot="top-insertion-point" class="top-insertion-point">
            <!--@top_insertion_points-->
        </div>
        <!--@alternative_view_insertion_points-->
    </tg-entity-centre>
`;

Polymer({
    _template: entityCentreTemplate,

    is: 'tg-@mi_type-centre',

    behaviors: [ TgEntityCentreTemplateBehavior ],

    hostAttributes: {
        'class': 'layout vertical',
        'entity-type': '@full_entity_type',
        'mi-type': '@full_mi_type'
    },

    ready: function () {
        const self = this;
        this.classList.add('generatedCentre');
        // assign the download attachment function to EGI
        this.$.egi.downloadAttachment = this.$.selection_criteria.mkDownloadAttachmentFunction();
        
        // TODO smth. like this should be generated here:
        this.frontActions = [
            //generatedFrontActionObjects
        ];
        this.shareActions = [
            //generatedShareActionObjects
        ];
        this.topLevelActions = [
            //generatedActionObjects
        ];
        // TODO do we need to notify paths?
        // TODO do we need to notify paths?
        this.secondaryActions = [
            //generatedSecondaryActions
        ];

        this.insertionPointActions = [
            //generatedInsertionPointActions
        ];
        this.primaryAction = [
            //generatedPrimaryAction
        ];
        this.propActions = [
            //generatedPropActions
        ];

        //toolbarGeneratedFunction

        //@centre-is-ready-custom-code

    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    attached: function () {
        console.timeEnd("ready-to-attached");
        console.warn("attached-to-attached-async");
        console.time("attached-to-attached-async");
        const self = this;
        self.async(function () {
            console.warn("tg-@mi_type-centre: attached async");
            console.timeEnd("attached-to-attached-async");

            self.postRetrieved = self.postRetrieved || function (entity, bindingEntity, customObject) {
                console.log("postRetrieved");
            }.bind(self);

            //gridLayoutConfig

            //@centre-has-been-attached-custom-code

        }, 1);
    },
});

const egiMasterTemplate = html`
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
            <!--@egi_editors-->
            <tg-action slot="cancel-button" class="master-cancel-action" is-icon icon="clear" enabled-states='[[_actions.REFRESH.enabledStates]]' short-desc='Cancel' long-desc='Cancel changes' current-state='[[currentState]]' action='[[closeMaster]]' post-action='{{_postClose}}'></tg-action>
            <tg-action slot="save-button" class="master-save-action" is-icon icon="check" enabled-states='[[_actions.SAVE.enabledStates]]' short-desc='Save' long-desc='Save changes' current-state='[[currentState]]' id='_saveAction' action='[[_actions.SAVE.action]]' post-action='{{_postSavedDefault}}' post-action-error='{{_postSavedDefaultError}}'></tg-action>
    </tg-entity-master>`;

Polymer({
    _template: egiMasterTemplate,

    is: 'tg-egi-@mi_type-master',

    behaviors: [ TgEgiMasterBehavior ]
});
