package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.security.tokens.TokenUtils.findToken;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.basic.config.MenuVisibilityMode;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;


/**
 * DAO implementation for companion object {@link IMenuSaveAction}.
 *
 * @author Developers
 *
 */
@EntityType(MenuSaveAction.class)
public class MenuSaveActionDao extends CommonEntityDao<MenuSaveAction> implements IMenuSaveAction {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final IUserProvider userProvider;
    private final String menuVisibilityMode;
    private final IMenuRetriever menuRetriever;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    public MenuSaveActionDao(final IFilter filter, final IUserProvider userProvider, final IMenuRetriever menuRetriever, final ISecurityTokenProvider securityTokenProvider, final @Named("menuVisibilityMode") String menuVisibilityMode) {
        super(filter);
        this.userProvider = userProvider;
        this.menuVisibilityMode = menuVisibilityMode;
        this.menuRetriever = menuRetriever;
        this.securityTokenProvider = securityTokenProvider;
    }

    @Override
    @SessionRequired
    public MenuSaveAction save(final MenuSaveAction entity) {
        if (userProvider.getUser().isBase()) {
            if (MenuVisibilityMode.tokenBased.name().equals(menuVisibilityMode)) {
                saveReadTokenRoleAssociations(entity);
            } else {
                saveBaseUserMenuVisibility(entity);
            }

        }
        return entity;
    }

    private void saveReadTokenRoleAssociations(final MenuSaveAction entity) {
        final SecurityRoleAssociationCo associationCo = co$(SecurityRoleAssociation.class);
        final Menu menu = menuRetriever.getMenuEntity(DeviceProfile.DESKTOP);
        final Set<SecurityRoleAssociation> removedAssociations = new HashSet<>();
        entity.getInvisibleMenuItems().forEach(menuItem -> {
            createAssociationOptionaly(menuItem, menu, associationCo).ifPresent(association -> removedAssociations.add(association));
        });
        final Set<SecurityRoleAssociation> addedAssociations = new HashSet<>();
        entity.getVisibleMenuItems().forEach(menuItem -> {
            createAssociationOptionaly(menuItem, menu, associationCo).ifPresent(association -> addedAssociations.add(association));
        });
        final SecurityRoleAssociationBatchAction batchAction = new SecurityRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        co$(SecurityRoleAssociationBatchAction.class).save(batchAction);
    }

    private Optional<SecurityRoleAssociation> createAssociationOptionaly(final String menuItem, final Menu menu, final SecurityRoleAssociationCo associationCo) {
        final List<String> menuParts = MenuProducer.decodeParts(menuItem.split("/"));
        return menuParts.stream()
            .reduce(Optional.of(menu), MenuSaveActionDao::accumulator, MenuSaveActionDao::combiner)
            .map(item -> {
                return associationCo.new_()
                        .setRole(userProvider.getUser().getRole())
                        .setSecurityToken(findToken(item.getView().getEntityType(), READ, securityTokenProvider).orElse(null));
            });
    }

    private static Optional<IMenuManager> accumulator(final Optional<IMenuManager> menuItemManager, final String menuPart) {
        return menuItemManager.flatMap(value -> value.getMenuItem(menuPart));
    }

    private static Optional<IMenuManager> combiner(final Optional<IMenuManager> menuItemManager1, final Optional<IMenuManager> menuItemManager2) {
        return menuItemManager2;
    }

    private void saveBaseUserMenuVisibility(final MenuSaveAction entity) {
        final IWebMenuItemInvisibility coMenuInvisibility = co$(WebMenuItemInvisibility.class);
        if (!entity.getInvisibleMenuItems().isEmpty()) {
            entity.getInvisibleMenuItems().forEach(menuItem -> {
                try {
                    coMenuInvisibility.save(getEntityFactory().newByKey(WebMenuItemInvisibility.class, userProvider.getUser(), menuItem));
                } catch (final EntityCompanionException e) {
                    logger.error(e.getMessage());
                }
            });
        }
        if (!entity.getVisibleMenuItems().isEmpty()) {
            final EntityResultQueryModel<WebMenuItemInvisibility> model = select(WebMenuItemInvisibility.class).where()//
            .prop("owner").eq().val(userProvider.getUser()).and()//
            .prop("menuItemUri").in().values(entity.getVisibleMenuItems().toArray(new String[0])).model();
            coMenuInvisibility.batchDelete(model);
        }
    }
}