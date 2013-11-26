package ua.com.fielden.platform.gis.gps.actors;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;

/**
 * A message type that contains response with a last {@link Message}.
 */
public class LastMessage<T extends AbstractAvlMessage> extends LastMessageResponse {
    private final Long machineId;
    private final T message;

    public LastMessage(final Long machineId, final T message) {
	this.machineId = machineId;
	this.message = message;
    }

    public T getMessage() {
	return message;
    }

    public Long getMachineId() {
	return machineId;
    }
}
