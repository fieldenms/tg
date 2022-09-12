package ua.com.fielden.platform.web.sse;

/**
 * Contract to manage list of registered event sources that is used to sent events to all clients registered via {@link IEmitter}
 *
 * @author TG Team
 *
 */
public interface IEventSourceManager {

    /**
     * Registers new event source on server to send events to all registered clients.
     *
     * @param eventSource
     * @return
     */
    IEventSourceManager registerEventSource(final IEventSource eventSource);
}
