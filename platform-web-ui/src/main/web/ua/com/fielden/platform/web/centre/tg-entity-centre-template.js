console.error('load');
console.time('load');
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

import '/resources/egi/tg-entity-grid-inspector.js';
import '/resources/centre/tg-selection-criteria.js';
import { TgSelectionCriteriaTemplateBehavior } from '/resources/centre/tg-selection-criteria-template-behavior.js';
import '/resources/centre/tg-entity-centre.js';
import '/resources/centre/tg-entity-centre-styles.js';
import '/resources/centre/tg-selection-criteria-styles.js';
import { TgEntityCentreTemplateBehavior } from '/resources/centre/tg-entity-centre-template-behavior.js';
import '/resources/centre/tg-entity-centre-insertion-point.js';

const selectionCritTemplate = html`
    <style include="tg-selection-criteria-styles"></style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <tg-selection-criteria id="masterDom" mi-type="[[miType]]" save-as-name="[[saveAsName]]" query-part="[[queryPart]]" is-running="{{isRunning}}" _post-validated-default="[[_postValidatedDefault]]" _post-validated-default-error="[[_postValidatedDefaultError]]" _process-response="[[_processResponse]]" _process-error="[[_processError]]" _process-retriever-response="[[_processRetrieverResponse]]" _process-retriever-error="[[_processRetrieverError]]" _process-runner-response="[[_processRunnerResponse]]" _process-runner-error="[[_processRunnerError]]">
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
    },

    attached: function () {
        console.error('load');
        console.timeEnd('load');
    }
});

const entityCentreTemplate = html`
    <style include="tg-entity-centre-styles"></style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <style>
        /*toolbarStyles*/
    </style>
    <tg-entity-centre id="dom" _selected-view="{{_selectedView}}" _url="[[_url]]" _bind-centre-info="[[_bindCentreInfo]]" _process-discarder-response="[[_processDiscarderResponse]]" _process-discarder-error="[[_processDiscarderError]]" _saver-disabled="[[_saverDisabled]]" _discarder-disabled="[[_discarderDisabled]]" _runner-disabled="[[_runnerDisabled]]" _viewer-disabled="[[_viewerDisabled]]" save="[[save]]" discard="[[discard]]" run="[[run]]" _activate-result-set-view="[[_activateResultSetView]]" stale-criteria-message="[[staleCriteriaMessage]]" _show-dialog="[[_showDialog]]" save-as-name="{{saveAsName}}" _create-context-holder="[[_createContextHolder]]" uuid="[[uuid]]">
        <tg-@mi_type-selection-criteria id="selection_criteria" slot="custom-selection-criteria" _was-run="{{_wasRun}}" _centre-changed="{{_centreChanged}}" _edited-props-exist="{{_editedPropsExist}}" _criteria-loaded="{{_criteriaLoaded}}" uuid="[[uuid]]" mi-type="[[miType]]" save-as-name="{{saveAsName}}" query-part="[[queryPart]]" page-capacity="@pageCapacity" post-run="[[_postRun]]" get-selected-entities="[[_getSelectedEntities]]" get-master-entity="[[getMasterEntity]]" post-retrieved="[[postRetrieved]]" page-number="{{pageNumber}}" page-count="{{pageCount}}" page-number-updated="{{pageNumberUpdated}}" page-count-updated="{{pageCountUpdated}}" is-running="{{isRunning}}" stale-criteria-message="{{staleCriteriaMessage}}" @queryEnhancerContextConfig></tg-@mi_type-selection-criteria>

        <!--@custom-front-actions-->
        
        <tg-entity-grid-inspector id="egi" slot="custom-egi" class="entity-grid-inspector" centre-selection="[[centreSelection]]" column-properties-mapper="{{columnPropertiesMapper}}" custom-shortcuts="@customShortcuts" visible-row-count="@visibleRowCount" constant-height="@egiHeight" row-height="@egiRowHeight" @fitToHeight @canDragFrom @toolbarVisible @checkboxVisible @dragAnchorFixed @checkboxesFixed @checkboxesWithPrimaryActionsFixed num-of-fixed-cols="@numOfFixedCols" @secondaryActionsFixed @headerFixed @summaryFixed @gridLayout>
            <!-- EGI COLUMNS DOM (GENERATED) -->
            <!--@egi_columns-->

            <!--@toolbar-->

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
        <div slot="bottom-insertion-point">
            <!--@bottom_insertion_points-->
        </div>
        <div slot="top-insertion-point">
            <!--@top_insertion_points-->
        </div>
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
        this.classList.add('generatedCentre');
        // assign the download attachment function to EGI
        this.$.egi.downloadAttachment = this.$.selection_criteria.mkDownloadAttachmentFunction();
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

            // TODO smth. like this should be generated here:
            self.frontActions = [
                //generatedFrontActionObjects
            ];
            self.topLevelActions = [
                //generatedActionObjects
            ];
            // TODO do we need to notify paths?
            // TODO do we need to notify paths?
            self.secondaryActions = [
                //generatedSecondaryActions
            ];

            self.insertionPointActions = [
                //generatedInsertionPointActions
            ];
            self.primaryAction = [
                //generatedPrimaryAction
            ];
            self.propActions = [
                //generatedPropActions
            ];
            //gridLayoutConfig

            //@centre-has-been-attached-custom-code
        }, 1);
    },
});