import { FullCalendar, momentTimezonePlugin } from '/resources/polymer/lib/fullcalendar-lib.js';
import moment from '/resources/polymer/lib/moment-lib.js';
import { now } from '/resources/reflection/tg-date-utils.js';
import '/resources/components/tg-dropdown-switch.js';
import '/resources/layout/tg-flex-layout.js';
import '/resources/images/tg-icons.js';
import { tearDownEvent, allDefined } from '/resources/reflection/tg-polymer-utils.js';
import { RunActions } from '/resources/centre/tg-selection-criteria-behavior.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

const template = html`
    <style include='iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning'>
        :host {
            width: 100%;
            height: 100%;
            position: relative;
            @apply --layout-vertical;
        }
        .toolbar {
            margin: 8px;
        }
        .left-toolbar {
            @apply --layout-horizontal;
        }
        .centre-toolbar {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-weight: bold;
            font-size: 1.5rem;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
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
        .legend-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
        .legend-item {
            font-size: smaller;
            margin: 8px;
            @apply --layout-horizontal;
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
    <slot id="editActionSlot" name="calendar-action"></slot>
    <tg-flex-layout class="toolbar" when-desktop="[[_desktopToolbarLayout]]" when-mobile="[[_mobileToolbarLayout]]">
        <div class="left-toolbar">
            <paper-icon-button icon="event" tooltip-text="Today" on-tap="_today"></paper-icon-button>
            <paper-icon-button icon="chevron-left" tooltip-text="Previous" on-tap="_prev"></paper-icon-button>
            <paper-icon-button icon="chevron-right" tooltip-text="Next" on-tap="_next"></paper-icon-button>
            <paper-icon-button id="calendarTrigger" icon="tg-icons:legend" tooltip-text="Show the colour legend." on-tap="_showLegend"></paper-icon-button>
        </div>
        <span class="centre-toolbar" pos="center">[[calendarTitle]]</span>
        <tg-dropdown-switch view-index="0" views="[[viewTypes]]" button-width="40" change-current-view-on-select on-tg-centre-view-change="_changeView"></tg-dropdown-switch>
    </tg-flex-layout>
    <div id="calendarContainer"></div>
    <iron-dropdown id="dropdown" horizontal-align="left" restore-focus-on-close always-on-top>
        <div class="legend-content" slot="dropdown-content">
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
    </iron-dropdown>`;
    
