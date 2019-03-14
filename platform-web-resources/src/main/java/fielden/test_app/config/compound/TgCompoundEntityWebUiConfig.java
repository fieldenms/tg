package fielden.test_app.config.compound;

import static ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao.enhanceEmbededCentreQuery;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.test.server.config.LocatorFactory.mkLocator;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.actionAddDesc;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.actionEditDesc;
import static ua.com.fielden.platform.web.test.server.config.StandardScrollingConfigs.standardEmbeddedScrollingConfig;
import static ua.com.fielden.platform.web.test.server.config.StandardScrollingConfigs.standardStandaloneScrollingConfig;

import java.util.Optional;

import com.google.inject.Injector;

import fielden.test_app.main.menu.compound.MiTgCompoundEntity;
import fielden.test_app.main.menu.compound.MiTgCompoundEntityMaster_TgCompoundEntityChild;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityLocator;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.producers.TgCompoundEntityDetailProducer;
import ua.com.fielden.platform.sample.domain.compound.producers.TgCompoundEntityProducer;
import ua.com.fielden.platform.sample.domain.compound.ui_actions.OpenTgCompoundEntityMasterAction;
import ua.com.fielden.platform.sample.domain.compound.ui_actions.producers.OpenTgCompoundEntityMasterActionProducer;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.test.server.config.StandardActions;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.compound.Compound;
import ua.com.fielden.platform.web.view.master.api.compound.impl.CompoundMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/** 
 * {@link TgCompoundEntity} Web UI configuration.
 * 
 * @author TG Team
 *
 */
public class TgCompoundEntityWebUiConfig {

    private final Injector injector;

    public final EntityCentre<TgCompoundEntity> centre;
    public final EntityMaster<TgCompoundEntity> master;
    public final EntityMaster<OpenTgCompoundEntityMasterAction> compoundMaster;
    public final EntityActionConfig editTgCompoundEntityAction;
    public final EntityActionConfig newTgCompoundEntityAction;

