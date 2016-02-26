package ua.com.fielden.platform.entity.proxy;

/**
 * A special exception that is thrown upon accessing properties of proxied entity instances that have a <code>STRICT</code> mode.
 *
 * @author TG Team
 *
 */
public class StrictProxyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates exception indication strict mode violation for proxies entity instances.
     * 
     * @param msg -- A custom message providing more information on the nature of the problem
     */
    public StrictProxyException(final String msg) {
        super(msg);
    }

}
