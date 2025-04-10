import { getDefaultExportFromCjs } from './_commonjsHelpers.js';
import { __require as requireMomentTimezone } from '../polymer/moment-timezone/index.js';

var momentTimezoneExports = requireMomentTimezone();
var moment = /*@__PURE__*/getDefaultExportFromCjs(momentTimezoneExports);

export { moment as default };
