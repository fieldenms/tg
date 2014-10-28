package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link TgPersonDao}
 *
 * @author TG Team
 *
 */
@EntityType(TgPerson.class)
public class TgPersonDao extends CommonEntityDao<TgPerson> implements ITgPerson {

    private final IUserController userDao;
    private final EntityFactory entityFactory;
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    protected TgPersonDao(final IFilter filter, final EntityFactory entityFactory, final IUserController userDao) {
        super(filter);
        this.entityFactory = entityFactory;
        this.userDao = userDao;
    }

    @Override
    public TgPerson makeUser(final TgPerson person, final String privateKey) {
        if (person.isUser()) {
            throw new IllegalArgumentException("Person " + person.getKey() + " is already an application user.");
        }

        final TgPerson latestPerson = findById(person.getId(), fetchAll(TgPerson.class));
        final User su = userDao.findByKey("SU");
        latestPerson.setBasedOnUser(su);
        latestPerson.setUsername(person.getKey());
        resetPasswd(latestPerson, privateKey);
        return save(latestPerson);
    }

    @Override
    public TgPerson unmakeUser(final TgPerson person, final String privateKey) {
        if (!person.isUser()) {
            throw new IllegalArgumentException("Person " + person.getKey() + " is not a application user.");
        }
        if (User.system_users.isOneOf(person.getUsername())) {
            throw new IllegalArgumentException("Person " + person.getKey() + " is an application built-in user account and cannot changed.");
        }

        final TgPerson latestPerson = findById(person.getId(), fetchAll(TgPerson.class));
        latestPerson.setUsername(null);
        latestPerson.setPassword(null);
        latestPerson.setPublicKey(null);
        latestPerson.setBasedOnUser(null);
        return save(latestPerson);
    }

    @Override
    public TgPerson resetPassword(final TgPerson person, final String privateKey) {
        final TgPerson latestPerson = findById(person.getId(), fetchAll(TgPerson.class));
        try {
            resetPasswd(latestPerson, privateKey);
            return save(latestPerson);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not reset user password.", e);
        }
    }

    private void resetPasswd(final TgPerson person, final String privateKey) {
        if (!person.isUser()) {
            throw new IllegalArgumentException("Cannot reset password -- person " + person.getKey() + " is not an application user.");
        }
        try {
            person.setPassword(new Cypher().encrypt(person.getUsername(), privateKey));
        } catch (final Exception e) {
            throw new IllegalStateException("Could not reset user password.", e);
        }
    }

    @Override
    public TgPerson populateNew(final String givenName, final String surName, final String fullName, final String userName, final String privateKey) {
        // generate "personCode" and "description" for creating a new person:
        final TgPerson newPerson = User.system_users.SU.name().equals(userName) ? entityFactory.newEntity(TgPerson.class, userName, "Super User")
                : entityFactory.newEntity(TgPerson.class, generateUniquePersonCode(givenName, surName, userName), generatePersonDesc(givenName, surName, fullName));
        newPerson.setUsername(userName);
        // "based on user" is required. SU is the base user and thus has no "based on user".
        newPerson.setBasedOnUser(User.system_users.SU.name().equals(userName) ? null : userDao.findByKey("SU"));
        newPerson.setBase(User.system_users.SU.name().equals(userName));

        final TgPerson p = save(newPerson);
        try {
            final User user = userDao.findByKey(userName);
            final Cypher cypher = new Cypher();
            user.setPassword(cypher.encrypt(user.getKey(), privateKey));
            userDao.save(user);
        } catch (final Exception e) {
            throw new IllegalStateException("A password reset for a new user [" + userName + "] failed. " + e.getMessage());
        }

        logger.info("A new user [" + p.getKey() + " -- " + p.getDesc() + "] with username [" + userName + "] has been created. Its password has been resetted.");
        return p;
    }

    /**
     * Generates a person code that is required to be unique in the system and, possibly, short (4 letters preferred).
     *
     * @param givenName
     * @param surName
     * @param userName
     * @return
     */
    private String generateUniquePersonCode(final String givenName, final String surName, final String userName) {
        final String gn = letters(givenName, 1);
        final String four = gn + letters(surName, 3), three = gn + letters(surName, 2), two = gn + letters(surName, 1), usernameFour = letters(userName, 4);

        if (!entityWithKeyExists(four)) {
            return four;
            // error("A person code [" + code + "] is not unique. Currently just fail.");
        } else if (!entityWithKeyExists(three)) {
            return three;
        } else if (!entityWithKeyExists(two)) {
            return two;
        } else if (!entityWithKeyExists(usernameFour)) {
            return usernameFour;
        } else {
            // as in T32 :
            for (char c = 'A'; c <= 'Z'; c++) {
                final String code = three + c;
                if (!entityWithKeyExists(code)) {
                    return code;
                }
            }
            for (char c = '0'; c <= '9'; c++) {
                final String code = three + c;
                if (!entityWithKeyExists(code)) {
                    return code;
                }
            }
            final String ex = "A new user/person creation failed for [" + givenName + " " + surName + "] with automatically generated username [" + userName
                    + "]. All the following usernames are regretably already assigned to other persons: [" + four + "," + three + "," + two + "," + usernameFour
                    + "] and all codes [" + three + " + letter/digit]. Is it possible? Please contact your administrator.";
            logger.error(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns first n letters (or digits) in upper case form.
     *
     * @param i
     * @param givenName
     * @return
     */
    private static String letters(final String str, final int n) {
        final String cleanUpperCase = str.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return cleanUpperCase.substring(0, n);
    }

    /**
     * Generates person description.
     *
     * @param givenName
     * @param surName
     * @param fullName
     * @return
     */
    public static String generatePersonDesc(final String givenName, final String surName, final String fullName) {
        // please note that a fullName, as was specified in LDAP server, could be used as description too, but for now (as in T32) : surName + " " + givenName :
        return surName + " " + givenName;
    }

    @Override
    public TgPerson currentPerson() {
        final EntityResultQueryModel<TgPerson> qExecModel = select(TgPerson.class).where().prop("username").eq().val(getUsername()).model();
        return getEntity(from(qExecModel).model());
    }

}
