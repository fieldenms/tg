import { createPlugin } from '../core/index.js';
import moment from '../../../_virtual/index.js';
import { bw as NamedTimeZoneImpl } from '../core/internal-common.js';

class MomentNamedTimeZone extends NamedTimeZoneImpl {
    offsetForArray(a) {
        return moment.tz(a, this.timeZoneName).utcOffset();
    }
    timestampToArray(ms) {
        return moment.tz(ms, this.timeZoneName).toArray();
    }
}

var index = createPlugin({
    name: '@fullcalendar/moment-timezone',
    namedTimeZonedImpl: MomentNamedTimeZone,
});

export { index as default };
