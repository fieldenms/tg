import {FullCalendar} from '/resources/components/fullcalendar/fullcalendar-component.js';
import '/resources/components/fullcalendar/fullcalendar-style.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

const template = html`
    <style include='fullcalendar-style'>
        :host {
            width: 100%;
            height: 100%;
            position: relative;
            @apply --layout-vertical;
        }
        #calendarContainer {
            z-index: 0;
            margin: 40px; 
            @apply --layout-vertical;
            @apply --layout-flex;
        }
        .toolbar {
            padding: 16px 32px 0 32px;
            height: 20px; /* this is the reduced height to compensate the height of invisible labels for boolean editors beneath */
            @apply --layout-horizontal;
            @apply --layout-end-justified;
            @apply --layout-center;
        }
        .toolbar ::slotted(.orange) {
            color: var(--paper-orange-500);
            border-color: var(--paper-orange-500);
        }
        .toolbar ::slotted(paper-icon-button.revers) {
            transform: scale(-1, 1);
        }
        .legend {
            margin: 0 40px;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
            @apply --layout-wrap;
        }
        .legend-item {
            font-size: smaller;
            margin-right: 16px;
            margin-bottom: 16px;
            @apply --layout-horizontal;
            @apply --layout-center-center;
        }
        .legend-item-point {
            width: 0;
            height: 0;
            border-width: 8px;
            border-radius: 8px;
            border-style: solid; 
        }
        .legend-title-container {
            margin-left: 8px;
            @apply --layout-vertical;
        }
        .lock-layer {
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }
        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <div class="toolbar">
        <slot name="standart-action"></slot>
    </div>
    <div id="calendarContainer"></div>
    <div class="legend">
        <template is="dom-repeat" items="[[_calcLegendItems(entities, colorProperty, colorTitleProperty, colorDescProperty)]]">
            <div class="legend-item">
                <div class="legend-item-point" style$="[[_calcLegendItemStyle(item)]]"></div>
                <div class="legend-title-container">
                    <b>[[item.key]]</b>
                    <i>[[item.desc]]</i>
                </div>
            </div>
        </template>
    </div>
    <div class="lock-layer" lock$="[[lock]]"></div>`;
    
class TgFullcalendar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            entities: {
                type: Array,
                value: () => []
            },
            eventKeyProperty: String,
            eventDescProperty: String,
            eventFromProperty: String,
            eventToProperty: String,
            colorProperty: String,
            colorTitleProperty: String,
            colorDescProperty: String,

            centreState: {
                type: Object,
                observer: "_centreStateChanged"
            },

            /**
             * Need for locking tg-fullcalendar component during data loading.
             */
             lock: {
                type: Boolean,
                value: false
            },

            _calendar: Object
        };
    }

    static get observers() {
        return [
          '_updateEventSource(entities, eventKeyProperty, eventDescProperty, eventFromProperty, eventToProperty, _calendar)'
        ];
    }

    ready () {
        super.ready();

        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        this._calendar = new FullCalendar.Calendar(this.$.calendarContainer, {
            initialView: 'dayGridMonth',
            headerToolbar: {
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            eventTimeFormat: {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            },
            height: 'auto',
          });
          this._calendar.render();
    }

    _updateEventSource(entities, eventKeyProperty, eventDescProperty, eventFromProperty, eventToProperty, _calendar) {
        if (allDefined(entities, eventKeyProperty, eventDescProperty, eventFromProperty, eventToProperty) && _calendar) {
            _calendar.getEvents().forEach(event => event.remove());
            let startTime = Infinity;
            entities.forEach(entity => {
                if (startTime > entity.get(eventFromProperty)) {
                    startTime = entity.get(eventFromProperty);
                }
                const eventColor = this.colorProperty ? entity.get(this.colorProperty) : undefined;
                _calendar.addEvent({
                    title: entity.get(eventKeyProperty) + (eventDescProperty ? " - "+ entity.get(eventDescProperty) : ""),
                    start: entity.get(eventFromProperty),
                    end: entity.get(eventToProperty),
                    backgroundColor: eventColor ? '#' + eventColor["hashlessUppercasedColourValue"]  : "#3788d8",
                });
                if (startTime && startTime < Infinity) {
                    _calendar.gotoDate(startTime);
                }
            });
            _calendar.render();
        } else if (_calendar){
            _calendar.gotoDate(new Date());
            _calendar.getEvents().forEach(event => event.remove());
            _calendar.render();
        }
    }

    _centreStateChanged (centreState) {
        if (centreState === "VIEW") {
            this.lock = true;
        } else {
            this.lock = false;
        }
    }

    _resizeEventListener () {
        if (this._calendar) {
            this._calendar.render();
        }
    }

    _calcLegendItems(entities, colorProperty, colorTitleProperty, colorDescProperty) {
        if (!entities || !colorProperty || !colorTitleProperty || !colorDescProperty) {
            return {};           
        };
        const colorEntity = {};
        entities.forEach(entity => {
            const title = entity.get(colorTitleProperty);
            if (!colorEntity.hasOwnProperty(title)) {
                colorEntity[title] = entity;
            }
        });

        return Object.keys(colorEntity)
            .map(key => {
                const entity = colorEntity[key];
                const eventColor = this.colorProperty ? entity.get(this.colorProperty) : undefined;
                return {
                    key: key,
                    desc: entity.get(colorDescProperty),
                    color: eventColor ? '#' + eventColor["hashlessUppercasedColourValue"]  : "#3788d8"
                }
            }) // let's also sort the data first by colour and then by key
            .sort((a, b) => {
                const colourA = parseInt(a.color.substring(1), 16);
                const colourB = parseInt(b.color.substring(1), 16)
                const byColour = colourB - colourA;
                return byColour !== 0 ? byColour : a.key.localeCompare(a.key);
            });
    }

    _calcLegendItemStyle(item) {
        return "border-color: " + item.color + ";";
    }
}

customElements.define('tg-fullcalendar', TgFullcalendar);