package ua.com.fielden.platform.security.user;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.menu.WebMenuItemInvisibility;
import ua.com.fielden.platform.menu.WebMenuItemInvisibilityCo;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.user.User.*;
import static ua.com.fielden.platform.security.user.UserSecret.SECRET_RESET_UUID_SEPERATOR;
import static ua.com.fielden.platform.security.user.UserSecretCo.newUserPasswordRestExpirationTime;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;

/// DAO implementation of [IUser].
///
@EntityType(User.class)
public class UserDao extends CommonEntityDao<User> implements IUser {

    private static final Logger logger = getLogger(UserDao.class);
    public static final String
            ERR_USER_ID_WAS_RETURNED_INSTEAD_OF_AN_INSTANCE = "Unexpected error: user ID [%s] was returned instead of an instance after saving user [%s].",
            ERR_INITIATING_PASSWORD_RESET = "Could not initiate password reset.",
            ERR_DELETING_USERS_WITH_ROLES = "Users assigned to roles can’t be deleted. Deactivate such users instead.";

    private final INewUserNotifier newUserNotifier;
    private final SessionIdentifierGenerator crypto;
    private final boolean ssoMode;

    private final fetch<User> fetchModel = fetch(User.class).with(ACTIVE_ROLES, fetch(SynUserAndRoleAssociationActive.class))
                                                            .with(INACTIVE_ROLES, fetch(SynUserAndRoleAssociationInactive.class));

    @Inject
    public UserDao(
            final INewUserNotifier newUserNotifier,
            final SessionIdentifierGenerator crypto,
            final IApplicationSettings appSettings)
    {
        this.newUserNotifier = newUserNotifier;
        this.crypto = crypto;
        this.ssoMode = appSettings.authMode() == AuthMode.SSO;
    }

    @Override
    public User new_() {
        final User newUser = super.new_();
        newUser.getProperty(User.SSO_ONLY).setValue(ssoMode, /* enforce */ true); // set ssoOnly to reflect the current authentication mode; set forcibly to ensure execution of UserSsoOnlyDefiner, which processes the meta-property
        newUser.getProperty(User.BASED_ON_USER).setRequired(true);
        return newUser;
    }

    /**
     * Saves a user instance. Special care is taken for the case where only property {@code refCount} is changed.
     * This is why this method is not annotated with {@code @Authorise(User_CanSave_Token.class)}.
     * Authorisation happens for {@link #save(User, Optional)}, which is invoked for all other cases.
     */
    @Override
    @SessionRequired
    public User save(final User user) {
        // anybody should be able to save updated reference count
        if (user.getDirtyProperties().size() == 1 && user.getProperty(User.REF_COUNT).isDirty()) {
            // use super save with refetching based on the reconstructed fetch model,
            // which should be slim comparing to IUser.FETCH_PROVIDER
            return super.save(user);
        } else {
            return save(user, of(FETCH_PROVIDER.fetchModel())).orElseThrow(id -> new EntityCompanionException(ERR_USER_ID_WAS_RETURNED_INSTEAD_OF_AN_INSTANCE.formatted(id, user)));
        }

    }

