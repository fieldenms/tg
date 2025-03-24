import { globalPlugins } from '../@fullcalendar/core/index.js';
export { Calendar, createPlugin, globalLocales, globalPlugins } from '../@fullcalendar/core/index.js';
import index from '../@fullcalendar/interaction/index.js';
import index$1 from '../@fullcalendar/daygrid/index.js';
import index$2 from '../@fullcalendar/timegrid/index.js';
import index$3 from '../@fullcalendar/list/index.js';
import index$4 from '../@fullcalendar/multimonth/index.js';

globalPlugins.push(index, index$1, index$2, index$3, index$4);
