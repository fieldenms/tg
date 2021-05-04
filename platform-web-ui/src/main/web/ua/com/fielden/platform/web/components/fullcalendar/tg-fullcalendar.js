import {FullCalendar} from '/resources/components/fullcalendar/fullcalendar-component.js';
import '/resources/components/fullcalendar/fullcalendar-style.js';

import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style include='fullcalendar-style'>
        #calendarContainer {
            max-width: 1100px;
            margin: 40px auto;
        }
    </style>
    <div id="calendarContainer">    
    </div>`;
    
class TgFullcalendar extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            entities: {
                type: Array,
                value: () => []
            },
            eventTitleProperty: String,
            eventFromProperty: String,
            eventToProperty: String,

            _calendar: {
                type: Object,
                value: null
            }
        };
    }

    static get observers() {
        return [
          '_updateEventSource(entities, eventTitleProperty, eventFromProperty, eventToProperty, _calendar)'
        ];
    }

    ready () {
        super.ready();
        this._calendar = new FullCalendar.Calendar(this.$.calendarContainer, {
            initialView: 'dayGridMonth',
            headerToolbar: {
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek,timeGridDay'
            }
          });
        calendar.render();
    }

    _updateEventSource(entities, eventTitleProperty, eventFromProperty, eventToProperty, _calendar) {
        if (allDefined(entities, entityTitleProperty, eventFromProperty, eventToProperty)) {
            const startTime = Infinity;
            entities.forEach(entity => {
                if (startTime > entity.get(eventFromProperty)) {
                    startTime = entity.get(eventFromProperty);
                }
                _calendar.addEvent({
                    title: entity.get(eventTitleProperty),
                    start: entity.get(eventFromProperty),
                    end: entity.get(eventToProperty),
                });
                if (startTime && startTime < Infinity) {
                    _calendar.gotoDate(startTime);
                }
            });
        } else if (_calendar){
            _calendar.gotoDate(new Date());
            _calendar.getEvents().forEach(event => event.remove());
        }
    }
}

customElements.define('tg-fullcalendar', TgFullcalendar);