    @Override
    @Authorise(User_CanSave_Token.class)
    protected Either<Long, User> save(final User user, final Optional<fetch<User>> maybeFetch) {
        if (User.system_users.VIRTUAL_USER.matches(user)) {
            throw new SecurityException("VIRTUAL_USER cannot be persisted.");
        }
        user.isValid().ifFailure(Result::throwRuntime);
        // remove all authenticated sessions in case the user is being deactivated
        if (user.isPersisted() && !user.isActive() && user.getProperty(ACTIVE).isDirty()) {
            final IUserSession coUserSession = co(UserSession.class);
            coUserSession.clearAll(user);
            final UserSecretCo coUserSecrete = co(UserSecret.class);
            coUserSecrete.batchDelete(listOf(user.getId()));
        }

        // User becomes a based-on user or has its base user changed -- need to handle menu invisibility.
        // If a based-on user is new or became active or changed its base user then we need remove any previous invisible menu items and create new ones
        // for menu items, which are invisible for all based-on users for the current base user (and whish is now also a base user for the user instance being saved).
        final List<String> menuItemsToSave = new ArrayList<>();
        // the use is new or it either has base user or active flag changed
        final boolean newOrHasBaseUserOrActivePropsChanged = !user.isPersisted() ||
                                                             (user.isPersisted() && (user.getProperty("basedOnUser").isDirty() || user.getProperty(ACTIVE).isDirty()));
        if (!user.isBase() && user.isActive() && newOrHasBaseUserOrActivePropsChanged) {
            final WebMenuItemInvisibilityCo coMenuItemInvisibility = co(WebMenuItemInvisibility.class);
            coMenuItemInvisibility.batchDelete(select(WebMenuItemInvisibility.class).where().prop("owner").eq().val(user).model());
            menuItemsToSave.addAll(invisibleMenuItems(user));
        }

        // if a new active user is being created then need to send an activation email, but only if user is not restricted to SSO only for an application in the SSO mode
        // this is possible only if an email address is associated with the user, which is required for active users
        // there could also be a situation where an inactive existing user, which did not have their password set in the first place, is being activated... this also warrants an activation email
        final Either<Long, User> savedUser;
        if ((!user.isPersisted() && user.isActive() && notRestrictedToSsoOnly(user)) ||
            ( user.isPersisted() && user.isActive() && notRestrictedToSsoOnly(user) && user.getProperty(ACTIVE).isDirty() && passwordNotAssigned(user))) {
            savedUser = super.save(user, maybeFetch);
            final Function<Long, EntityCompanionException> error = (Long id) -> new EntityCompanionException(ERR_USER_ID_WAS_RETURNED_INSTEAD_OF_AN_INSTANCE.formatted(id, user));
            newUserNotifier.notify(assignPasswordResetUuid(savedUser.orElseThrow(error).getKey(), newUserPasswordRestExpirationTime()).orElseThrow(() -> new SecurityException(ERR_INITIATING_PASSWORD_RESET)));
        } else {
            savedUser = super.save(user, maybeFetch);
        }

        // save menu item invisibility for a user, this may require fetching the user in case savedUser is only an ID (i.e. left).
        final User menuOwner = savedUser.isLeft() ? co(User.class).findById(savedUser.asLeft().value(), WebMenuItemInvisibilityCo.FETCH_PROVIDER.<User>fetchFor("owner").fetchModel()) : savedUser.asRight().value();
        saveMenuItemInvisibility(menuItemsToSave, menuOwner);

        return savedUser;
    }

    /**
     * A helper predicate, which return {@code true} for users who are not restricted to SSO only in the SSO authentication mode.
     *
     * @param user
     * @return
     */
    private boolean notRestrictedToSsoOnly(final User user) {
        return !ssoMode || !user.isSsoOnly();
    }

    /**
     * Saves new {@link WebMenuItemInvisibility} for menu item URIs specified in menuItems, and specified non base user.
     *
     * TODO once issue https://github.com/fieldenms/tg/issues/1032 is merged, this saving should be optimised
     *
     * @param menuItems
     * @param menuOwner
     */
    private void saveMenuItemInvisibility(final List<String> menuItems, final User menuOwner) {
        final WebMenuItemInvisibilityCo co$MenuItemInvisibility = co$(WebMenuItemInvisibility.class);
        menuItems.forEach(menuItem -> {
            co$MenuItemInvisibility.save(co$MenuItemInvisibility.new_().setOwner(menuOwner).setMenuItemUri(menuItem));
        });
    }

