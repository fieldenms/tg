package ua.com.fielden.platform.domaintree;

import java.util.Optional;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * This interface defines how domain tree can be managed in the global client application scope. It manages all entity-centres as well as locator managers in entity-masters. To
 * manage default type-related locators, please use {@link IGlobalDomainTreeRepresentation} member by accessing it with {@link #getGlobalRepresentation()} method.<br>
 * <br>
 * 
 * The only single instance of this interface should be used for client application and persisted.
 * 
 * @author TG Team
 * 
 */
public interface IGlobalDomainTreeManager {
    /**
     * {@link User} companion.
     * 
     * @return
     */
    IUser coUser();
    
    /**
     * Finds configuration with some <code>name</code>.
     * 
     * @return
     */
    EntityCentreConfig findConfig(final Class<?> menuItemType, final String name);
    
    /**
     * Saves configuration using {@code quickSave} instead of {@code save}.
     */
    void saveConfig(final EntityCentreConfig config);
    
    /**
     * Returns a user provider. The user is a part of domain tree context. Some domain tree actions is permitted only for base users. Some behaviour also can differ for different
     * types of users.
     * 
     * @return
     */
    IUserProvider getUserProvider();

    /**
     * If {@link #getUserProvider()} contains base user then this method returns empty {@link Optional}.
     * Otherwise returns global manager for parent user (aka the user which this global manager's user {@link #getUserProvider()} is based on).
     * <p>
     * This method is used to initialise centre configurations for non-base users from base configurations. 
     * 
     * @return
     */
    Optional<IGlobalDomainTreeManager> basedOnManager();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////// ENTITY CENTRE MANAGERS //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialises a brand new <b>entity-centre manager</b> for menu item type <b>menuItemType</b> with specified <b>name</b>. The <b>name</b> should represent a name of
     * non-principle entity-centre or <code>null</code> for principle entity-centre. <br>
     * <br>
     * 
     * <b>User-driven constraints</b>: Base or non-base users can do nothing with non-visible (or non-existent) reports (throws {@link DomainTreeException}). Non-base users
     * can init, access, modify, saveAs, ask for the changes etc. for all reports that are visible to him (its own reports + its base user's reports including principle), but
     * cannot save/remove base user's reports (throws {@link DomainTreeException}). Base users can init, access, modify, saveAs, ask for the changes etc. for all reports that
     * are visible to him (its own reports including principle), but cannot remove principle report (throws {@link DomainTreeException}). <br>
     * <br>
     * 
     * The current version of a entity-centre manager should be initialised before usage ({@link #initEntityCentreManager(Class, String)}), then can be altered by its methods, and
     * then saved ({@link #saveEntityCentreManager(Class, String, String)}), saved as non-principle entity-centre ({@link #saveAsEntityCentreManager(Class, String, String, String)}) or discarded (
     * {@link #discardEntityCentreManager(Class, String)}). After that it can be removed ({@link #removeEntityCentreManager(Class, String)}) and asked "if changed" (
     * {@link #isChangedEntityCentreManager(Class, String)}).<br>
     * <br>
     * 
     * @param menuItemType
     *            -- a menu item type relevant to an entity-centre manager.
     * @param name
     *            -- should represent a name of non-principle entity-centre or <code>null</code> for principle entity-centre.
     */
    Either<ICentreDomainTreeManagerAndEnhancer, DomainTreeException> initEntityCentreManager(final Class<?> menuItemType, final String name);

    /**
     * Saves a current version of <b>entity-centre manager</b> for menu item type <b>menuItemType</b> with specified <b>name</b>. If a current version of <b>entity-centre
     * manager</b> was freezed then it just "accepts" the current version. The <b>name</b> should represent a name of non-principle entity-centre or <code>null</code> for principle
     * entity-centre. <br>
     * <br>
     * 
     * <b>User-driven constraints</b>: Base or non-base users can do nothing with non-visible (or non-existent) reports (throws {@link DomainTreeException}). Non-base users
     * can init, access, modify, saveAs, ask for the changes etc. for all reports that are visible to him (its own reports + its base user's reports including principle), but
     * cannot save/remove base user's reports (throws {@link DomainTreeException}). Base users can init, access, modify, saveAs, ask for the changes etc. for all reports that
     * are visible to him (its own reports including principle), but cannot remove principle report (throws {@link DomainTreeException}). <br>
     * <br>
     * 
     * Throws {@link DomainTreeException} when entity-centre was not initialised.<br>
     * <br>
     * 
     * The current version of a entity-centre manager should be initialised before usage ({@link #initEntityCentreManager(Class, String)}), then can be altered by its methods, and
     * then saved ({@link #saveEntityCentreManager(Class, String, String)}), saved as non-principle entity-centre ({@link #saveAsEntityCentreManager(Class, String, String, String)}) or discarded (
     * {@link #discardEntityCentreManager(Class, String)}). After that it can be removed ({@link #removeEntityCentreManager(Class, String)}) and asked "if changed" (
     * {@link #isChangedEntityCentreManager(Class, String)}).<br>
     * <br>
     * 
     * @param menuItemType
     *            -- a menu item type relevant to an entity-centre manager.
     * @param name
     *            -- should represent a name of non-principle entity-centre or <code>null</code> for principle entity-centre.
     * @param newDesc
     *            -- new description for configuration
     */
    IGlobalDomainTreeManager saveEntityCentreManager(final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer centre, final String newDesc);
    
    /**
     * Duplicates an <b>entity-centre manager</b> for menu item type <b>menuItemType</b> with <b>originalName</b> to an <b>entity-centre manager</b> with <b>newName</b> and then
     * saves a manager copy. The <b>originalName</b> and <b>newName</b> should represent a names of non-principle entity-centres or <code>null</code> for principle entity-centres. <br>
     * <br>
     * 
     * <b>User-driven constraints</b>: Base or non-base users can do nothing with non-visible (or non-existent) reports (throws {@link DomainTreeException}). Non-base users
     * can init, access, modify, saveAs, ask for the changes etc. for all reports that are visible to him (its own reports + its base user's reports including principle), but
     * cannot save/remove base user's reports (throws {@link DomainTreeException}). Base users can init, access, modify, saveAs, ask for the changes etc. for all reports that
     * are visible to him (its own reports including principle), but cannot remove principle report (throws {@link DomainTreeException}). <br>
     * <br>
     * 
     * Throws {@link DomainTreeException} when entity-centre was not initialised.<br>
     * <br>
     * 
     * The current version of a entity-centre manager should be initialised before usage ({@link #initEntityCentreManager(Class, String)}), then can be altered by its methods, and
     * then saved ({@link #saveEntityCentreManager(Class, String, String)}), saved as non-principle entity-centre ({@link #saveAsEntityCentreManager(Class, String, String, String)}) or discarded (
     * {@link #discardEntityCentreManager(Class, String)}). After that it can be removed ({@link #removeEntityCentreManager(Class, String)}) and asked "if changed" (
     * {@link #isChangedEntityCentreManager(Class, String)}).<br>
     * <br>
     * 
     * @param menuItemType
     *            -- a menu item type relevant to an entity-centre manager.
     * @param originalName
     *            -- should represent a name of non-principle entity-centre or <code>null</code> for principle entity-centre, which should be copied.
     * @param newName
     *            -- should represent a not empty (if empty -- throws {@link DomainTreeException}) name of new entity-centre.
     * @param newDesc 
     *            -- new description for configuration
     */
    ICentreDomainTreeManagerAndEnhancer saveAsEntityCentreManager(final Class<?> menuItemType, final ICentreDomainTreeManagerAndEnhancer centre, final String newName, final String newDesc);
    
}