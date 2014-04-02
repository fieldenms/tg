package ua.com.fielden.platform.dir;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

/**
 * A class that contains a common JNDI operations extensions.
 * 
 * @author TG Team
 * 
 */
public class DirectoryWorker {
    private final static Logger logger = Logger.getLogger(DirectoryWorker.class);

    /**
     * Connects to appropriate LDAP server database with provided master credentials and retrieves working directory context, that later could be used for search/modify/etc.
     * operations.
     * 
     * @param server
     *            - a server name or its IP address
     * @param port
     *            - a port for LDAP server, typically 389.
     * @param base
     *            - a base for which a dir context will be created (e.g. ou=TG Users,ou=Users,dc=example,dc=com)
     * @param username
     *            - an username of some user with an appropriate privileges (e.g. "JHOU@example.com" or "cn=JHOU,ou=TG Users,ou=Users,dc=example,dc=com")
     * @param password
     *            - a password for some user with an appropriate privileges
     * @return
     * @throws NamingException
     */
    public static synchronized DirContext ldapConnect(final String server, final String port, final String base, final String username, final String password) throws Exception {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + server + ":" + port + "/" + base);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            final DirContext ctx = new InitialDirContext(env);
            return ctx;
        } catch (final Exception e) {
            final String message = "Could not connect to LDAP server [" + server + "] via port [" + port + "] for base [" + base + "] with master user [" + username + "]. "
                    + e.getMessage();
            logger.error(message);
            throw new Exception(message);
        }
    }

}
