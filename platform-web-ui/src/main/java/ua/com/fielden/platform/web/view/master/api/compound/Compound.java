package ua.com.fielden.platform.web.view.master.api.compound;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.MasterWithMasterBuilder;

public class Compound {
    
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
    
}
