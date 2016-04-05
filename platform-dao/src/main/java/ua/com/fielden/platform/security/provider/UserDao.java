package ua.com.fielden.platform.security.provider;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import ua.com.fielden.platform.security.exceptions.SecurityException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Implementation of the user controller, which should be used managing system user information.
 * 
 * @author TG Team
 * 
 */
@EntityType(User.class)
public class UserDao extends CommonEntityDao<User> implements IUser {

    private transient final Logger logger = Logger.getLogger(UserDao.class);
    
    
    private final SessionIdentifierGenerator crypto;
    private final IUserSession coUserSession;
    private final IUserRoleDao userRoleDao;
    private final IUserAndRoleAssociationDao userAssociationDao;
    private final IUniversalConstants constants;

    private final fetch<User> fetchModel = fetch(User.class).with("roles", fetch(UserAndRoleAssociation.class));

    @Inject
    public UserDao(
            final SessionIdentifierGenerator crypto,
            final IUserSession coUserSession,
            final IUserRoleDao userRoleDao, 
            final IUserAndRoleAssociationDao userAssociationDao,
            final IUniversalConstants constants,
            final IFilter filter) {
        super(filter);

        this.crypto = crypto;
        this.coUserSession = coUserSession;
        
        this.constants = constants;
        
        this.userRoleDao = userRoleDao;
        this.userAssociationDao = userAssociationDao;
    }
    
    @Override
    public List<? extends UserRole> findAllUserRoles() {
        return userRoleDao.findAll();
    }

    @Override
    public List<User> findAllUsers() {
        return findAllUsersWithRoles();
    }