    /**
     * Returns menu item URIs to save as invisible menu items for specified user. That is needed when user changes it's base user or it is new user or
     * the user that becomes active. The given user should be non base user
     *
     * @param user
     * @return
     */
    private List<String> invisibleMenuItems(final User user) {
        //First find all active non base users for specified non base user.
        final Set<User> availableUsers = findBasedOnUsers(user.getBasedOnUser(), fetchKeyAndDescOnly(User.class));
        //Then find all invisible menu items for non base users based on the same user as base user of given user.
        final Map<String, Set<User>> invisibleMenuItems = getInvisibleMenuItemsForBaseUser(user.getBasedOnUser());
        //Find all menu items those are invisible for all non base user of specified user's base user.
        return invisibleMenuItems.entrySet().stream()
            .filter(entry -> entry.getValue().containsAll(availableUsers))
            .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public Set<User> findBasedOnUsers(final User baseUser, final fetch<User> userFetch) {
        return new LinkedHashSet<>(co(User.class).getAllEntities(from(
                select(User.class).where()
                .prop(ACTIVE).eq().val(true).and()
                .prop(BASE).eq().val(false).and()
                .prop(BASED_ON_USER).eq().val(baseUser).model())
                .with(userFetch).with(orderBy().prop(KEY).asc().model()).model()));
    }

    /**
     * Returns all invisible menu items for active non-base users based on given base user.
     *
     * @param baseUser
     * @return
     */
    private Map<String, Set<User>> getInvisibleMenuItemsForBaseUser(final User baseUser) {
        final WebMenuItemInvisibilityCo coMenuItemInvisibility = co(WebMenuItemInvisibility.class);
        final EntityResultQueryModel<WebMenuItemInvisibility> query = select(WebMenuItemInvisibility.class).where()
                .prop("owner.basedOnUser").eq().val(baseUser).and()
                .prop("owner.active").eq().val(true).model();
        try (Stream<WebMenuItemInvisibility> stream = coMenuItemInvisibility.stream(from(query).with(fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model())) {
            return stream.collect(Collectors.groupingBy(WebMenuItemInvisibility::getMenuItemUri, Collectors.mapping(WebMenuItemInvisibility::getOwner, toSet())));
        }
    }

    private Boolean passwordNotAssigned(final User user) {
        return co(UserSecret.class).findByIdOptional(user.getId()).map(us -> isEmpty(us.getPassword())).orElse(true);
    }

    @Override
    public List<? extends UserRole> findAllUserRoles() {
        return this.<UserRoleCo, UserRole>co$(UserRole.class).findAll();
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

    private void saveAssociation(final Set<UserAndRoleAssociation> associations) {
        for (final UserAndRoleAssociation association : associations) {
            co$(UserAndRoleAssociation.class).save(association);
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
        // it is critical that the fetch is as tight here as possible in order not to leak any sensitive info to the client
        final fetch<User> fetch = fetchOnly(User.class).with(KEY).with(ACTIVE).with("base").with("basedOnUser", fetchIdOnly(User.class));
        return findByKeyAndFetch(fetch, username);
    }

    @Override
    public IFetchProvider<User> createFetchProvider() {
        return IUser.FETCH_PROVIDER;
    }

    /**
     * A convenient method that either returns an instance of {@link UserSecret} that is already associated with {@code user},
     * or a new instance of {@link UserSecret}.
     *
     * @param user
     * @param coUserSecret
     * @return
     */
    private UserSecret findOrCreateNewSecret(final User user, final UserSecretCo coUserSecret) {
        if (!user.isPersisted()) {
            throw new SecurityException("User must be persisted.");
        }
        return coUserSecret.findByIdOptional(user.getId(), coUserSecret.getFetchProvider().fetchModel()).orElseGet(() -> coUserSecret.new_().setKey(user));
    }

    @Override
    @SessionRequired(allowNestedScope = false)
    @Authorise(AlwaysAccessibleToken.class)
    public void lockoutUser(final String username) {
        // deactivate user if found
        final User user = co$(User.class).findByKeyAndFetch(getFetchProvider().fetchModel(), username);
        if (user != null) {
            save(user.setActive(false));
        }

        // attempt to delete user secret regardless of whether user exists or not
        // this is to reduce the difference in the computation time that is required for processing existing and non-existing accounts
        final UserSecretCo coUserSecret = co(UserSecret.class);
        coUserSecret.batchDelete(select(UserSecret.class).where().prop("key.key").eq().val(username).model());
    }

    @Override
    @SessionRequired
    @Authorise(AlwaysAccessibleToken.class)
    public UserSecret resetPasswd(final User forUser, final String passwd) {
        try {
            final User user = forUser.isInstrumented() && forUser.isDirty() ? save(forUser) : forUser;

            final UserSecretCo co$UserSecret = co$(UserSecret.class);
            final UserSecret secret = findOrCreateNewSecret(user, co$UserSecret);
            // salt needs to be unique... at least amongst the users
            // it should be unique algorithmically, but let's be defensive and regenerate the salt if it conflicts with existing values
            secret.setSalt(crypto.genSalt());
            final MetaProperty<String> saltProp = secret.getProperty("salt");
            final int maxTries = 1000;
            int tries = 1;
            while (!saltProp.isValid() && tries < maxTries) {
                secret.setSalt(crypto.genSalt());
                tries++;
            }
            // we've tried hard, so if the salt is still not unique than bad luck...
            if (!saltProp.isValid()) {
                throw saltProp.getFirstFailure();
            }
            secret.setPassword(co$UserSecret.hashPasswd(passwd, secret.getSalt()));
            secret.setResetUuid(null);
            final UserSecret savedSecret = co$UserSecret.save(secret);

            // clear all the current user sessions
            this.<IUserSession, UserSession>co$(UserSession.class).clearAll(savedSecret.getKey());
            return savedSecret;
        } catch (final Exception ex) {
            logger.warn("Could not reset password for user [%s].", ex);
            throw new SecurityException("Could not reset user password.", ex);
        }
    }

    @Override
    public boolean isPasswordStrong(final String passwd) {
        final Zxcvbn zxcvbn = new Zxcvbn();
        final Strength strength = zxcvbn.measure(passwd);
        final double strengthTarget = (1 * 365 /* years */ + 90 /* days */ ) * 24 /* hours*/ * 60 /* minutes */ * 60 /* seconds */;
        final double secondsToCrack = strength.getCrackTimeSeconds().getOnlineNoThrottling10perSecond();
        return strengthTarget <= secondsToCrack;
    }

    @Override
    public Optional<User> findUserByResetUuid(final String uuid) {
        if (isEmpty(uuid)) {
            throw new SecurityException("User password resetting UUID cannot be empty.");
        }

        final String[] uuidParts = uuid.split(SECRET_RESET_UUID_SEPERATOR);
        if (uuidParts.length != 3) {
            return empty();
        }

        final EntityResultQueryModel<User> query = select(UserSecret.class)
                .where().prop("resetUuid").eq().val(uuid)
                .yield().prop("key").modelAsEntity(User.class);

        final User user = getEntity(from(query).with(fetchAll(User.class)).model());
        return ofNullable(user);
    }

    @Override
    public Optional<UserSecret> assignPasswordResetUuid(final String usernameOrEmail, final Date expirationTime) {
        // Let's try to find a user by username or email.
        // In the SSO authentication mode, it is necessary to exclude those users who are restricted to SSO only.
        final ICompoundCondition0<User> rsoCondition = select(User.class)
                .where()
                .prop(ACTIVE).eq().val(true)
                .and()
                .begin()
                    .lowerCase().prop(KEY).eq().lowerCase().val(usernameOrEmail).or()
                    .lowerCase().prop(EMAIL).eq().lowerCase().val(usernameOrEmail)
                .end();
        final EntityResultQueryModel<User> query = (ssoMode ? rsoCondition.and().prop(User.SSO_ONLY).eq().val(false) : rsoCondition).model();

        final User user = getEntity(from(query).with(fetchAll(User.class)).model());

        // if the user was found then a password reset request UUID needs to be generated
        // and associated wit the identified user
        if (user != null) {
            final UserSecretCo co$UserSecret = co$(UserSecret.class);
            final UserSecret secret = findOrCreateNewSecret(user, co$UserSecret);
            final long expirationTimeMillis = expirationTime.getTime();
            final String uuid = "%s%s%s%s%s".formatted(user.getKey(), SECRET_RESET_UUID_SEPERATOR, crypto.nextSessionId(), SECRET_RESET_UUID_SEPERATOR, expirationTimeMillis);
            final var encodedUuid = UrlEscapers.urlFragmentEscaper().escape(uuid);
            return of(co$UserSecret.save(secret.setResetUuid(encodedUuid)));
        }

        return empty();
    }

    @Override
    @SessionRequired
    public boolean isPasswordResetUuidValid(final String uuid, final Date now) {
        final Optional<User> user = findUserByResetUuid(uuid);
        // If there is no user associated with UUID then it cannot be valid.
        if (user.isEmpty()) {
            return false;
        } else {
            // If a corresponding user was found, then UUID needs to be checked for expiration.
            final String[] uuidParts = uuid.split(SECRET_RESET_UUID_SEPERATOR);
            final long expirationTime = Long.parseLong(uuidParts[2]);

            final boolean expired = now.getTime() >= expirationTime;
            if (expired) {
                // Remove expired UUID.
                final UserSecretCo co$UserSecret = co$(UserSecret.class);
                final UserSecret secret = findOrCreateNewSecret(user.get(), co$UserSecret);
                co$UserSecret.save(secret.setResetUuid(null));
            }

            return !expired;
        }
    }

    @Override
    @SessionRequired
    @Authorise(User_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> userIds) {
        // first clear and remove all user sessions
        for (final Long userId: userIds) {
            final User user = findById(userId, fetchAll(User.class));
            this.<IUserSession, UserSession>co$(UserSession.class).clearAll(user);
        }

        // then let's remove all user related configurations
        final Long[] ids = userIds.toArray(new Long[]{});

        final var qUserRoleAssociations = select(UserAndRoleAssociation.class).where().prop("user.id").in().values(ids).model();
        if (co$(UserAndRoleAssociation.class).exists(qUserRoleAssociations)) {
            throw new InvalidStateException(ERR_DELETING_USERS_WITH_ROLES);
        }

        final EntityResultQueryModel<WebMenuItemInvisibility> qMainMenuItemInvisibility = select(WebMenuItemInvisibility.class).where().prop("owner.id").in().values(ids).model();
        this.co$(WebMenuItemInvisibility.class).batchDelete(qMainMenuItemInvisibility);

        final EntityResultQueryModel<EntityLocatorConfig> qEntityLocatorConfig = select(EntityLocatorConfig.class).where().prop("owner.id").in().values(ids).model();
        this.co$(EntityLocatorConfig.class).batchDelete(qEntityLocatorConfig);

        final EntityResultQueryModel<EntityMasterConfig> qEntityMasterConfig = select(EntityMasterConfig.class).where().prop("owner.id").in().values(ids).model();
        this.co$(EntityMasterConfig.class).batchDelete(qEntityMasterConfig);

        final EntityResultQueryModel<EntityCentreConfig> qEntityCentreConfig = select(EntityCentreConfig.class).where().prop("owner.id").in().values(ids).model();
        this.co$(EntityCentreConfig.class).batchDelete(qEntityCentreConfig);

        // let's remove secrets for all users
        co$(UserSecret.class).batchDelete(userIds);

        // and only now can we delete users
        return defaultBatchDelete(userIds);
    }
}