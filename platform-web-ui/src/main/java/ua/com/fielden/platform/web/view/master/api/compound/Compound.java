package ua.com.fielden.platform.web.view.master.api.compound;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.MasterWithMasterBuilder;

public class Compound {

    protected static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miMaster(
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

    protected static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miCentre(
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

    protected static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityActionConfig miAction(
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
        // Here master entity is required for situations where new entity with compound master is created from a centre embedded into another compound master
        return open(openCompoundMasterActionType, icon, shortDesc, longDesc, prefDim, context().withMasterEntity().build());
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
        return open(openCompoundMasterActionType, null, shortDesc, null, prefDim, context().withCurrentEntity().build());
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
        return open(openCompoundMasterActionType, null, shortDesc, longDesc, prefDim, context().withCurrentEntity().build());
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
        return open(openCompoundMasterActionType, icon, shortDesc, longDesc, prefDim, context().withCurrentEntity().build());
    }

    private static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig open(
            final Class<OPEN_ACTION> openCompoundMasterActionType,
            final String icon,
            final String shortDesc,
            final String longDesc,
            final PrefDim prefDim,
            final CentreContextConfig centreContextConfig
            ) {
        if (icon != null) {
            if (StringUtils.isEmpty(longDesc)) {
                return action(openCompoundMasterActionType)
                        .withContext(centreContextConfig)
                        .icon(icon)
                        .shortDesc(shortDesc)
                        .shortcut("alt+n")
                        .prefDimForView(prefDim)
                        .withNoParentCentreRefresh()
                        .build();
            } else {
                return action(openCompoundMasterActionType)
                        .withContext(centreContextConfig)
                        .icon(icon)
                        .shortDesc(shortDesc)
                        .longDesc(longDesc)
                        .shortcut("alt+n")
                        .prefDimForView(prefDim)
                        .withNoParentCentreRefresh()
                        .build();
            }
        } else {
            if (StringUtils.isEmpty(longDesc)) {
                return action(openCompoundMasterActionType)
                        .withContext(centreContextConfig)
                        .shortDesc(shortDesc)
                        .shortcut("alt+n")
                        .prefDimForView(prefDim)
                        .withNoParentCentreRefresh()
                        .build();
            } else {
                return action(openCompoundMasterActionType)
                        .withContext(centreContextConfig)
                        .shortDesc(shortDesc)
                        .longDesc(longDesc)
                        .shortcut("alt+n")
                        .prefDimForView(prefDim)
                        .withNoParentCentreRefresh()
                        .build();
            }
        }
    }
}
