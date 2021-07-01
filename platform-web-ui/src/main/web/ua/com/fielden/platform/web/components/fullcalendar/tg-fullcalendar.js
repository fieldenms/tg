import {FullCalendar} from '/resources/components/fullcalendar/fullcalendar-component.js';
import '/resources/components/fullcalendar/fullcalendar-style.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';

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
        .toolbar {
            margin: 8px;
            @apply --layout-horizontal;
            @apply --layout-center
            @apply --layout-justified;
            @apply --layout-wrap;
        }
        .left-toolbar {
            whitespace: nowrap
            @apply --layout-horizontal;
        }
        .centre-toolbar {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-weight: bold;
            font-size: 1.5rem;
            @apply --layout-horizontal;
        }
        .right-toolbar {
            @apply --layout-horizontal;
        }
        #calendarContainer {
            z-index: 0;
            margin: 8px; 
            min-height: 0;
            overflow: auto;
            @apply --layout-vertical;
            @apply --layout-flex;
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
        .fc-event {
            cursor: pointer;
        }
        .fc-event-time, .fc-event-title {
            text-overflow: ellipsis;
        }
    </style>
    <slot id="editAction" name="calendar-action"></slot>
    <div class="toolbar">
        <div class="left-toolbar">
            <paper-icon-button icon="chevron-left" on-tap="_prev"></paper-icon-button>
            <paper-icon-button icon="chevron-right" on-tap="_next"></paper-icon-button>
            <paper-button raised on-tap="_today"><span>today</span></paper-button>
        </div>
        <span class="centre-toolbar">[[calendarTitle]]</span>
        <div class="right-toolbar">
            <paper-button raised toggles on-tap="_monthView" active$="[[_isActive(currentView, 'dayGridMonth')]]"><span>month</span></paper-button>
            <paper-button raised toggles on-tap="_weekView" active$="[[_isActive(currentView, 'timeGridWeek')]]"><span>week</span></paper-button>
            <paper-button raised toggles on-tap="_dayView" active$="[[_isActive(currentView, 'timeGridDay')]]"><span>day</span></paper-button>
        </div>
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
    </div>`;
    
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
            currentView: {
                type: String,
                observer: '_currentViewChanged'
            },
            _calendar: Object,
            _editAction: Object,
            _reflector: Object,
            _appConfig: Object
        };
    }

    static get observers() {
        return [
          '_updateEventSource(entities, eventKeyProperty, eventDescProperty, eventFromProperty, eventToProperty, _calendar)'
        ];
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
    }

    ready () {
        super.ready();

        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        this._calendar = new FullCalendar.Calendar(this.$.calendarContainer, {
            initialView: 'dayGridMonth',
            headerToolbar: false,
            datesSet: (dataInfo) => {
                this.calendarTitle = dataInfo.view.title;
            },
            eventClick: (eventInfo) => {
                if (this._editAction && eventInfo.event.extendedProps.entity) {
                    this._editAction.currentEntity = () => eventInfo.event.extendedProps.entity;
                    this._editAction._run();
                }
            },
            eventDidMount: (eventInfo) => {
                if (eventInfo.event.extendedProps.entity && eventInfo.el) {
                    const titleValues = [];
                    const entity = eventInfo.event.extendedProps.entity;
                    titleValues.push({
                        title: this._reflector.getEntityTypeProp(entity, "key").title(),
                        value: "<b>" + entity.get("key") + "</b><br><i>" + entity.get("desc") + "</i>"
                    });
                    titleValues.push({
                        title: this._reflector.getEntityTypeProp(entity, "waType").title(),
                        value: "<b>" + entity.get("waType.key") + "</b><br><i>" + entity.get("waType.desc") + "</i>"
                    });
                    entity.get(this.eventFromProperty) && titleValues.push({
                        title: this._reflector.getEntityTypeProp(entity, this.eventFromProperty).title(),
                        value: "<b>" + this._reflector.tg_toString(entity.get(this.eventFromProperty), entity.type(), this.eventFromProperty, { display: true, locale: this._appConfig.locale }) + "</b>"
                    });
                    entity.get(this.eventToProperty) && titleValues.push({
                        title: this._reflector.getEntityTypeProp(entity, this.eventToProperty).title(),
                        value: "<b>" + this._reflector.tg_toString(entity.get(this.eventToProperty), entity.type(), this.eventToProperty, { display: true, locale: this._appConfig.locale }) + "</b>"
                    });
                    titleValues.push({
                        title: "With action",
                        value: "<b>Work Activity</b><br><i>Edit Work Activity</i>"
                    });
                    const tooltip = "<table>" +
                        titleValues.map(entry => "<tr><td valign='top'>" + entry.title + ": </td><td valign='top'>" + entry.value + "</td></tr>").join("\n") +
                    "</table>";
                    eventInfo.el.setAttribute("tooltip-text", tooltip);
                }
            },
            eventTimeFormat: {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            },
            height: 'auto',
          });
          this._calendar.render();
          this.currentView = 'dayGridMonth';
          //Initialising edit action
          this._editAction = this.$.editAction.assignedNodes()[0]
    }

    _prev() {
        if (this._calendar) {
            this._calendar.prev();
        }
    }

    _next() {
        if (this._calendar) {
            this._calendar.next();
        }
    }

    _today() {
        if (this._calendar) {
            this._calendar.today();
        }
    }

    _monthView() {
        this.currentView = "dayGridMonth";
    }

    _weekView() {
        this.currentView = "timeGridWeek";
    }

    _dayView() {
        this.currentView = "timeGridDay";
    }

    _isActive(currentView, viewName) {
        return currentView === viewName;
    }

    _currentViewChanged(newView) {
        if (this._calendar) {
            this._calendar.changeView(newView);
        }
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
                    extendedProps: {
                        entity: entity
                    },
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