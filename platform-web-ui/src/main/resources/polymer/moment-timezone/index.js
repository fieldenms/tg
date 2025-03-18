import { __module as momentTimezone } from '../../_virtual/index2.js';
import { __require as requireMomentTimezone$1 } from './moment-timezone.js';
import require$$1 from './data/packed/latest.json.js';

var hasRequiredMomentTimezone;

function requireMomentTimezone () {
	if (hasRequiredMomentTimezone) return momentTimezone.exports;
	hasRequiredMomentTimezone = 1;
	var moment = momentTimezone.exports = requireMomentTimezone$1();
	moment.tz.load(require$$1);
	return momentTimezone.exports;
}

export { requireMomentTimezone as __require };
