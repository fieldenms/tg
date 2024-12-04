import fullcalendarStyleStrings from '/resources/fullcalendar/main.css.js';
import { createStyleModule } from '/resources/reflection/tg-polymer-utils.js';
createStyleModule('fullcalendar-styles', fullcalendarStyleStrings);

import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
export const fullcalendarStyles = html`<style include='fullcalendar-styles'></style>`; // can't use 'const ... = 'fullcalendar-styles'', because of html tag function stringent security

import '/resources/fullcalendar/main.js';
export const FullCalendar = window.FullCalendar;

import '/resources/components/moment-lib.js';

import '/resources/fullcalendar/moment-timezone/main.global.js';
export const momentTimezonePlugin = FullCalendar.momentTimezonePlugin;