export class TgFullcalendar extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            /**
             * Entities to be bound to calendar component as events with from / to dates.
             */
            entities: {
                type: Array,
                value: () => []
            },
            /**
             * The property to be used as title for the event and in its tooltip.
             */
            eventKeyProperty: String,
            /**
             * The property to be used as description for the event and in its tooltip.
             */
            eventDescProperty: String,
            /**
             * The property to be used as start date of the event.
             */
            eventFromProperty: String,
            /**
             * The property to be used as finish date of the event.
             */
            eventToProperty: String,
            /**
             * The property to be used as background colour of the event.
             */
            colorProperty: String,
            /**
             * The property to be used as title of the event's colour.
             */
            colorTitleProperty: String,
            /**
             * The property to be used as description of the event's colour.
             */
            colorDescProperty: String,
            currentView: {
                type: String,
                observer: '_currentViewChanged'
            },
            dataChangeReason: String,
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

        this.viewTypes = [
            { index: 0, title: "month", valueToSet: "dayGridMonth", icon: "tg-icons:month" },
            { index: 1, title: "week", valueToSet: "timeGridWeek", icon: "tg-icons:week" },
            { index: 2, title: "day", valueToSet: "timeGridDay", icon: "tg-icons:day" }
        ];

        this._desktopToolbarLayout = ['horizontal', 'justified', 'center', [], [], []];
        this._mobileToolbarLayout = [['justified', 'center', [], []], ['select:pos=center']];

        // configures calendar
        const config = {
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
                    eventInfo.el.setAttribute("tooltip-text", this.getTooltip(eventInfo.event.extendedProps.entity));
                }
            },
            eventTimeFormat: {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            },
            height: 'auto',
            firstDay: this._appConfig.firstDayOfWeek
        };
        if (moment.defaultZone) {
            config.plugins = [ momentTimezonePlugin ];
            config.timeZone = moment.defaultZone.name;
            config.now = () => now().toDate().toISOString(); // Return in ISO 8601 string format, as per default implementation
        }
        this._calendar = new FullCalendar(this.$.calendarContainer, config);
          this._calendar.render();
          this.currentView = 'dayGridMonth';
          //Initialising edit action
          this._editAction = this.$.editActionSlot.assignedNodes()[0];
          //initialise legend dropdown
          this.$.dropdown.positionTarget= this.$.calendarTrigger;
    }

    getTooltip(entity) {
        const tooltipValues = [];
        tooltipValues.push(this.getTooltipForProp(entity, this.eventKeyProperty, this.eventKeyProperty, this.eventDescProperty));
        tooltipValues.push(this.getTooltipForProp(entity, this.colorTitleProperty, this.colorTitleProperty, this.colorDescProperty));
        entity.get(this.eventFromProperty) && tooltipValues.push(this.getTooltipForDateProp(entity, this.eventFromProperty));
        entity.get(this.eventToProperty) && tooltipValues.push(this.getTooltipForDateProp(entity, this.eventToProperty));
        tooltipValues.push(this.getTooltipForAction(this._editAction));
        return this.makeTableForTooltip(tooltipValues);
    }

    getTooltipForProp(entity, titleProp, keyProp, descProp) {
        return {
            title: this._reflector.getEntityTypeProp(entity, titleProp).title(),
            value: "<b>" + entity.get(keyProp) + "</b>" + (descProp && entity.get(descProp) ? "<br><i>" + entity.get(descProp) + "</i>": "")
        }
    }

    getTooltipForDateProp(entity, dateProp) {
        return {
            title: this._reflector.getEntityTypeProp(entity, dateProp).title(),
            value: "<b>" + this._reflector.tg_toString(entity.get(dateProp), entity.type(), dateProp, { display: true }) + "</b>"
        };
    }

    getTooltipForAction(action) {
        return {
            title: "With action",
            value: `<b>${action.shortDesc}</b><br><i>${action.longDesc}</i>`
        };
    }

    makeTableForTooltip(tooltipValues) {
        return "<table>" +
            tooltipValues.map(entry => `<tr><td valign='top'>${entry.title}: </td><td valign='top'>${entry.value}</td></tr>`).join("\n") +
            "</table>"
    }

    _prev() {
        if (this._calendar) {
            this._calendar.prev();
            this.notifyResize();
        }
    }

    _next() {
        if (this._calendar) {
            this._calendar.next();
            this.notifyResize();
        }
    }

    _today() {
        if (this._calendar) {
            this._calendar.today();
        }
    }

    _changeView(e) {
        if (this._calendar) {
            this._calendar.changeView(this.viewTypes[e.detail].valueToSet);
        }
        tearDownEvent(e);
    }

    _showLegend() {
        this.$.dropdown.open();
    }

    /**
     * Updates calendar data; moves it to the date of the chronologically first event (if any); re-renders calendar.
     */
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
                    title: entity.get(eventKeyProperty) + (eventDescProperty && entity.get(eventDescProperty) ? " - "+ entity.get(eventDescProperty) : ""),
                    start: entity.get(eventFromProperty),
                    end: entity.get(eventToProperty),
                    backgroundColor: eventColor ? '#' + eventColor["hashlessUppercasedColourValue"]  : "#3788d8",
                });
                if (this.dataChangeReason !== RunActions.refresh && startTime && startTime < Infinity) {
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

    /**
     * Updates legend data; re-renders legend.
     */
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
                return byColour !== 0 ? byColour : a.key.localeCompare(b.key);
            });
    }

    _calcLegendItemStyle(item) {
        return "border-color: " + item.color + ";";
    }
}

customElements.define('tg-fullcalendar', TgFullcalendar);