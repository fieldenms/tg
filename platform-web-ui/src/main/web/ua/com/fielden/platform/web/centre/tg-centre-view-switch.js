import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-icons/hardware-icons.js';
import '/resources/polymer/@polymer/iron-icons/image-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        .view-item {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
    </style>
    <div class="view-item" on-tap="_showViews">
        <iron-icon icon="[[_currentView.icon]]"></iron-icon>
        <span>[[_currentView.title]]</span>
        <iron-icon icon="icons:arrow-drop-down"></iron-icon>
    </div>
    <iron-dropdown id="dropdown" always-on-top>
        <template is="dom-repeat" items="[[_hiddenViewes]]" as="view">
            <div class="view-item" view-index$="[[view.index]]" on-tap="_changeView">
                <iron-icon icon="[[view.icon]]"></iron-icon>
                <span>[[view.title]]</span>
            </div>
        </template>
    </iron-dropdown>`;


export class TgCentreViewSwitch extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            viewIndex: Number, 
            viewes: Array,
            _currentView: Object,
            _hiddenViewes: Array
        };
    }

    static get observers() {
        return [
            "_updateViewes(viewes, viewIndex)"
        ];
    }

    ready() {
        super.ready();
    }

    _updateViewes(viewes, viewIndex) {
        if (allDefined(arguments) && viewIndex !== null && viewIndex >= 0 && viewes.length > viewIndex) {
            this._hiddenViewes = this.viewes.filter(view => {
                return view.index !== viewIndex;
            });
            this._currentView = this.viewes.find(view => view.index === viewIndex);
        }
    }

    _showViews(e) {
        this.$.dropdown.open();
    }

    _changeView(e) {
        this.$.dropdown.close();
        this.fire("tg-centre-view-change", +e.target.getAttribute("view-index"));
    }
}

customElements.define('tg-centre-view-switch', TgCentreViewSwitch);

