package ua.com.fielden.platform.web.view.master.api.compound;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder1;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder6;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.MasterWithMasterBuilder;

public class Compound {

    protected static <K extends AbstractEntity<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miMaster(
            final Class<MENU_ITEM> menuItemType,
            final EntityMaster<? extends AbstractEntity<?>> embeddedMaster,
            final Injector injector) {
        return new EntityMaster<MENU_ITEM>(
                menuItemType,
                new MasterWithMasterBuilder<MENU_ITEM>()
                /*  */.forEntityWithSaveOnActivate(menuItemType)
                /*  */.withMaster(embeddedMaster)
                /*  */.done(),
                injector);
    }

    protected static <K extends AbstractEntity<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miCentre(
            final Class<MENU_ITEM> menuItemType,
            final EntityCentre<? extends AbstractEntity<?>> embeddedCentre,
            final Injector injector) {
        return new EntityMaster<MENU_ITEM>(
                menuItemType,
                new MasterWithCentreBuilder<MENU_ITEM>()
                    .forEntityWithSaveOnActivate(menuItemType)
                    .withCentre(embeddedCentre)
                    .done(),
                injector);
    }

    public static <DETAILS_ACTION extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<DETAILS_ACTION> detailsCentre(
            final Class<DETAILS_ACTION> menuItemType,
            final EntityCentre<? extends AbstractEntity<?>> embeddedCentre,
            final Injector injector) {
        return new EntityMaster<DETAILS_ACTION>(
                menuItemType,
                new MasterWithCentreBuilder<DETAILS_ACTION>()
                    .forEntityWithSaveOnActivate(menuItemType)
                    .withCentre(embeddedCentre)
                    .done(),
                injector);
    }

    public static <DETAILS_ACTION extends AbstractFunctionalEntityWithCentreContext<?>> EntityMaster<DETAILS_ACTION> detailsMaster(
            final Class<DETAILS_ACTION> menuItemType,
            final EntityMaster<? extends AbstractEntity<?>> embeddedMaster,
            final Injector injector) {
        return new EntityMaster<DETAILS_ACTION>(
                menuItemType,
                new MasterWithMasterBuilder<DETAILS_ACTION>()
                    .forEntityWithSaveOnActivate(menuItemType)
                    .withMaster(embeddedMaster)
                    .done(),
                injector);
    }

    protected static <K extends AbstractEntity<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityActionConfig miAction(
            final Class<MENU_ITEM> menuItemType,
            final String icon,
            final String shortDesc,
            final String longDesc) {
        return action(menuItemType)
                .withContext(context().withMasterEntity().build())
                .icon(icon)
                .shortDesc(shortDesc)
                .longDesc(longDesc)
                .withNoParentCentreRefresh()
                .build();
    }

