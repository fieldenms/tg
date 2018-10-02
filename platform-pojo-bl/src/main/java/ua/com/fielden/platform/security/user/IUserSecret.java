package ua.com.fielden.platform.security.user;

import java.util.Optional;

import ua.com.fielden.platform.dao.IEntityDao;

public interface IUserSecret extends IEntityDao<UserSecret> {

    /**
     * A convenient method to find user's secret by their name.
     *
     * @param username
     * @return
     */
    Optional<UserSecret> findByUsername(final String username);

    /**
     * A method for hashing the user password before storing it into the database.
     * 
     * @param passwd
     * @param salt
     * @return
     */
    default String hashPasswd(final String passwd, final String salt) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * A convenient method for generation of new salt for cryptographic hashing.
     *
     * @return
     */
    String newSalt();

}
