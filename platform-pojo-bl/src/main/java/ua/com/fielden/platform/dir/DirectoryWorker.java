package ua.com.fielden.platform.dir;

import static java.lang.String.format;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dir.exceptions.LdapException;

/**
 * A class that contains a common JNDI operations extensions.
 * 
 * @author TG Team
 * 
 */
public class DirectoryWorker {
    private static final Logger logger = Logger.getLogger(DirectoryWorker.class);

    private DirectoryWorker() {}

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
    public static DirContext ldapConnect(final String server, final String port, final String base, final String username, final String password) {
        final Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + server + ":" + port + "/" + base);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            return new InitialDirContext(env);
        } catch (final Exception ex) {
            final String msg = format("Could not connect to LDAP server [%s] via port [%s] for base [%s] with master user [%s].", server, port, base, username);
            logger.error(msg, ex);
            throw new LdapException(msg, ex);
        }
    }

}
