package ua.com.fielden.platform.entity.proxy;

/**
 * A special exception that is thrown upon accessing properties of proxied entity instances that have a <code>STRICT</code> mode.
 *
 * @author TG Team
 *
 */
public class StrictProxyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final Object owner;
    private final Object entity;
    private final String msg;

    /**
     * Creates exception indication strict mode violation for proxies entity instances.
     * @param owner -- The owner of an entity proxy, which was violated.
     * @param entity -- The violated entity proxy.
     * @param msg -- A custom message providing more information on the nature of the problem
     */
    public StrictProxyException(final Object owner, final Object entity, final String msg) {
        this.owner = owner;
        this.entity = entity;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Object getOwner() {
        return owner;
    }

    public Object getEntity() {
        return entity;
    }
}
