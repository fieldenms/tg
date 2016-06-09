package ua.com.fielden.platform.sample.domain;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO for {@link TgPersonDao}
 *
 * @author TG Team
 *
 */
@EntityType(TgPerson.class)
public class TgPersonDao extends CommonEntityDao<TgPerson> implements ITgPerson {

    private final IUser coUser;
    private final EntityFactory entityFactory;
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    protected TgPersonDao(final IFilter filter, final EntityFactory entityFactory, final IUser coUser) {
        super(filter);
        this.entityFactory = entityFactory;
        this.coUser = coUser;
    }

    @Override
    @SessionRequired
    public TgPerson makeUser(final TgPerson person) {
        if (person.isUser()) {
            throw Result.failure(format("Person [%s] is already an application user.", person.getKey()));
        }

        final User user = entityFactory.newByKey(User.class, person.getKey());
        user.setDesc(format("User for person [%s].", person.getDesc()));
        final User su = coUser.findByKeyAndFetch(fetchAll(User.class), User.system_users.SU.name());
        user.setBasedOnUser(su);
        final User savedUser = coUser.resetPasswd(user, user.getKey());
        
        final TgPerson latestPerson = findById(person.getId(), fetchAll(TgPerson.class));
        latestPerson.setUser(savedUser);
        return save(latestPerson);
    }

    @Override
    @SessionRequired
    public TgPerson unmakeUser(final TgPerson person) {
        if (!person.isUser()) {
            throw new SecurityException(format("Person [%s] is not an application user.", person.getKey()));
        }
        
        if (User.system_users.isOneOf(person.getUser().getKey())) {
            throw new SecurityException(format("Person [%s] is an application built-in user account and cannot be changed.", person.getKey()));
        }

        final User user = person.getUser();
        person.setUser(null);
        final TgPerson savedPerson = save(person);
        coUser.delete(user);
        
        return savedPerson;
    }

    @Override
    @SessionRequired
    public TgPerson resetPassword(final TgPerson person) {
        // reset the user password
        coUser.resetPasswd(person.getUser(), person.getUser().getKey());
        // return person now pointing to an updated user
        return findById(person.getId(), fetchAll(TgPerson.class));
    }

    @Override
    @SessionRequired
    public TgPerson populateNew(final String givenName, final String surName, final String fullName, final String userName) {
        // generate "personCode" and "description" for creating a new person:
        final TgPerson newPerson = User.system_users.SU.name().equals(userName) ? entityFactory.newEntity(TgPerson.class, userName, "Super User")
                : entityFactory.newEntity(TgPerson.class, generateUniquePersonCode(givenName, surName, userName), generatePersonDesc(givenName, surName, fullName));
        // according to LDAP integration business logic, a new user should be "sector user".
        final TgPerson p = save(newPerson);
        
        if (User.system_users.SU.name().equals(userName)) {
            p.setUser(coUser.findByKeyAndFetch(fetchAll(User.class), User.system_users.SU.name()));
            return save(p);
        } else {
            return makeUser(p);
        }
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
