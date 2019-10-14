import '/resources/polymer/@polymer/paper-fab/paper-fab.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import '/resources/polymer/@polymer/paper-styles/color.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        :host {
            position: relative;
            font: inherit;
            outline: none;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-user-select: none;
            user-select: none;
            cursor: pointer;
            z-index: 0;
        }
        :host([hidden]) {
            display: none !important;
        }
        #spinner {
            position: absolute;
            top: 50%;/*position Y halfway in*/
            left: 50%;/*position X halfway in*/
            transform: translate(-50%,-50%);/*move it halfway back(x,y)*/
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }
    </style>
    <paper-fab mini icon="[[icon]]" title="[[title]]" disabled$="[[isActionInProgress]]"></paper-fab>
    <paper-spinner id="spinner" active="[[isActionInProgress]]" hidden="[[!isActionInProgress]]" alt="in progress"></paper-spinner>`;

export class TgFab extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            icon: String,
            title: String,
            //Indicates whether action is iun progress or not.
            isActionInProgress: {
                type: Boolean,
                value: false
            } 
        };
    }

    ready () {
        super.ready();
    }
}

customElements.define('tg-fab', TgFab);