    /**
     * Creates standard action of opening compound master for new entity. This is usually to be placed as "+" top level action on some centre.
     *
     * @param openCompoundMasterActionType -- functional entity type for compound master opening
     * @param icon -- icon for action
     * @param shortDesc -- short description of action
     * @param longDesc -- long description of action
     * @param prefDim - preferred dimension for compound master dialog
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openNew(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        // TODO here empty context will be relevant in most cases, please use it when API for empty context will be implemented (for example, context().empty().build())
        return open(openCompoundMasterActionType, empty(), ofNullable(icon), empty(), shortDesc, ofNullable(longDesc), prefDim, context().withSelectionCrit().build());
    }

    /**
     * The same as {@link #openNew(Class, String, String, String, PrefDim)}, but with <code>iconStyle</code> that must have a value.
     *
     * @param openCompoundMasterActionType
     * @param icon
     * @param iconStyle
     * @param shortDesc
     * @param longDesc
     * @param prefDim
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openNew(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final String iconStyle,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, empty(), ofNullable(icon), ofNullable(iconStyle), shortDesc, ofNullable(longDesc), prefDim, context().withSelectionCrit().build());
    }

    /**
     * Creates standard action for opening a compound master for a new entity where a master entity is required as part of the context.
     * Such actions should always be a part of some other entity master. For example, for creating of new entities from an embedded centre on some compound master.
     *
     * @param openCompoundMasterActionType
     * @param icon
     * @param shortDesc
     * @param longDesc
     * @param prefDim
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openNewWithMaster(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, empty(), ofNullable(icon), empty(), shortDesc, ofNullable(longDesc), prefDim, context().withMasterEntity().build());
    }

    /**
     * Creates standard action of opening compound master to edit entity. This is usually to be placed as property action on some centre.
     *
     * @param openCompoundMasterActionType -- functional entity type for compound master opening
     * @param shortDesc -- short description of action
     * @param prefDim - preferred dimension for compound master dialog
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openEdit(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String shortDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, of(new EntityNavigationPreAction(shortDesc)), empty(), empty(), shortDesc, empty(), prefDim, context().withCurrentEntity().build());
    }

    /**
     * Creates standard action of opening compound master to edit entity. This is usually to be placed as property action on some centre.
     *
     * @param openCompoundMasterActionType -- functional entity type for compound master opening
     * @param shortDesc -- short description of action
     * @param longDesc -- long description of action
     * @param prefDim - preferred dimension for compound master dialog
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openEdit(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, of(new EntityNavigationPreAction(shortDesc)), empty(), empty(), shortDesc, ofNullable(longDesc), prefDim, context().withCurrentEntity().build());
    }

    /**
     * Creates standard action of opening compound master to edit entity. This is usually to be placed as property action on some centre.
     *
     * @param openCompoundMasterActionType -- functional entity type for compound master opening
     * @param icon -- icon for action
     * @param shortDesc -- short description of action
     * @param longDesc -- long description of action
     * @param prefDim - preferred dimension for compound master dialog
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openEdit(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, of(new EntityNavigationPreAction(shortDesc)), ofNullable(icon), empty(), shortDesc, ofNullable(longDesc), prefDim, context().withCurrentEntity().build());
    }
    
    /**
     * Creates action of opening compound master to edit entity with a custom computational context. Could be used as a secondary action on some centre.
     *
     * @param openCompoundMasterActionType -- functional entity type for compound master opening
     * @param icon -- icon for action
     * @param computation -- custom computation for action
     * @param shortDesc -- short description of action
     * @param longDesc -- long description of action
     * @param prefDim - preferred dimension for compound master dialog
     * @return
     */
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openEdit(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, of(new EntityNavigationPreAction(shortDesc)), ofNullable(icon), empty(), shortDesc, ofNullable(longDesc), prefDim, context().withCurrentEntity().withComputation(computation).build());
    }
    

    private static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig open(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final Optional<IPreAction> preAction,
            final Optional<String> icon,
            final Optional<String> iconStyle,
            final String shortDesc,
            final Optional<String> longDesc,
            final PrefDim prefDim,
            final CentreContextConfig centreContextConfig
            ) {
        final IEntityActionBuilder1<AbstractEntity<?>> actionPart = action(openCompoundMasterActionType).withContext(centreContextConfig);
        IEntityActionBuilder2<AbstractEntity<?>> actionWithPreAction;
        if (preAction.isPresent()) {
            actionWithPreAction = actionPart.preAction(preAction.get());
        } else {
            actionWithPreAction = actionPart;
        }
        final IEntityActionBuilder6<AbstractEntity<?>> actionWithIcon;
        if (icon.isPresent()) {
            actionWithIcon = actionWithPreAction.icon(icon.get())
                    .withStyle(iconStyle.orElse(null))
                    .shortDesc(shortDesc);
        } else {
            actionWithIcon = actionWithPreAction.shortDesc(shortDesc);
        }
        if (longDesc.isPresent() && !StringUtils.isEmpty(longDesc.get())) {
            return actionWithIcon.longDesc(longDesc.get())
                    .shortcut("alt+n")
                    .prefDimForView(prefDim)
                    .withNoParentCentreRefresh()
                    .build();
        } else {
            return actionWithIcon.shortcut("alt+n")
                    .prefDimForView(prefDim)
                    .withNoParentCentreRefresh()
                    .build();
        }
    }
}
