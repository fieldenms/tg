package ua.com.fielden.platform.security.provider;

import static java.lang.String.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.annotations.PasswordHashingKey;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * Implementation of the user controller, which should be used managing system user information.
 * 
 * @author TG Team
 * 
 */
@EntityType(User.class)
public class UserDao extends CommonEntityDao<User> implements IUser {

    private transient final Logger logger = Logger.getLogger(UserDao.class);
    
    
    private final String hashingKey;
    private final SessionIdentifierGenerator crypto;
    private final IUserSession coUserSession;
    private final IUserRoleDao userRoleDao;
    private final IUserAndRoleAssociationDao userAssociationDao;

    private final fetch<User> fetchModel = fetch(User.class).with("roles", fetch(UserAndRoleAssociation.class));

    @Inject
    public UserDao(
            final @PasswordHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto,
            final IUserSession coUserSession,
            final IUserRoleDao userRoleDao, 
            final IUserAndRoleAssociationDao userAssociationDao, 
            final IFilter filter) {
        super(filter);

        this.hashingKey = hashingKey;
        this.crypto = crypto;
        this.coUserSession = coUserSession;
        
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
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("base", "basedOnUser.base", "roles"); //
    }

    @Override
    public final String hashPasswd(final String passwd) throws Exception {
        return crypto.calculateRFC2104HMAC(passwd, hashingKey);
    }
    
    @Override
    @SessionRequired
    public User resetPasswd(final User user) {
        try {
            user.setPassword(hashPasswd(user.getKey()));
        } catch (Exception ex) {
            logger.warn("Could not reset password for user [%s].", ex);
            throw new SecurityException("Could not reset user password.", ex);
        }
        
        final User savedUser = save(user);
        
        // clear all the current user sessions
        coUserSession.clearAll(savedUser);
        return savedUser;
        
    }
    
    
    public static void main(String[] args) throws Exception {
        final Zxcvbn zxcvbn = new Zxcvbn();
        final String passwd = "Ambulance services save many lives.";
        final Strength strength = zxcvbn.measure(passwd);
        
        final double strengthTarget = 1 /* years */ * 365 /* days */ * 24 /* hours*/ * 60 /* minutes */ * 60 /* seconds */; 
        
        final double secondsToCrack = strength.getCrackTimeSeconds().getOfflineFastHashing1e10PerSecond();
        
        final SessionIdentifierGenerator crypto = new SessionIdentifierGenerator();
        final String hashingKey = "This is a hasing key, which is used to hash user passwords for Trident Fleet server. We also need to make sure this key is very long and strong.";
        final String hash = crypto.calculateRFC2104HMAC(passwd, hashingKey);
        
        System.out.println(format("Acceptable: %s, Score: %s, Estimated crack time: %s, [%s second(s)],\nHash: %s (%s characters)",
                strengthTarget < secondsToCrack, 
                strength.getScore(), 
                strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond(),
                secondsToCrack,
                hash,
                hash.length()));

    }

    @Override
    public boolean isPasswordStrong(final String passwd) {
        if ("Cows are animals that produce milk.".equalsIgnoreCase(passwd)) {
            return false;
        }
        
        final Zxcvbn zxcvbn = new Zxcvbn();
        final Strength strength = zxcvbn.measure(passwd);
        final double strengthTarget = 1 /* years */ * 365 /* days */ * 24 /* hours*/ * 60 /* minutes */ * 60 /* seconds */; 
        final double secondsToCrack = strength.getCrackTimeSeconds().getOfflineFastHashing1e10PerSecond();
        return strengthTarget <= secondsToCrack;
    }
}