    @Override
    public IPage<? extends User> firstPageOfUsersWithRoles(final int capacity) {
        final EntityResultQueryModel<User> model = select(User.class).where().prop(AbstractEntity.KEY).isNotNull().model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.KEY).asc().model();
        return firstPage(from(model).with(fetchModel).with(orderBy).model(), capacity);
    }

    @Override
    public void updateUsers(final Map<User, Set<UserRole>> userRoleMap) {
        for (final Map.Entry<User, Set<UserRole>> userRoleEntry : userRoleMap.entrySet()) {
            updateUser(userRoleEntry.getKey(), userRoleEntry.getValue());
        }
    }

    @SessionRequired
    private void updateUser(final User user, final Set<UserRole> checkedRoles) {
        // remove list at the first stage of the algorithm contains the associations of the given user.
        // At the last stage of the algorithm that list contains only associations those must be removed from the data base
        final Set<UserAndRoleAssociation> removeList = new HashSet<UserAndRoleAssociation>(user.getRoles());
        // contains the list of associations those must be saved
        final Set<UserAndRoleAssociation> saveList = new HashSet<UserAndRoleAssociation>();

        for (final UserRole role : checkedRoles) {
            final UserAndRoleAssociation roleAssociation = user.getEntityFactory().newByKey(UserAndRoleAssociation.class, user, role);
            if (!removeList.contains(roleAssociation)) {
                saveList.add(roleAssociation);
            } else {
                removeList.remove(roleAssociation);
            }
        }

        // first remove user/role associations
        userAssociationDao.removeAssociation(removeList);
        // then insert new user/role associations
        saveAssociation(saveList);
    }

    private void saveAssociation(final Set<UserAndRoleAssociation> associations) {
        for (final UserAndRoleAssociation association : associations) {
            userAssociationDao.save(association);
        }
    }

    @Override
    public User findUserByIdWithRoles(final Long id) {
        return findById(id, fetchModel);
    }

    @Override
    public User findUserByKeyWithRoles(final String key) {
        final EntityResultQueryModel<User> query = select(User.class).where().prop(AbstractEntity.KEY).eq().val(key).model();
        return getEntity(from(query).with(fetchModel).model());
    }

    @Override
    public List<User> findAllUsersWithRoles() {
        final EntityResultQueryModel<User> model = select(User.class).where().prop(AbstractEntity.KEY).isNotNull().model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.KEY).asc().model();
        return getAllEntities(from(model).with(fetchModel).with(orderBy).model());
    }

    @Override
    public User findUser(final String username) {
        return findByKeyAndFetch(fetch(User.class), username);
    }
    
    @Override
    public IFetchProvider<User> createFetchProvider() {
        return super.createFetchProvider()
                .with("key", "email") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("base", "basedOnUser.base"); //
    }

    @Override
    public final String hashPasswd(final String passwd, final String salt) throws Exception {
        return crypto.calculatePBKDF2WithHmacSHA1(passwd, salt);
    }
    
    @Override
    @SessionRequired
    public User resetPasswd(final User user, final String passwd) {
        try {
            // salt needs to be unique... at least amongst the users
            // it should be unique algorithmically, but let's be defensive and regenerate the salt if it conflicts with existing values
            user.setSalt(crypto.genSalt());
            final MetaProperty<Object> saltProp = user.getProperty("salt");
            final int maxTries = 1000;
            int tries = 1;
            while (!saltProp.isValid() && tries < maxTries) {
                user.setSalt(crypto.genSalt());
                tries++;
            }
            // we've tried hard, so if the salt is still not unique than bad luck...
            if (!saltProp.isValid()) {
                throw saltProp.getFirstFailure();
            }
            user.setPassword(hashPasswd(passwd, user.getSalt()));
            user.setResetUuid(null);
        } catch (Exception ex) {
            logger.warn("Could not reset password for user [%s].", ex);
            throw new SecurityException("Could not reset user password.", ex);
        }
        
        final User savedUser = save(user);
        
        // clear all the current user sessions
        coUserSession.clearAll(savedUser);
        return savedUser;
        
    }
    
    @Override
    public boolean isPasswordStrong(final String passwd) {
        final Zxcvbn zxcvbn = new Zxcvbn();
        final Strength strength = zxcvbn.measure(passwd);
        final double strengthTarget = 1 /* years */ * 365 /* days */ * 24 /* hours*/ * 60 /* minutes */ * 60 /* seconds */; 
        final double secondsToCrack = strength.getCrackTimeSeconds().getOfflineFastHashing1e10PerSecond();
        return strengthTarget <= secondsToCrack;
    }

    private static final String passwordResetUuidSeperator = "-";
    
    @Override
    public Optional<User> findUserByResetUuid(final String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            throw new SecurityException("User password resetting UUID cannot be empty.");
        }
        
        final String[] uuidParts = uuid.split(passwordResetUuidSeperator);
        if (uuidParts.length != 3) {
            throw new SecurityException(format("Invalid UUID [%s].", uuid));
        }
        final String userName = uuidParts[0];
        final EntityResultQueryModel<User> query = select(User.class)
                .where().prop("key").eq().val(userName)
                .and().prop("resetUuid").eq().val(uuid)
                .model();
        final User user = getEntity(from(query).with(fetchAll(User.class)).model());
        return Optional.ofNullable(user);
    }
    
    @Override
    public Optional<User> assignPasswordResetUuid(final String usernameOrEmail) {
        // let's try to find a user by username or email
        final EntityResultQueryModel<User> query = select(User.class)
                .where().prop("key").eq().val(usernameOrEmail)
                .or().prop("email").eq().val(usernameOrEmail).model();
        
        final User user = getEntity(from(query).with(fetchAll(User.class)).model());

        // if the user was found then a password reset request UUID needs to be generated
        // and associated wit the identified user
        if (user != null) {
            final String uuid = format("%s%s%s%s%s", user.getKey(), passwordResetUuidSeperator, crypto.nextSessionId(), passwordResetUuidSeperator, constants.now().plusHours(24).getMillis());
            return Optional.of(save(user.setResetUuid(uuid)));
        }

        return Optional.empty();
    }
    
    @Override
    public boolean isPasswordResetUuidValid(final String uuid) {
        final Optional<User> user = findUserByResetUuid(uuid);
        // if there is no user associated with UUID then it cannot be valid
        if (!user.isPresent()) {
            return false;
        } else {
            // if a corresponding user was found then UUID is valid if it is not expired
            final String[] uuidParts = uuid.split(passwordResetUuidSeperator);
            final long expirationTime = Long.valueOf(uuidParts[2]);
            final boolean expired = constants.now().getMillis() >= expirationTime;
            // dissociation UUID form user if has expired
            if (expired) {
                save(user.get().setResetUuid(null));
            }
            
            return !expired;
        }
    }
   
}