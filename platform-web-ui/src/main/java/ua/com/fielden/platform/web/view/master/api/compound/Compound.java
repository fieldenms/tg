package ua.com.fielden.platform.web.view.master.api.compound;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.ICompoundMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.compound.impl.CompoundMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.MasterWithMasterBuilder;

public class Compound {
    
    public static <T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> ICompoundMasterBuilder<T, F> create(final Injector injector, final IWebUiBuilder builder) {
        return new CompoundMasterBuilder<T, F>(injector, builder);
    }
    
    public static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miMaster(
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
    
    public static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityMaster<MENU_ITEM> miCentre(
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
    
    public static <K extends Comparable<?>, MENU_ITEM extends AbstractFunctionalEntityForCompoundMenuItem<K>> EntityActionConfig miAction(
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
    
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openNew(
            final Class<OPEN_ACTION> openCompoundMasterActionType, 
            final String icon, 
            final String shortDesc, 
            final String longDesc,
            final PrefDim prefDim) {
        // TODO here empty context will be relevant in most cases, please use it when API for empty context will be implemented (for example, context().empty().build())
        return open(openCompoundMasterActionType, icon, shortDesc, longDesc, prefDim, context().withSelectionCrit().build());
    }
    
    public static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig openEdit(
            final Class<OPEN_ACTION> openCompoundMasterActionType, 
            final String shortDesc, 
            final PrefDim prefDim) {
        return open(openCompoundMasterActionType, null, shortDesc, null, prefDim, context().withCurrentEntity().build());
    }
    
    private static <K extends Comparable<?>, OPEN_ACTION extends AbstractFunctionalEntityWithCentreContext<K>> EntityActionConfig open(
            final Class<OPEN_ACTION> openCompoundMasterActionType, 
            final String icon, 
            final String shortDesc, 
            final String longDesc,
            final PrefDim prefDim,
            final CentreContextConfig centreContextConfig
            ) {
        return icon != null ? 
                action(openCompoundMasterActionType)
                    .withContext(centreContextConfig)
                    .icon(icon)
                    .shortDesc(shortDesc)
                    .longDesc(longDesc)
                    .prefDimForView(prefDim)
                    .withNoParentCentreRefresh()
                    .build():
                action(openCompoundMasterActionType)
                    .withContext(centreContextConfig)
                    .shortDesc(shortDesc)
                    .prefDimForView(prefDim)
                    .withNoParentCentreRefresh()
                    .build();
    }
}