    public static TgCompoundEntityWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgCompoundEntityWebUiConfig(injector, builder);
    }

    private TgCompoundEntityWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        this.injector = injector;

        final PrefDim dims = mkDim(960, 640, Unit.PX);
        editTgCompoundEntityAction = Compound.openEdit(OpenTgCompoundEntityMasterAction.class, TgCompoundEntity.ENTITY_TITLE, actionEditDesc(TgCompoundEntity.ENTITY_TITLE), dims);
        newTgCompoundEntityAction = Compound.openNew(OpenTgCompoundEntityMasterAction.class, "add-circle-outline", TgCompoundEntity.ENTITY_TITLE, actionAddDesc(TgCompoundEntity.ENTITY_TITLE), dims);
        builder.registerOpenMasterAction(TgCompoundEntity.class, editTgCompoundEntityAction);

        centre = createTgCompoundEntityCentre(builder);
        builder.register(centre);

        master = createTgCompoundEntityMaster();
        builder.register(master);

        compoundMaster = CompoundMasterBuilder.<TgCompoundEntity, OpenTgCompoundEntityMasterAction>create(injector, builder)
            .forEntity(OpenTgCompoundEntityMasterAction.class)
            .withProducer(OpenTgCompoundEntityMasterActionProducer.class)
            .addMenuItem(TgCompoundEntityMaster_OpenMain_MenuItem.class)
                .icon("icons:picture-in-picture")
                .shortDesc(OpenTgCompoundEntityMasterAction.MAIN)
                .longDesc(TgCompoundEntity.ENTITY_TITLE + " main")
                .withView(master)
            .also()
            .addMenuItem(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.class)
                .icon("icons:view-module")
                .shortDesc(OpenTgCompoundEntityMasterAction.TGCOMPOUNDENTITYDETAILS)
                .longDesc(TgCompoundEntity.ENTITY_TITLE + " " + OpenTgCompoundEntityMasterAction.TGCOMPOUNDENTITYDETAILS)
                .withView(createTgCompoundEntityDetailMaster())
            .also()
            .addMenuItem(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class)
                .icon("icons:view-module")
                .shortDesc(OpenTgCompoundEntityMasterAction.TGCOMPOUNDENTITYCHILDS)
                .longDesc(TgCompoundEntity.ENTITY_TITLE + " " + OpenTgCompoundEntityMasterAction.TGCOMPOUNDENTITYCHILDS)
                .withView(createTgCompoundEntityChildCentre())
            .done();
        builder.register(compoundMaster);
    }

    /**
     * Creates entity centre for {@link TgCompoundEntity}.
     *
     * @return
     */
    private EntityCentre<TgCompoundEntity> createTgCompoundEntityCentre(final IWebUiBuilder builder) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);
        final EntityActionConfig locator = mkLocator(builder, injector, TgCompoundEntityLocator.class, "tgCompoundEntity");
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgCompoundEntity.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgCompoundEntity.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgCompoundEntity> ecc = EntityCentreBuilder.centreFor(TgCompoundEntity.class)
                //.runAutomatically()
                .addFrontAction(newTgCompoundEntityAction).also()
                .addFrontAction(locator)
                .addTopAction(newTgCompoundEntityAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(locator).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgCompoundEntity.class).also()
                .addCrit("desc").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withScrollingConfig(standardStandaloneScrollingConfig(0))
                .addProp("this").order(1).asc().minWidth(100)
                    .withSummary("total_count_", "COUNT(SELF)", String.format("Count:The total number of matching %ss.", TgCompoundEntity.ENTITY_TITLE))
                    .withAction(editTgCompoundEntityAction).also()
                .addProp("desc").minWidth(300)
                .addPrimaryAction(editTgCompoundEntityAction)
                .build();

        return new EntityCentre<>(MiTgCompoundEntity.class, MiTgCompoundEntity.class.getSimpleName(), ecc, injector, null);
    }

    /**
     * Creates entity master for {@link TgCompoundEntity}.
     *
     * @return
     */
    private EntityMaster<TgCompoundEntity> createTgCompoundEntityMaster() {
        final String layout = LayoutComposer.mkGridForMasterFitWidth(1, 2);

        final IMaster<TgCompoundEntity> masterConfig = new SimpleMasterBuilder<TgCompoundEntity>().forEntity(TgCompoundEntity.class)
                .addProp("key").asSinglelineText().also()
                .addProp("desc").asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(PrefDim.mkDim(400, 300))
                .done();

        return new EntityMaster<>(TgCompoundEntity.class, TgCompoundEntityProducer.class, masterConfig, injector);
    }

    private EntityMaster<TgCompoundEntityDetail> createTgCompoundEntityDetailMaster() {

        final String layout = LayoutComposer.mkGridForMasterFitWidth(1, 1);

        final IMaster<TgCompoundEntityDetail> config = new SimpleMasterBuilder<TgCompoundEntityDetail>().forEntity(TgCompoundEntityDetail.class)
                // row 1
                .addProp("desc").asSinglelineText().also()
                // entity actions
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancels current changes if any or refresh the data")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<>(
                TgCompoundEntityDetail.class,
                TgCompoundEntityDetailProducer.class,
                config,
                injector);
    }
    private EntityCentre<TgCompoundEntityChild> createTgCompoundEntityChildCentre() {
        final Class<TgCompoundEntityChild> root = TgCompoundEntityChild.class;
        final String layout = LayoutComposer.mkVarGridForCentre(1);

        final EntityActionConfig standardNewAction = StandardActions.NEW_WITH_MASTER_ACTION.mkAction(TgCompoundEntityChild.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgCompoundEntityChild.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_EMBEDDED_CENTRE_ACTION.mkAction(TgCompoundEntityChild.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgCompoundEntityChild.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgCompoundEntityChild> ecc = EntityCentreBuilder.centreFor(root)
                .runAutomatically()
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("date").asRange().date()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withScrollingConfig(standardEmbeddedScrollingConfig(0))
                .addProp("date").order(1).asc().minWidth(80)
                    .withSummary("total_count_", "COUNT(SELF)", String.format("Count:The total number of matching %ss.", TgCompoundEntityChild.ENTITY_TITLE))
                .addPrimaryAction(standardEditAction)
                .setQueryEnhancer(TgCompoundEntityMaster_TgCompoundEntityChildCentre_QueryEnhancer.class, context().withMasterEntity().build())
                .build();

        return new EntityCentre<>(MiTgCompoundEntityMaster_TgCompoundEntityChild.class, MiTgCompoundEntityMaster_TgCompoundEntityChild.class.getSimpleName(), ecc, injector, null);
    }

    private static class TgCompoundEntityMaster_TgCompoundEntityChildCentre_QueryEnhancer implements IQueryEnhancer<TgCompoundEntityChild> {
        @Override
        public ICompleted<TgCompoundEntityChild> enhanceQuery(final IWhere0<TgCompoundEntityChild> where, final Optional<CentreContext<TgCompoundEntityChild, ?>> context) {
            return enhanceEmbededCentreQuery(where, createConditionProperty("tgCompoundEntity"), context.get().getMasterEntity().getKey());
        }
    }

}