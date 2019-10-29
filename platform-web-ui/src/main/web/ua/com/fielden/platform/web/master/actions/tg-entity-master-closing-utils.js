/**
 * Creates successful 'then' handler for master 'entity action' promise.
 */
export const createEntityActionThenCallback = function (eventChannel, role, postalLib, _afterExecution, closeAfterExecution) {
    return function (ironRequest) {
        if (eventChannel && role) {
            console.log('AJAX PROMISE THEN', ironRequest.successful);
            if (ironRequest.xhr.status === 200 && ironRequest.successful === true) {
                // the data to be published consists of an entityId
                // and an indication whether an associated view containing this action can be closed
                // how this data is actually used depends purely on the subscriber
                const dataValue = {
                    id: ironRequest.entityId,
                    canClose: closeAfterExecution
                };

                // action with role 'save' should only encourage closing in cases
                // where the returned after save entity is persistent and isn't new
                // this case pertains to entit editing
                // such approach enables continuous creation of entities as per issue #285
                // (https://github.com/fieldenms/tg/issues/285)
                if (role === 'save' &&
                    ironRequest.entityPersistent === true &&
                    !ironRequest.entityId &&
                    ironRequest.entityContinuation === false) {
                    dataValue.canClose = false;
                }

                // action with role 'refresh' should only encourage closing in cases
                // where the returned after refresh entity is new (aka not persisted) as per issue #916
                // (https://github.com/fieldenms/tg/issues/916)
                if (role === 'refresh' && ironRequest.entityId) {
                    dataValue.canClose = false;
                }

                postalLib.publish({
                    channel: eventChannel,
                    topic: role + '.post.success',
                    data: dataValue
                });
            } else {
                postalLib.publish({
                    channel: eventChannel,
                    topic: role + '.post.error',
                    data: {}
                });

                if (_afterExecution) {
                    // call the generic button execution logic, which ensures stopping of the spinner and handling of
                    // its enabled state
                    _afterExecution();
                }
            }
        }
    };
};