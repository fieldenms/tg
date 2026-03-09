import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/centre/criterion/tg-abstract-criterion.js';
import { TgAbstractCriterionBehavior } from '/resources/centre/criterion/tg-abstract-criterion-behavior.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

const template = html`
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-abstract-criterion id="dom"
        mnemonics-visible="[[mnemonicsVisible]]"
        exclude-missing="[[excludeMissing]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]"
        _or-null="{{_orNull}}"
        _not="{{_not}}">
        <div slot="criterion-editors" class="layout horizontal flex criterion-editors" style="margin-right:20px;">
            <slot name="range-criterion-editor-1"></slot>
        </div>
        <div slot="criterion-editors" class="layout horizontal flex criterion-editors">
            <slot name="range-criterion-editor-2"></slot>
        </div>
    </tg-abstract-criterion>
`;

Polymer({
    _template: template,

    is: 'tg-boolean-criterion',

    observers: [
        '_updateIconButtonStyle(orNull, not, orGroup)'
    ],

    behaviors: [ TgAbstractCriterionBehavior ],

    ready: function () {
        this.orGroupOpened = true;
    },

    _hasNoValue: function() {
        const everyEditorEquals = (editors, val) => {
            return editors.every(editor => editor.convertFromString(editor._editingValue) === val);
        };
        const editors = [...this._dom().querySelectorAll("slot")].map(editorSlot => editorSlot.assignedNodes()[0]).filter(editor => !!editor);
        return everyEditorEquals(editors, true) || everyEditorEquals(editors, false);
    },

    _dom: function () {
        return this.$.dom;
    }
});