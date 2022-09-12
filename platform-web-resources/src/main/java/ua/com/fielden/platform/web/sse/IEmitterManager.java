package ua.com.fielden.platform.web.sse;

/**
 * A contract to manage clients for SSE.
 *
 * @author TG Team
 *
 */
public interface IEmitterManager {

    /**
     * Registers new client represented with {@link IEmitter} instance.
     *
     * @param uid
     * @param emitter
     * @return
     */
    IEmitterManager registerEmitter(String uid, IEmitter emitter);

    /**
     * Returns {@link IEmitter} instance by client UID.
     *
     * @param uid
     * @return
     */
    IEmitter getEmitter(String uid);

    /**
     * Closes the {@link IEmitter} that was registered with specified UID.
     *
     * @param uid
     * @return
     */
    boolean closeEmitter(String uid);
}
