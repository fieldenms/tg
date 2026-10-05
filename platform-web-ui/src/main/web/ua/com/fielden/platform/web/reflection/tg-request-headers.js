import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';
import { DEVICE_PROFILE_HEADER, deviceProfile } from '/resources/reflection/tg-polymer-utils.js';

/**
 * Custom headers added to every client-side request that reaches a server resource.
 *
 * 'Time-Zone' carries the real time-zone of the client application, to be assigned to the thread-local
 * 'IDates.timeZone' so that 'Now' is computed properly.
 *
 * 'Device-Profile' carries the device profile resolved on the client, which is the only source of truth for it.
 * On the server it selects the main menu and namespaces Entity Centre configurations
 * -- see AbstractWebResource.calculateDeviceProfile and CentreUpdater.deviceSpecific.
 *
 * Both are produced here, together, so that a request can never carry one without the other.
 */
export function _requestHeaders () {
    return {
        ..._timeZoneHeader(),
        [DEVICE_PROFILE_HEADER]: deviceProfile()
    };
};
