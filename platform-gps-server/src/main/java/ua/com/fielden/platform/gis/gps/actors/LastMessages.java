package ua.com.fielden.platform.gis.gps.actors;

import java.util.List;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;

/**
 * A message type that contains response with a last {@link Message}.
 */
public class LastMessages<T extends AbstractAvlMessage> extends LastMessagesResponse {
    private final Long machineId;
    private final List<T> messages;

    public LastMessages(final Long machineId, final List<T> messages) {
	this.machineId = machineId;
	this.messages = messages;
    }

    public List<T> getMessages() {
	return messages;
    }

    public Long getMachineId() {
	return machineId;
    }
}
