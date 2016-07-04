package ua.com.fielden.platform.web.test.server;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.yesNo;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.single;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.sample.domain.ExportAction;
import ua.com.fielden.platform.sample.domain.ExportActionProducer;
import ua.com.fielden.platform.sample.domain.ITgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.ITgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParentProducer;
import ua.com.fielden.platform.sample.domain.TgCreatePersistentStatusAction;
import ua.com.fielden.platform.sample.domain.TgCreatePersistentStatusActionProducer;
import ua.com.fielden.platform.sample.domain.TgDeletionTestEntity;
import ua.com.fielden.platform.sample.domain.TgDeletionTestEntityProducer;
import ua.com.fielden.platform.sample.domain.TgDummyAction;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependencyProducer;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContextProducer;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesProducer;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.sample.MiDeletionTestEntity;
import ua.com.fielden.platform.ui.menu.sample.MiDetailsCentre;
import ua.com.fielden.platform.ui.menu.sample.MiEntityCentreNotGenerated;
import ua.com.fielden.platform.ui.menu.sample.MiTgCollectionalSerialisationParent;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithPropertyDependency;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.ui.menu.sample.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties1;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties2;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties3;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties4;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.action.post.FileSaverPostAction;
import ua.com.fielden.platform.web.action.pre.ExportPreAction;
import ua.com.fielden.platform.web.app.AbstractWebUiConfig;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2Properties;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithRunConfig;
import ua.com.fielden.platform.web.config.StandardMastersWebUiConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.resources.webui.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.resources.webui.UserRoleWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.UserWebUiConfig;
import ua.com.fielden.platform.web.test.matchers.ContextMatcher;
import ua.com.fielden.platform.web.test.server.master_action.NewEntityAction;
import ua.com.fielden.platform.web.test.server.master_action.NewEntityActionWebUiConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;

import com.google.inject.Inject;

/**
 * App-specific {@link IWebUiConfig} implementation.
 *
 * @author TG Team
 *
 */
public class WebUiConfig extends AbstractWebUiConfig {

    private final String domainName;
    private final String path;
    private final int port;

    public WebUiConfig(final String domainName, final int port, final Workflows workflow, final String path) {
        super("TG Test and Demo Application", workflow, new String[0]);
        if (StringUtils.isEmpty(domainName) || StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Both the domain name and application binding path should be specified.");
        }
        this.domainName = domainName;
        this.port = port;
        this.path = path;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getPath() {
        return path;
    }

    private EntityMaster<ExportAction> createExportActionMaster() {
        final String bottomButtonPanel = "['horizontal', 'margin-top: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";
        final String actionButton = "'margin: 10px', 'width: 110px'";
        final IMaster<ExportAction> masterConfig = new SimpleMasterBuilder<ExportAction>()
                .forEntity(ExportAction.class)
                .addProp("count").asSpinner()
                .also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        " ['padding:20px', "
                        + " [['flex']],"
                        + format(bottomButtonPanel, actionButton, actionButton)
                        + "]"))
                .done();
        final EntityMaster<ExportAction> master = new EntityMaster<ExportAction>(
                ExportAction.class,
                ExportActionProducer.class,
                masterConfig,
                injector());

        return master;
    }

    /**
     * Configures the {@link WebUiConfig} with custom centres and masters.
     */
    @Override
    public void initConfiguration() {
        configApp().setTimeFormat("HH:mm").setTimeWithMillisFormat("HH:mm:ss.SSS");
        // Add entity centres.

        //Centre configuration for deletion test case entity.
        final EntityCentre<TgDeletionTestEntity> deletionTestCentre = new EntityCentre<>(MiDeletionTestEntity.class, "TgDeletionTestEntity",
                EntityCentreBuilder.centreFor(TgDeletionTestEntity.class)
                        .addTopAction(action(EntityNewAction.class).
                                withContext(context().withSelectionCrit().build()).
                                icon("add-circle-outline").
                                shortDesc("Add new").
                                longDesc("Add new delete action").
                                withNoParentCentreRefresh().
                                build())
                        .also()
                        .addTopAction(action(EntityDeleteAction.class).
                                withContext(context().withSelectedEntities().build()).
                                icon("remove-circle-outline").
                                shortDesc("Delete selected").
                                longDesc("Deletes the selected entities").
                                build())
                        .addProp("this").also()
                        .addProp("desc")
                        // .addProp("additionalProp")
                        .build(), injector(), null);
        configApp().addCentre(deletionTestCentre);
        final SimpleMasterBuilder<TgDeletionTestEntity> masterBuilder = new SimpleMasterBuilder<TgDeletionTestEntity>();
        final String actionStyle = "'margin: 10px', 'width: 110px'";
        final String outer = "'flex', 'min-width:200px'";

        final String desktopTabletMasterLayout = ("['padding:20px',"
                //                      key
                + format("['justified', [%s]],", outer)
                //                      desc
                + format("['justified', [%s]],", outer)
                + format("['margin-top: 20px', 'wrap', 'justify-content: center', [%s],   [%s]]", actionStyle, actionStyle)
                + "]");
        @SuppressWarnings("unchecked")
        final IMaster<TgDeletionTestEntity> deletionMaster = masterBuilder.forEntity(TgDeletionTestEntity.class)
            .addProp("key").asSinglelineText().also()//
            .addProp("desc").asSinglelineText().also()
            .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancels current changes if any or refresh the data")
            .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), desktopTabletMasterLayout).done();
        configApp().addMaster(new EntityMaster<TgDeletionTestEntity>(TgDeletionTestEntity.class, TgDeletionTestEntityProducer.class, deletionMaster, injector()));

        final EntityCentre<TgFetchProviderTestEntity> fetchProviderTestCentre = new EntityCentre<>(MiTgFetchProviderTestEntity.class, "TgFetchProviderTestEntity",
                EntityCentreBuilder.centreFor(TgFetchProviderTestEntity.class)
                        .addCrit("property").asMulti().autocompleter(TgPersistentEntityWithProperties.class).setDefaultValue(multi().string().setValues("KE*").value()).
                        setLayoutFor(Device.DESKTOP, Optional.empty(), "[[]]")

                        .addProp("property")
                        .setFetchProvider(EntityUtils.fetch(TgFetchProviderTestEntity.class).with("additionalProperty"))
                        // .addProp("additionalProp")
                        .build(), injector(), null);
         configApp().addCentre(fetchProviderTestCentre);

         final EntityCentre<TgCollectionalSerialisationParent> collectionalSerialisationTestCentre = new EntityCentre<>(MiTgCollectionalSerialisationParent.class, "TgCollectionalSerialisationParent",
                 EntityCentreBuilder.centreFor(TgCollectionalSerialisationParent.class)
                         .addCrit("desc").asMulti().text()
                         .setLayoutFor(Device.DESKTOP, Optional.empty(), "[[]]")

                         .addProp("desc")
                         .addPrimaryAction(action(EntityEditAction.class).
                                 withContext(context().withCurrentEntity().withSelectionCrit().build()).
                                 icon("editor:mode-edit").
                                 shortDesc("Edit entity").
                                 longDesc("Opens master for editing this entity").
                                 build())
                         .build(), injector(), null);
          configApp().addCentre(collectionalSerialisationTestCentre);

        final EntityCentre<TgPersistentEntityWithProperties> detailsCentre = createEntityCentre(MiDetailsCentre.class, "Details Centre", createEntityCentreConfig(false, true, true, true));
        final EntityCentre<TgEntityWithPropertyDependency> propDependencyCentre = new EntityCentre<>(MiTgEntityWithPropertyDependency.class, "Property Dependency Example",
                EntityCentreBuilder.centreFor(TgEntityWithPropertyDependency.class)
                .runAutomatically()
                .addTopAction(action(EntityNewAction.class).
                        withContext(context().withSelectionCrit().build()).
                        icon("add-circle-outline").
                        shortDesc("Add new").
                        longDesc("Start continuous creation of entities").
                        build())
                .also()
                .addTopAction(action(EntityDeleteAction.class).
                        withContext(context().withSelectedEntities().build()).
                        icon("remove-circle-outline").
                        shortDesc("Delete selected").
                        longDesc("Deletes the selected entities").
                        build())
                .addCrit("property").asMulti().text().also()
                .addCrit("dependentProp").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")

                .addProp("this").also()
                .addProp("property").also()
                .addProp("dependentProp")
                .addPrimaryAction(action(EntityEditAction.class).
                        withContext(context().withCurrentEntity().withSelectionCrit().build()).
                        icon("editor:mode-edit").
                        shortDesc("Edit entity").
                        longDesc("Opens master for editing this entity").
                        build())
                .build(), injector(), (centre) -> {
                    // ... please implement some additional hooks if necessary -- for e.g. centre.getFirstTick().setWidth(...), add calculated properties through domain tree API, etc.
                    centre.getSecondTick().setWidth(TgEntityWithPropertyDependency.class, "", 60);
                    centre.getSecondTick().setWidth(TgEntityWithPropertyDependency.class, "property", 60);
                    centre.getSecondTick().setWidth(TgEntityWithPropertyDependency.class, "dependentProp", 60);
                    return centre;
                });

        final EntityCentre<TgEntityWithPropertyDescriptor> propDescriptorCentre = new EntityCentre<>(MiTgEntityWithPropertyDescriptor.class, "Property Descriptor Example",
                EntityCentreBuilder.centreFor(TgEntityWithPropertyDescriptor.class)
                .runAutomatically()
                .addTopAction(action(EntityNewAction.class).
                        withContext(context().withSelectionCrit().build()).
                        icon("add-circle-outline").
                        shortDesc("Add new").
                        longDesc("Starts creation of entities").
                        build())
                .also()
                .addTopAction(action(EntityDeleteAction.class).
                        withContext(context().withSelectedEntities().build()).
                        icon("remove-circle-outline").
                        shortDesc("Delete selected").
                        longDesc("Deletes the selected entities").
                        build())
                .addCrit("this").asMulti().autocompleter(TgEntityWithPropertyDescriptor.class).also()
                .addCrit("propertyDescriptor").asMulti().autocompleter((Class<PropertyDescriptor<TgPersistentEntityWithProperties>>) ClassesRetriever.findClass("ua.com.fielden.platform.entity.meta.PropertyDescriptor"))
                    .withMatcher(TgEntityWithPropertyDescriptorPropertyDescriptorMatcher.class, context().withSelectedEntities().build())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")

                .addProp("this")
                    .width(60)
                .also()
                .addProp("propertyDescriptor")
                    .width(160)
                .addPrimaryAction(action(EntityEditAction.class).
                        withContext(context().withCurrentEntity().withSelectionCrit().build()).
                        icon("editor:mode-edit").
                        shortDesc("Edit entity").
                        longDesc("Opens master for editing this entity").
                        build())
                .build(), injector(), null);

        final EntityCentre<TgPersistentEntityWithProperties> entityCentre = createEntityCentre(MiTgPersistentEntityWithProperties.class, "TgPersistentEntityWithProperties", createEntityCentreConfig(true, true, false, true));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentreNotGenerated = createEntityCentre(MiEntityCentreNotGenerated.class, "MiEntityCentreNotGenerated", createEntityCentreConfig(true, true, false, false));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre1 = createEntityCentre(MiTgPersistentEntityWithProperties1.class, "TgPersistentEntityWithProperties 1", createEntityCentreConfig(false, false, false, true));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre2 = createEntityCentre(MiTgPersistentEntityWithProperties2.class, "TgPersistentEntityWithProperties 2", createEntityCentreConfig(false, false, false, true));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre3 = createEntityCentre(MiTgPersistentEntityWithProperties3.class, "TgPersistentEntityWithProperties 3", createEntityCentreConfig(false, false, false, true));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre4 = createEntityCentre(MiTgPersistentEntityWithProperties4.class, "TgPersistentEntityWithProperties 4", createEntityCentreConfig(false, false, false, true));

        final UserWebUiConfig userWebUiConfig = new UserWebUiConfig(injector());
        final UserRoleWebUiConfig userRoleWebUiConfig = new UserRoleWebUiConfig(injector());

        configApp().addCentre(entityCentre);
        configApp().addCentre(entityCentreNotGenerated);
        configApp().addCentre(entityCentre1);
        configApp().addCentre(entityCentre2);
        configApp().addCentre(entityCentre3);
        configApp().addCentre(entityCentre4);
        configApp().addCentre(detailsCentre);
        configApp().addCentre(propDependencyCentre);
        configApp().addCentre(propDescriptorCentre);
        configApp().addCentre(userWebUiConfig.centre);
        configApp().addCentre(userRoleWebUiConfig.centre);

        //Add custom view
        final CustomTestView customView = new CustomTestView();
        configApp().addCustomView(customView);

        //        app.addCentre(new EntityCentre(MiTimesheet.class, "Timesheet"));
        // Add custom views.
        //        app.addCustomView(new MyProfile(), true);
        //        app.addCustomView(new CustomWebView(new CustomWebModel()));

        final String mr = "'margin-right: 20px', 'width:300px'";
        final String fmr = "'flex', 'margin-right: 20px'";
        final String actionMr = "'margin-top: 20px', 'margin-left: 20px', 'width: 110px'";
        // Add entity masters.
        final SimpleMasterBuilder<TgPersistentEntityWithProperties> smb = new SimpleMasterBuilder<TgPersistentEntityWithProperties>();
        @SuppressWarnings("unchecked")
        final IMaster<TgPersistentEntityWithProperties> masterConfig = smb.forEntity(TgPersistentEntityWithProperties.class)
                // PROPERTY EDITORS
                .addProp("entityProp.entityProp").asAutocompleter().withProps(pair("desc", true))
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("entityProp").asAutocompleter().withMatcher(ContextMatcher.class)
                .withProps(pair("desc", true),
                        pair("compositeProp", false),
                        pair("booleanProp", false))
                .withAction(
                        action(TgExportFunctionalEntity.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "self.setEditorValue4Property('requiredValidatedProp', functionalEntity, 'value');\n"
                                + "self.setEditorValue4Property('entityProp', functionalEntity, 'parentEntity');\n"
                                )) // self.retrieve()
                        .postActionError(new PostActionError(""))
                        .icon("trending-up")
                        .shortDesc("Export")
                        .longDesc("Export action")
                        .build())
                .also()
                .addProp("key").asSinglelineText()
                .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("bigDecimalProp").asDecimal()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("stringProp").asSinglelineText().skipValidation()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("stringProp").asMultilineText()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("dateProp").asDateTimePicker()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("booleanProp").asCheckbox()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("compositeProp").asAutocompleter()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("requiredValidatedProp").asSpinner()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())
                .also()
                .addProp("status").asAutocompleter()
                    .withAction(
                        action(TgCreatePersistentStatusAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "self.setEditorValue4Property('status', functionalEntity, 'status');\n"
                                )) // self.retrieve()
                        .postActionError(new PostActionError(""))
                        .icon("add-circle")
                        .shortDesc("Create Status")
                        .longDesc("Creates new status and assignes it back to the Status property")
                        .shortcut("ctrl+shift+l")
                        .build())
                .also()
                .addProp("colourProp").asColour()
                .also()

                .addAction(MasterActions.REFRESH)
                    .icon("highlight-off")
                    .shortDesc("CANCEL")
                    .longDesc("Cancels any changes and closes the master (if in dialog)")
                    // .shortcut("ctrl+x") // overridden from default esc

                // ENTITY CUSTOM ACTIONS
                .addAction(
                        action(TgExportFunctionalEntity.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "self.setEditorValue4Property('requiredValidatedProp', functionalEntity, 'value');\n"
                                + "self.setEditorValue4Property('entityProp', functionalEntity, 'parentEntity');\n"
                                )) // self.retrieve()
                        .postActionError(new PostActionError(""))
                        .icon("trending-up")
                        .shortDesc("Export")
                        .longDesc("Export action")
                        .shortcut("ctrl+shift+e")
                        .build())

                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                    // .shortDesc("SAVE")
                    // .longDesc("SAVE")
                    // .shortcut("ctrl+shift+s") // -- overridden from default ctrl+s
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', "
                        + format("[[%s], [%s], [%s], [%s], ['flex']],", fmr, fmr, fmr, fmr)
                        + format("[[%s], [%s], [%s], [%s], ['flex']],", fmr, fmr, fmr, fmr)
                        + format("['subheader:Other components', 'flex'],")
                        + format("[[%s], ['flex']],", fmr)
                        + format("['margin-top: 20px', 'wrap', [%s],[%s],[%s],[%s],[%s],[%s]]", actionMr, actionMr, actionMr, actionMr, actionMr, actionMr)
                        + "    ]"))
                .setLayoutFor(Device.TABLET, Optional.empty(), ("['padding:20px',"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[[fmr], ['flex'], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]")
                        .replace("fmr", fmr)
                        .replace("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, Optional.empty(), ("['padding:20px',"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]")
                        .replace("fmr", fmr)
                        .replace("actionMr", actionMr))
                .done();

        final IMaster<TgEntityForColourMaster> masterConfigForColour = new SimpleMasterBuilder<TgEntityForColourMaster>().forEntity(TgEntityForColourMaster.class)
                // PROPERTY EDITORS

                .addProp("colourProp").asColour()
                    .withAction(
                        action(TgDummyAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(new PostActionSuccess(""
                                + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                ))
                        .icon("accessibility")
                        .shortDesc("Dummy")
                        .longDesc("Dummy action, simply prints its result into console.")
                        .build())

                .also()
                .addProp("booleanProp").asCheckbox()
                .also()
                .addProp("stringProp").asSinglelineText().skipValidation()
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("REFRESH2")
                /*      */.longDesc("REFRESH2 action")

                // ENTITY CUSTOM ACTIONS
                .addAction(
                        action(TgExportFunctionalEntity.class)
                        .withContext(context().withMasterEntity().build())
                        .icon("trending-up")
                        .shortDesc("Export")
                        .longDesc("Export action")
                        .build())
                .addAction(MasterActions.VALIDATE).addAction(MasterActions.SAVE).addAction(MasterActions.EDIT).addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), ("['padding:20px', "
                        + "[[fmr], ['flex']],"
                        + "[['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr)).setLayoutFor(Device.TABLET, Optional.empty(), ("['padding:20px',"
                                + "[[fmr],['flex']],"
                                + "[['flex']],"
                                + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                                + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr)).setLayoutFor(Device.MOBILE, Optional.empty(), ("['padding:20px',"
                                        + "[['flex']],"
                                        + "[['flex']],"
                                        + "[['flex']],"
                                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                                        + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr)).done();

        final IMaster<TgEntityWithPropertyDependency> masterConfigForPropDependencyExample = new SimpleMasterBuilder<TgEntityWithPropertyDependency>()
            .forEntity(TgEntityWithPropertyDependency.class)
            .addProp("property").asSinglelineText()
            .also()
            .addProp("dependentProp").asSinglelineText()
            .also()
            .addProp("roles").asSinglelineText()
            .also()
            .addAction(MasterActions.REFRESH)
            //      */.icon("trending-up") SHORT-CUT
            /*      */.shortDesc("CANCEL")
            /*      */.longDesc("Cancel action")
            .addAction(MasterActions.VALIDATE)
            .addAction(MasterActions.SAVE)
            .addAction(MasterActions.EDIT)
            .addAction(MasterActions.VIEW)

            .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                    "      ['padding:20px', "
                    + format("[[%s], [%s], ['flex']],", fmr, fmr)
                    + format("['margin-top: 20px', 'wrap', [%s],[%s],[%s],[%s],[%s]]", actionMr, actionMr, actionMr, actionMr, actionMr)
                    + "    ]"))
            .done();

        final IMaster<TgCollectionalSerialisationParent> masterConfigForCollSerialisationTest = new SimpleMasterBuilder<TgCollectionalSerialisationParent>()
                .forEntity(TgCollectionalSerialisationParent.class)
                .addProp("key").asSinglelineText()
                .also()
                .addProp("desc").asSinglelineText()
                .also()
                .addProp("collProp").asCollectionalRepresentor()
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', "
                        + format("[[%s], [%s], ['flex']],", fmr, fmr)
                        + format("['margin-top: 20px', 'wrap', [%s],[%s],[%s],[%s],[%s]]", actionMr, actionMr, actionMr, actionMr, actionMr)
                        + "    ]"))
                .done();

        final IMaster<TgFunctionalEntityWithCentreContext> masterConfigForFunctionalEntity = new SimpleMasterBuilder<TgFunctionalEntityWithCentreContext>()
                .forEntity(TgFunctionalEntityWithCentreContext.class) // forEntityWithSaveOnActivate
                .addProp("valueToInsert").asSinglelineText()
                .also()
                .addProp("withBrackets").asCheckbox()
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), ("['vertical', 'justified', 'margin:20px', "
                        + "[[mr], [mr]], "
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.TABLET, Optional.empty(), ("['vertical', 'margin:20px',"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], [mr]],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, Optional.empty(), ("['margin:20px',"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("actionMr", actionMr))
                .done();

        final EntityMaster<TgPersistentEntityWithProperties> entityMaster = new EntityMaster<TgPersistentEntityWithProperties>(
                TgPersistentEntityWithProperties.class,
                TgPersistentEntityWithPropertiesProducer.class,
                masterConfig,
                injector());

        final EntityMaster<NewEntityAction> functionalMasterWithEmbeddedPersistentMaster =  NewEntityActionWebUiConfig.createMaster(injector(), entityMaster);
        final EntityMaster<EntityNewAction> entityNewActionMaster = StandardMastersWebUiConfig.createEntityNewMaster(injector());
        final EntityMaster<EntityEditAction> entityEditActionMaster = StandardMastersWebUiConfig.createEntityEditMaster(injector());
        final EntityMaster<EntityExportAction> entityExportActionMaster = StandardMastersWebUiConfig.createExportMaster(injector());
        final EntityMaster<EntityDeleteAction> entityDeleteActionMaster = EntityMaster.noUiFunctionalMaster(EntityDeleteAction.class, injector());

        final CentreConfigurationWebUiConfig centreConfigurationWebUiConfig = new CentreConfigurationWebUiConfig(injector());

        final EntityMaster<TgEntityForColourMaster> clourMaster = new EntityMaster<TgEntityForColourMaster>(TgEntityForColourMaster.class, masterConfigForColour, injector());

        configApp().
            addMaster(entityNewActionMaster).
            addMaster(entityEditActionMaster).
            addMaster(entityDeleteActionMaster).
            addMaster(entityExportActionMaster).
            addMaster(new EntityMaster<EntityWithInteger>(EntityWithInteger.class, null, injector())). // efs(EntityWithInteger.class).with("prop")
            addMaster(entityMaster).//
            addMaster(functionalMasterWithEmbeddedPersistentMaster).
            addMaster(createExportActionMaster()).
            addMaster(new EntityMaster<TgEntityWithPropertyDependency>(
                    TgEntityWithPropertyDependency.class,
                    TgEntityWithPropertyDependencyProducer.class,
                    masterConfigForPropDependencyExample,
                    injector())).
            addMaster(new EntityMaster<TgCollectionalSerialisationParent>(
                    TgCollectionalSerialisationParent.class,
                    TgCollectionalSerialisationParentProducer.class,
                    masterConfigForCollSerialisationTest,
                    injector())).
            addMaster(userWebUiConfig.master).
            addMaster(userWebUiConfig.rolesUpdater).
            addMaster(userRoleWebUiConfig.master).
            addMaster(userRoleWebUiConfig.tokensUpdater).
            // Centre configuration management
            addMaster(centreConfigurationWebUiConfig.centreConfigUpdater).
            addMaster(clourMaster).//

                addMaster(new EntityMaster<TgFunctionalEntityWithCentreContext>(
                        TgFunctionalEntityWithCentreContext.class,
                        TgFunctionalEntityWithCentreContextProducer.class,
                        masterConfigForFunctionalEntity,
                        injector())).
                addMaster(new EntityMaster<TgCentreInvokerWithCentreContext>(
                        TgCentreInvokerWithCentreContext.class,
                        new MasterWithCentreBuilder<TgCentreInvokerWithCentreContext>().forEntityWithSaveOnActivate(TgCentreInvokerWithCentreContext.class).withCentre(detailsCentre).done(),
                        injector())).
                addMaster(new EntityMaster<TgPersistentCompositeEntity>(
                        TgPersistentCompositeEntity.class,
                        null,
                        injector())).
                addMaster(EntityMaster.noUiFunctionalMaster(TgExportFunctionalEntity.class, TgExportFunctionalEntityProducer.class, injector())).
                addMaster(EntityMaster.noUiFunctionalMaster(TgDummyAction.class, injector())).
                addMaster(new EntityMaster<TgCreatePersistentStatusAction>(
                        TgCreatePersistentStatusAction.class,
                        TgCreatePersistentStatusActionProducer.class,
                        masterConfigForTgCreatePersistentStatusAction(), // TODO need to provide functional entity master configuration
                        injector())).
                addMaster(new EntityMaster<TgStatusActivationFunctionalEntity>(
                        TgStatusActivationFunctionalEntity.class,
                        null,
                        injector())).
                addMaster(new EntityMaster<TgISStatusActivationFunctionalEntity>(
                        TgISStatusActivationFunctionalEntity.class,
                        null,
                        injector())).
                addMaster(new EntityMaster<TgIRStatusActivationFunctionalEntity>(
                        TgIRStatusActivationFunctionalEntity.class,
                        null,
                        injector())).
                addMaster(new EntityMaster<TgONStatusActivationFunctionalEntity>(
                        TgONStatusActivationFunctionalEntity.class,
                        null,
                        injector())).
                addMaster(new EntityMaster<TgSRStatusActivationFunctionalEntity>(
                        TgSRStatusActivationFunctionalEntity.class,
                        null,
                        injector())).
                done();

        // here comes main menu configuration
        // it has two purposes -- one is to provide a high level navigation structure for the application,
        // another is to bind entity centre (and potentially other views) to respective menu items
        configMobileMainMenu()
                .addModule("Fleet Mobile")
                .description("Fleet Mobile")
                .icon("mobile-menu:fleet")
                .detailIcon("menu-detailed:fleet")
                .bgColor("#00D4AA")
                .captionBgColor("#00AA88")
                .master(entityMaster)
                //.centre(entityCentre)
                // .view(null)
                .done()

                .addModule("DDS Mobile")
                .description("DDS Mobile")
                .icon("mobile-menu:divisional-daily-management")
                .detailIcon("menu-detailed:divisional-daily-management")
                .bgColor("#00D4AA")
                .captionBgColor("#00AA88")
                //.master(entityMaster)
                .centre(entityCentre)
                //.view(null)
                .done();

        configDesktopMainMenu()
                .addModule("Fleet")
                .description("Fleet")
                .icon("menu:fleet")
                .detailIcon("menu-detailed:fleet")
                .bgColor("#00D4AA")
                .captionBgColor("#00AA88")
                .view(null)
                .done()
                .addModule("Import utilities")
                .description("Import utilities")
                .icon("menu:import-utilities")
                .detailIcon("menu-detailed:import-utilities")
                .bgColor("#5FBCD3")
                .captionBgColor("#2C89A0")
                .menu().addMenuItem("First view").description("First view description").view(null).done()
                /*  */.addMenuItem("Second view").description("Second view description").view(null).done()
                /*  */.addMenuItem("Entity Centre 1").description("Entity centre description").centre(entityCentre1).done()
                /*  */.addMenuItem("Entity Centre 2").description("Entity centre description").centre(entityCentre2).done()
                /*  */.addMenuItem("Entity Centre 3").description("Entity centre description").centre(entityCentre3).done()
                /*  */.addMenuItem("Entity Centre 4").description("Entity centre description").centre(entityCentre4).done()
                /*  */.addMenuItem("Collectional Serialisation Test").description("Collectional Serialisation Test description").centre(collectionalSerialisationTestCentre).done()
                /*  */.addMenuItem("Third view").description("Third view description").view(null).done().done()
                /*.menu()
                    .addMenuItem("Entity Centre").description("Entity centre description").centre(entityCentre).done()*/
                .done()
                .addModule("Division daily management")
                .description("Division daily management")
                .icon("menu:divisional-daily-management")
                .detailIcon("menu-detailed:divisional-daily-management")
                .bgColor("#CFD8DC")
                .captionBgColor("#78909C")
                .menu()
                .addMenuItem("Entity Centre").description("Entity centre description").centre(entityCentre).done()
                .addMenuItem("Not Generated Centre").description("Entity centre without calculated / custom properties, which type is strictly TgPersistentEntityWithProperties.class").centre(entityCentreNotGenerated).done()
                .addMenuItem("Custom View").description("Custom view description").view(customView).done()
                .addMenuItem("Deletion Centre").description("Deletion centre description").centre(deletionTestCentre).done()
                .addMenuItem("Property Dependency Example").description("Property Dependency Example description").centre(propDependencyCentre).done()
                .addMenuItem("Property Descriptor Example").description("Property Descriptor Example description").centre(propDescriptorCentre).done()
                .done().done()
                .addModule("Accidents")
                .description("Accidents")
                .icon("menu:accidents")
                .detailIcon("menu-detailed:accidents")
                .bgColor("#FF9943")
                .captionBgColor("#C87137")
                .view(null)
                .done()
                .addModule("Maintenance")
                .description("Maintenance")
                .icon("menu:maintenance")
                .detailIcon("menu-detailed:maintenance")
                .bgColor("#00AAD4")
                .captionBgColor("#0088AA")
                .view(null)
                .done()
                .addModule("User")
                /*  */.description("User")
                /*  */.icon("menu:user")
                /*  */.detailIcon("menu-detailed:user")
                /*  */.bgColor("#FFE680")
                /*  */.captionBgColor("#FFD42A")
                /*  */.menu()
                /*      */.addMenuItem("Users").description("User centre").centre(userWebUiConfig.centre).done()
                /*      */.addMenuItem("User Roles").description("User role centre").centre(userRoleWebUiConfig.centre).done()
                /*  */.done()
                /*  */.done()
                .addModule("Online reports")
                .description("Online reports")
                .icon("menu:online-reports")
                .detailIcon("menu-detailed:online-reports")
                .bgColor("#00D4AA")
                .captionBgColor("#00AA88").
                view(null)
                .done()
                .addModule("Fuel")
                .description("Fuel")
                .icon("menu:fuel")
                .detailIcon("menu-detailed:fuel")
                .bgColor("#FFE680")
                .captionBgColor("#FFD42A")
                .view(null)
                .done()
                .addModule("Organisational")
                .description("Organisational")
                .icon("menu:organisational")
                .detailIcon("menu-detailed:organisational")
                .bgColor("#2AD4F6")
                .captionBgColor("#00AAD4")
                .view(null)
                .done()
                .addModule("Preventive maintenance")
                .description("Preventive maintenance")
                .icon("menu:preventive-maintenance")
                .detailIcon("menu-detailed:preventive-maintenance")
                .bgColor("#F6899A")
                .captionBgColor("#D35F5F")
                .view(null)
                .done()
                .setLayoutFor(Device.DESKTOP, null, "[[[{rowspan: 2,colspan: 2}], [], [], [{colspan: 2}]],[[{rowspan: 2,colspan: 2}], [], []],[[], [], [{colspan: 2}]]]")
                .setLayoutFor(Device.TABLET, null, "[[[{rowspan: 2,colspan: 2}], [], []],[[{rowspan: 2,colspan: 2}]],[[], []],[[{rowspan: 2,colspan: 2}], [], []],[[{colspan: 2}]]]")
                .setLayoutFor(Device.MOBILE, null, "[[[], []],[[], []],[[], []],[[], []],[[], []]]").minCellWidth(100).minCellHeight(148).done();

    }

    private static IMaster<TgCreatePersistentStatusAction> masterConfigForTgCreatePersistentStatusAction() {
        final String layout = ""
                + "['vertical', 'padding:20px', "
                + "  ['vertical', "
                + "      ['width:300px', 'flex'], "
                + "      ['width:300px', 'flex']"
                + "  ],"
                + "  ['horizontal', 'margin-top: 20px', 'justify-content: center', 'wrap', ['margin: 10px', 'width: 110px', 'flex'], ['margin: 10px', 'width: 110px', 'flex']]"
                + "]";
        final IMaster<TgCreatePersistentStatusAction> config =
                new SimpleMasterBuilder<TgCreatePersistentStatusAction>().forEntity(TgCreatePersistentStatusAction.class)
                .addProp("statusCode").asSinglelineText()
                .also()
                .addProp("desc").asMultilineText()
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCLE").longDesc("Cancles the action")
                .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();
        return config;
    }

    public static class TgPersistentEntityWithProperties_UserParamAssigner implements IValueAssigner<SingleCritOtherValueMnemonic<User>, TgPersistentEntityWithProperties> {
        private final IUserProvider userProvider;

        @Inject
        public TgPersistentEntityWithProperties_UserParamAssigner(final IUserProvider userProvider) {
            this.userProvider = userProvider;
        }

        @Override
        public Optional<SingleCritOtherValueMnemonic<User>> getValue(final CentreContext<TgPersistentEntityWithProperties, ?> entity, final String name) {
            final SingleCritOtherValueMnemonic<User> mnemonic = single().entity(User.class)./* TODO not applicable on query generation level not().*/setValue(userProvider.getUser())./* TODO not applicable on query generation level canHaveNoValue(). */value();
            return Optional.of(mnemonic);
        }
    }

    public static class EntityPropValueMatcherForCentre extends AbstractSearchEntityByKeyWithCentreContext<TgPersistentEntityWithProperties> {
        @Inject
        public EntityPropValueMatcherForCentre(final ITgPersistentEntityWithProperties dao) {
            super(dao);
        }

        @Override
        protected EntityResultQueryModel<TgPersistentEntityWithProperties> completeEqlBasedOnContext(final CentreContext<TgPersistentEntityWithProperties, ?> context, final String searchString, final ICompoundCondition0<TgPersistentEntityWithProperties> incompleteEql) {
            System.out.println("EntityPropValueMatcherForCentre: CONTEXT == " + getContext());
            return incompleteEql.model();
        }
    }

    /**
     * Value matcher for PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptor property for TgEntityWithPropertyDescriptor entity centre's criterion.
     *
     * @author TG Team
     */
    public static class TgEntityWithPropertyDescriptorPropertyDescriptorMatcher extends AbstractSearchEntityByKeyWithCentreContext<PropertyDescriptor<TgPersistentEntityWithProperties>> {

        @Inject
        public TgEntityWithPropertyDescriptorPropertyDescriptorMatcher() {
            super(null);
        }

        @Override
        public List<PropertyDescriptor<TgPersistentEntityWithProperties>> findMatches(final String searchString) {
            // final Class<WorkOrder> enclosingEntityType = (Class<WorkOrder>) AnnotationReflector.getPropertyAnnotation(IsProperty.class, WoStatusRequiredProperty.class, "requiredProperty").value();
            final List<PropertyDescriptor<TgPersistentEntityWithProperties>> allPropertyDescriptors = Finder.getPropertyDescriptors(TgPersistentEntityWithProperties.class);
            final PojoValueMatcher<PropertyDescriptor<TgPersistentEntityWithProperties>> matcher = new PojoValueMatcher<>(allPropertyDescriptors, AbstractEntity.KEY, allPropertyDescriptors.size());
            final List<PropertyDescriptor<TgPersistentEntityWithProperties>> matchedPropertyDescriptors = matcher.findMatches(searchString);
            return matchedPropertyDescriptors;
        }

        @Override
        public List<PropertyDescriptor<TgPersistentEntityWithProperties>> findMatchesWithModel(final String searchString) {
            return findMatches(searchString);
        }

        @Override
        protected EntityResultQueryModel<PropertyDescriptor<TgPersistentEntityWithProperties>> completeEqlBasedOnContext(final CentreContext<PropertyDescriptor<TgPersistentEntityWithProperties>, ?> context, final String searchString, final ICompoundCondition0<PropertyDescriptor<TgPersistentEntityWithProperties>> incompleteEql) {
            throw new UnsupportedOperationException("EQL queries should not be used for property descriptors retrieval.");
        }
    }

    public static class KeyPropValueMatcherForCentre extends AbstractSearchEntityByKeyWithCentreContext<TgPersistentEntityWithProperties> {
        @Inject
        public KeyPropValueMatcherForCentre(final ITgPersistentEntityWithProperties dao) {
            super(dao);
        }

        @Override
        protected EntityResultQueryModel<TgPersistentEntityWithProperties> completeEqlBasedOnContext(final CentreContext<TgPersistentEntityWithProperties, ?> context, final String searchString, final ICompoundCondition0<TgPersistentEntityWithProperties> incompleteEql) {
            System.out.println("KeyPropValueMatcherForCentre: CONTEXT == " + getContext());
            return incompleteEql.model();
        }
    }

    public static class CritOnlySingleEntityPropValueMatcherForCentre extends AbstractSearchEntityByKeyWithCentreContext<TgPersistentEntityWithProperties> {
        @Inject
        public CritOnlySingleEntityPropValueMatcherForCentre(final ITgPersistentEntityWithProperties dao) {
            super(dao);
        }

        @Override
        protected EntityResultQueryModel<TgPersistentEntityWithProperties> completeEqlBasedOnContext(final CentreContext<TgPersistentEntityWithProperties, ?> context, final String searchString, final ICompoundCondition0<TgPersistentEntityWithProperties> incompleteEql) {
            System.out.println("CritOnlySingleEntityPropValueMatcherForCentre: CONTEXT == " + getContext());
            return incompleteEql.model();
        }
    }

    public static class CompositePropValueMatcherForCentre extends AbstractSearchEntityByKeyWithCentreContext<TgPersistentCompositeEntity> {
        @Inject
        public CompositePropValueMatcherForCentre(final ITgPersistentCompositeEntity dao) {
            super(dao);
        }

        @Override
        protected EntityResultQueryModel<TgPersistentCompositeEntity> completeEqlBasedOnContext(final CentreContext<TgPersistentCompositeEntity, ?> context, final String searchString, final ICompoundCondition0<TgPersistentCompositeEntity> incompleteEql) {
            System.out.println("CompositePropValueMatcherForCentre: CONTEXT == " + getContext());
            return incompleteEql.model();
        }
    }

    private static class PreAction implements IPreAction {
        private final String code;

        public PreAction(final String code) {
            this.code = code;
        }

        @Override
        public JsCode build() {
            return new JsCode(code);
        }
    }

    private static class PostActionSuccess implements IPostAction {
        private final String code;

        public PostActionSuccess(final String code) {
            this.code = code;
        }

        @Override
        public JsCode build() {
            return new JsCode(code);
        }
    }

    private static class PostActionError implements IPostAction {
        private final String code;

        public PostActionError(final String code) {
            this.code = code;
        }

        @Override
        public JsCode build() {
            return new JsCode(code);
        }
    }

    private static class CustomPropsAssignmentHandler implements ICustomPropsAssignmentHandler<AbstractEntity<?>> {
        @Override
        public void assignValues(final AbstractEntity<?> entity) {
            if ("DR".equals(((AbstractEntity<?>) entity.get("status")).getKey())) {
                entity.set("dR", "X");
            } else if ("IS".equals(((AbstractEntity<?>) entity.get("status")).getKey())) {
                entity.set("iS", "X");
            } else if ("IR".equals(((AbstractEntity<?>) entity.get("status")).getKey())) {
                entity.set("iR", "X");
            } else if ("ON".equals(((AbstractEntity<?>) entity.get("status")).getKey())) {
                entity.set("oN", "X");
            } else if ("SR".equals(((AbstractEntity<?>) entity.get("status")).getKey())) {
                entity.set("sR", "X");
            }
        }
    }

    private static class DetailsCentreQueryEnhancer implements IQueryEnhancer<TgPersistentEntityWithProperties> {
        private final EntityFactory entityFactory;
        private final IWebUiConfig webUiConfig;
        private final ICompanionObjectFinder companionFinder;
        private final IServerGlobalDomainTreeManager serverGdtm;
        private final IUserProvider userProvider;
        private final ICriteriaGenerator critGenerator;

        @Inject
        public DetailsCentreQueryEnhancer(final EntityFactory entityFactory, final IWebUiConfig webUiConfig, final ICompanionObjectFinder companionFinder, final IServerGlobalDomainTreeManager serverGdtm, final IUserProvider userProvider, final ICriteriaGenerator critGenerator) {
            this.entityFactory = entityFactory;
            this.webUiConfig = webUiConfig;
            this.companionFinder = companionFinder;
            this.serverGdtm = serverGdtm;
            this.userProvider = userProvider;
            this.critGenerator = critGenerator;
        }

        @Override
        public ICompleted<TgPersistentEntityWithProperties> enhanceQuery(final IWhere0<TgPersistentEntityWithProperties> where, final Optional<CentreContext<TgPersistentEntityWithProperties, ?>> context) {
            if (context.get().getMasterEntity() != null) {
                System.out.println("DetailsCentreQueryEnhancer: master entity holder == " + context.get().getMasterEntity());
                final TgCentreInvokerWithCentreContext funcEntity = (TgCentreInvokerWithCentreContext) context.get().getMasterEntity();
                System.out.println("DetailsCentreQueryEnhancer: restored masterEntity: " + funcEntity);
                System.out.println("DetailsCentreQueryEnhancer: restored masterEntity (centre context): " + funcEntity.getContext());
                System.out.println("DetailsCentreQueryEnhancer: restored masterEntity (centre context's selection criteria): " + funcEntity.getContext().getSelectionCrit().get("tgPersistentEntityWithProperties_critOnlyBigDecimalProp"));
                System.out.println("DetailsCentreQueryEnhancer: restored masterEntity (centre context's selection criteria): " + funcEntity.getContext().getSelectionCrit().get("tgPersistentEntityWithProperties_bigDecimalProp_from"));
            }
            return where.val(1).eq().val(1);
        }
    }

    private static class TgPersistentEntityWithPropertiesQueryEnhancer implements IQueryEnhancer<TgPersistentEntityWithProperties> {
        private final ITgPersistentStatus coStatus;
        private final ITgPersistentEntityWithProperties coEntity;

        @Inject
        public TgPersistentEntityWithPropertiesQueryEnhancer(final ITgPersistentStatus statusCo, final ITgPersistentEntityWithProperties coEntity) {
            this.coStatus = statusCo;
            this.coEntity = coEntity;
        }

        @Override
        public ICompleted<TgPersistentEntityWithProperties> enhanceQuery(final IWhere0<TgPersistentEntityWithProperties> where, final Optional<CentreContext<TgPersistentEntityWithProperties, ?>> context) {
            System.err.println("CONTEXT IN QUERY ENHANCER == " + context.get());
            if (!context.get().getSelectedEntities().isEmpty()) {
                final Long id = (Long) context.get().getSelectedEntities().get(0).get("id");
                final TgPersistentEntityWithProperties justUpdatedEntity = coEntity.findById(id, fetchOnly(TgPersistentEntityWithProperties.class).with("status"));
                return where.prop("status").eq().val(justUpdatedEntity.getStatus());
            }

            return where.prop("status").eq().val(coStatus.findByKey("IS"));
        }
    }

    private EntityCentreConfig<TgPersistentEntityWithProperties> createEntityCentreConfig(final boolean isComposite, final boolean runAutomatically, final boolean withQueryEnhancer, final boolean withCalculatedAndCustomProperties) {
        final String centreMr = "['margin-right: 40px', 'flex']";
        final String centreMrLast = "['flex']";

        final ICentreTopLevelActionsWithRunConfig<TgPersistentEntityWithProperties> partialCentre = EntityCentreBuilder.centreFor(TgPersistentEntityWithProperties.class);
        ICentreTopLevelActions<TgPersistentEntityWithProperties> actionConf = (runAutomatically ? partialCentre.runAutomatically() : partialCentre)
                .hasEventSourceAt("/entity-centre-events")
                .enforcePostSaveRefresh()
                .addTopAction(action(EntityNewAction.class).
                                withContext(context().withSelectionCrit().build()).
                                icon("add-circle-outline").
                                shortDesc("Add new").
                                longDesc("Start coninuous creatio of entities").
                                shortcut("alt+n").
                                build())
                .also()
                .addTopAction(action(EntityDeleteAction.class).
                        withContext(context().withSelectedEntities().build()).
                        postActionSuccess(new IPostAction() {

                            @Override
                            public JsCode build() {
                                return new JsCode("self.$.egi.clearPageSelection()");
                            }
                        }).
                        icon("remove-circle-outline").
                        shortDesc("Delete selected").
                        longDesc("Deletes the selected entities").
                        shortcut("alt+d").
                        build())
                .also()
                .addTopAction(CentreConfigActions.SORT_ACTION.mkAction())
                .also()
                .addTopAction(action(NewEntityAction.class).
                        withContext(context().withCurrentEntity().build()).// the current entity could potentially be used to demo "copy" functionality
                        icon("add-circle").
                        shortDesc("Add new").
                        longDesc("Start coninuous creatio of entities").
                        withNoParentCentreRefresh().
                        build())
                .also();


        if (isComposite) {
            actionConf = actionConf.addTopAction(
                    action(TgCentreInvokerWithCentreContext.class)
                            .withContext(context().withSelectionCrit().withSelectedEntities().build())
                            .icon("assignment-ind")
                            .shortDesc("Function 4")
                            .longDesc("Functional context-dependent action 4")
                            .prefDimForView(mkDim("document.body.clientWidth / 4", "400"))
                            .withNoParentCentreRefresh()
                            .build()
                    ).also();

        }

        @SuppressWarnings("unchecked")
        final IWithTooltip<TgPersistentEntityWithProperties> afterMinWidthConf = actionConf
                .addTopAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                preAction(yesNo("Are you sure you want to proceed?")).
                                icon("assignment-ind").
                                shortDesc("Function 1").
                                //longDesc("Functional context-dependent action 1").
                                prefDimForView(mkDim(300, 200)).
                                build()
                )
                .also()
                .addTopAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                icon("assignment-returned").
                                shortDesc("Function 2").
                                longDesc("Functional context-dependent action 2").
                                build()
                )
                .also()
                .addTopAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withCurrentEntity().build()).
                                icon("assignment").
                                shortDesc("Function 3").
                                longDesc("Functional context-dependent action 3").
                                build()
                )
                .also()
                .addTopAction(
                        action(ExportAction.class).
                                withContext(context().withSelectionCrit().withSelectedEntities().build())
                                .preAction(yesNo("Would you like to proceed with data export?"))
                                .postActionSuccess(new FileSaverPostAction())
                                .icon("icons:save")
                                .shortDesc("Export Data")
                                .build()
                )
                .also()
                .addTopAction(
                        action(EntityExportAction.class)
                                .withContext(context().withSelectionCrit().withSelectedEntities().build())
                                .preAction(new ExportPreAction())
                                .postActionSuccess(new FileSaverPostAction())
                                .icon("icons:save")
                                .shortDesc("Export Data")
                                .withNoParentCentreRefresh()
                                .build()
                )
                .addCrit("this").asMulti().autocompleter(TgPersistentEntityWithProperties.class)
                .withMatcher(KeyPropValueMatcherForCentre.class, context().withSelectedEntities()./*withMasterEntity().*/build())
                .withProps(pair("desc", true), pair("booleanProp", false), pair("compositeProp", true), pair("compositeProp.desc", true))
                //*    */.setDefaultValue(multi().string().not().setValues("A*", "B*").canHaveNoValue().value())
                .also()
                .addCrit("stringProp").asMulti().text()
                //*    */.setDefaultValue(multi().string().not().setValues("DE*", "ED*").canHaveNoValue().value())
                .also()
                .addCrit("integerProp").asRange().integer()
                //*    */.setDefaultValue(range().integer().not().setFromValueExclusive(1).setToValueExclusive(2).canHaveNoValue().value())
                .also()
                .addCrit("entityProp").asMulti().autocompleter(TgPersistentEntityWithProperties.class)
                .withMatcher(EntityPropValueMatcherForCentre.class, context().withSelectedEntities()./*withMasterEntity().*/build())
                .lightDesc()
                //*    */.setDefaultValue(multi().string().not().setValues("C*", "D*").canHaveNoValue().value())
                .also()
                .addCrit("bigDecimalProp").asRange().decimal()
                //*    */.setDefaultValue(range().decimal().not().setFromValueExclusive(new BigDecimal(3).setScale(5) /* TODO scale does not give appropriate effect on centres -- the prop becomes 'changed by other user' -- investigate generated crit property */).setToValueExclusive(new BigDecimal(4).setScale(5)).canHaveNoValue().value())
                .also()
                .addCrit("booleanProp").asMulti().bool()
                //*    */.setDefaultValue(multi().bool().not().setIsValue(false).setIsNotValue(false).canHaveNoValue().value())
                .also()
                .addCrit("dateProp").asRange().date()
                //    */.setDefaultValue(range().date().not().next()./* TODO not applicable on query generation level dayAndAfter().exclusiveFrom().exclusiveTo().*/canHaveNoValue().value())
                //*    */.setDefaultValue(range().date().not().setFromValueExclusive(new Date(1000000000L)).setToValueExclusive(new Date(2000000000L)).canHaveNoValue().value())
                .also()
                .addCrit("compositeProp").asMulti().autocompleter(TgPersistentCompositeEntity.class).withMatcher(CompositePropValueMatcherForCentre.class, context().withSelectedEntities()./*withMasterEntity().*/build())
                //*    */.setDefaultValue(multi().string().not().setValues("DEFAULT_KEY 10").canHaveNoValue().value())
                .also()
                .addCrit("critOnlyDateProp").asSingle().date()
                /*    */.setDefaultValue(single().date()./* TODO not applicable on query generation level not().*/setValue(new Date(1000000000L))./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("critOnlyEntityProp").asSingle().autocompleter(TgPersistentEntityWithProperties.class)
                .withMatcher(CritOnlySingleEntityPropValueMatcherForCentre.class, context().withSelectedEntities()./*withMasterEntity().*/build())
                .lightDesc()
                /*    */.setDefaultValue(single().entity(TgPersistentEntityWithProperties.class)./* TODO not applicable on query generation level not().*/setValue(injector().getInstance(ITgPersistentEntityWithProperties.class).findByKey("KEY8"))./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("userParam").asSingle().autocompleter(User.class)
                .withProps(pair("base", false), pair("basedOnUser", false))
                /*    */.withDefaultValueAssigner(TgPersistentEntityWithProperties_UserParamAssigner.class)
                .also()
                .addCrit("critOnlyIntegerProp").asSingle().integer()
                /*    */.setDefaultValue(single().integer()./* TODO not applicable on query generation level not(). */setValue(1)./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("critOnlyBigDecimalProp").asSingle().decimal()
                /*    */.setDefaultValue(single().decimal()./* TODO not applicable on query generation level not(). */setValue(new BigDecimal(3).setScale(5) /* TODO scale does not give appropriate effect on centres -- the prop becomes 'changed by other user' -- investigate generated crit property */)./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("critOnlyBooleanProp").asSingle().bool()
                /*    */.setDefaultValue(single().bool()./* TODO not applicable on query generation level not(). */setValue(false)./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("critOnlyStringProp").asSingle().text()
                /*    */.setDefaultValue(single().text()./* TODO not applicable on query generation level not(). */setValue("DE*")./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("status").asMulti().autocompleter(TgPersistentStatus.class)
                /*    */.setDefaultValue(multi().string().not().canHaveNoValue().value())
                .setLayoutFor(Device.DESKTOP, Optional.empty(),
                        //                        ("[['center-justified', 'start', mrLast]]")
                        ("[['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mrLast]]")
                                .replaceAll("mrLast", centreMrLast).replaceAll("mr", centreMr)
                )
                .setLayoutFor(Device.TABLET, Optional.empty(),
                        ("[['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]," +
                                "['center-justified', 'start', mr, mrLast]]")
                                .replaceAll("mrLast", centreMrLast).replaceAll("mr", centreMr)
                )
                .setLayoutFor(Device.MOBILE, Optional.empty(),
                        ("[['center-justified', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]," +
                                "['center-justified', 'start', mrLast]]")
                                .replaceAll("mrLast", centreMrLast).replaceAll("mr", centreMr)
                )
                //.hideCheckboxes()
                //.notScrollable()
                .withScrollingConfig(ScrollConfig.configScroll().withFixedCheckboxesPrimaryActionsAndFirstProps(1).withFixedSecondaryActions().withFixedHeader().withFixedSummary().done())
                .setPageCapacity(20)
                .setVisibleRowsCount(10)
                .addProp("this")
                    .order(2).asc()
                    .minWidth(60);

        final IWithSummary<TgPersistentEntityWithProperties> afterSummary;
        if (withCalculatedAndCustomProperties) {
            afterSummary = afterMinWidthConf.withSummary("kount", "COUNT(SELF)", "Count:Number of entities");
        } else {
            afterSummary = afterMinWidthConf;
        }

        IResultSetBuilder2Properties<TgPersistentEntityWithProperties> beforeAddProp = afterSummary.
                withAction(action(EntityEditAction.class).
                        withContext(context().withCurrentEntity().withSelectionCrit().build()).
                        icon("editor:mode-edit").
                        shortDesc("Edit entity").
                        longDesc("Opens master for editing this entity").
                        build())
                .also()
                .addProp("desc").
                        order(1).asc().
                        minWidth(200).
                        withAction(action(TgFunctionalEntityWithCentreContext.class).
                        withContext(context().withSelectedEntities().build()).
                        icon("assignment-turned-in").
                        shortDesc("Function 5").
                        longDesc("Functional context-dependent action 5").
                        build())

                .also();

                if (withCalculatedAndCustomProperties) {
                beforeAddProp = beforeAddProp.addProp(mkProp("DR", "Defect Radio", String.class)).width(26).
                        withAction(action(TgStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to DR").
                        longDesc("Change Status to DR").
                        build())
                .also()
                .addProp(mkProp("IS", "In Service", String.class)).width(26).
                        withAction(action(TgISStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to IS").
                        longDesc("Change Status to IS").
                        build())
                .also()
                .addProp(mkProp("IR", "In Repair", String.class)).width(26).
                        withAction(action(TgIRStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to IR").
                        longDesc("Change Status to IR").
                        build())
                .also()
                .addProp(mkProp("ON", "On Road Defect Station", String.class)).width(26).
                        withAction(action(TgONStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to ON").
                        longDesc("Change Status to ON").
                        build())
                .also()
                .addProp(mkProp("SR", "Defect Smash Repair", String.class)).width(26).
                        withAction(action(TgSRStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to SR").
                        longDesc("Change Status to SR").
                        build())
                        .also();
                }
                final IWithSummary<TgPersistentEntityWithProperties> beforeSummaryConf = beforeAddProp.addProp("integerProp")
                    .minWidth(42)
                    .withTooltip("desc");

                    final IWithTooltip<TgPersistentEntityWithProperties> beforeSummaryConfForBigDecimalProp = (withCalculatedAndCustomProperties ? beforeSummaryConf.withSummary("sum_of_int", "SUM(integerProp)", "Sum of int. prop:Sum of integer property") : beforeSummaryConf)
                .also()
                .addProp("bigDecimalProp")
                    .minWidth(68);

                    final IAlsoSecondaryAction<TgPersistentEntityWithProperties> beforeRenderingCustomiserConfiguration = (withCalculatedAndCustomProperties ?
                            beforeSummaryConfForBigDecimalProp
                                .withSummary("max_of_dec", "MAX(bigDecimalProp)", "Max of decimal:Maximum of big decimal property")
                                .withSummary("min_of_dec", "MIN(bigDecimalProp)", "Min of decimal:Minimum of big decimal property")
                                .withSummary("sum_of_dec", "sum(bigDecimalProp)", "Sum of decimal:Sum of big decimal property") :
                                beforeSummaryConfForBigDecimalProp)
                .also()
                .addProp("entityProp").minWidth(40)
                .also()
                .addProp("booleanProp").minWidth(49)
                .also()
                .addProp("dateProp").minWidth(130)
                .also()
                .addProp("compositeProp").minWidth(110)
                .also()
                .addProp("stringProp").minWidth(50)
                //                .setCollapsedCardLayoutFor(Device.DESKTOP, Optional.empty(),
                //                        "["
                //                                + "[['flex', 'select:property=this'],       ['flex', 'select:property=desc'],        ['flex', 'select:property=integerProp'], ['flex', 'select:property=bigDecimalProp']],"
                //                                + "[['flex', 'select:property=entityProp'], ['flex', 'select:property=booleanProp'], ['flex', 'select:property=dateProp'],    ['flex', 'select:property=compositeProp']]"
                //                                + "]")
                //                .withExpansionLayout(
                //                        "["
                //                                + "[['flex', 'select:property=stringProp']]"
                //                                + "]")
                .setCollapsedCardLayoutFor(Device.TABLET, Optional.empty(),
                        "["
                                + "[['flex', 'select:property=this'],           ['flex', 'select:property=desc'],       ['flex', 'select:property=integerProp']],"
                                + "[['flex', 'select:property=bigDecimalProp'], ['flex', 'select:property=entityProp'], ['flex', 'select:property=booleanProp']]"
                                + "]")
                .withExpansionLayout(
                        "["
                                + "[['flex', 'select:property=dateProp'],['flex', 'select:property=compositeProp']],"
                                + "[['flex', 'select:property=stringProp']]"
                                + "]")
                .setCollapsedCardLayoutFor(Device.MOBILE, Optional.empty(),
                        "["
                                + "[['flex', 'select:property=this'],        ['flex', 'select:property=desc']],"
                                + "[['flex', 'select:property=integerProp'], ['flex', 'select:property=bigDecimalProp']]"
                                + "]")
                .withExpansionLayout(
                        "["
                                + "[['flex', 'select:property=entityProp'], ['flex', 'select:property=booleanProp']],"
                                + "[['flex', 'select:property=dateProp'],   ['flex', 'select:property=compositeProp']],"
                                + "[['flex', 'select:property=stringProp']]"
                                + "]")
                //                .also()
                //                .addProp("status")

                //                .also()
                //                .addProp(mkProp("Custom Prop", "Custom property with String type", String.class))
                //                .also()
                //                .addProp(mkProp("Custom Prop 2", "Custom property 2 with concrete value", "OK2"))

                .addPrimaryAction(action(EntityEditAction.class).
                        withContext(context().withCurrentEntity().withSelectionCrit().build()).
                        icon("editor:mode-edit").
                        shortDesc("Edit entity").
                        longDesc("Opens master for editing this entity").
                        build())
                //                .addPrimaryAction(
                //                        EntityActionConfig.createMasterInvocationActionConfig()
                //EntityActionConfig.createMasterInDialogInvocationActionConfig()
                //                        action(TgFunctionalEntityWithCentreContext.class).
                //                                withContext(context().withSelectedEntities().build()).
                //                                icon("assignment-turned-in").
                //                                shortDesc("Function 2.5").
                //                                longDesc("Functional context-dependent action 2.5").
                //                                build()

                //) // EntityActionConfig.createMasterInvocationActionConfig() |||||||||||| actionOff().build()
                .also()
                /*.addSecondaryAction(
                        EntityActionConfig.createMasterInDialogInvocationActionConfig()
                ).also()*/
                .addSecondaryAction(
                        action(TgDummyAction.class)
                                .withContext(context().withSelectedEntities().build())
                                .postActionSuccess(new PostActionSuccess(""
                                        + "console.log('ACTION PERFORMED RECEIVING RESULT: ', functionalEntity);\n"
                                        ))
                                .icon("accessibility")
                                .shortDesc("Dummy")
                                .longDesc("Dummy action, simply prints its result into console.")
                                .build()
                )
                .also()
                .addSecondaryAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                icon("assignment-turned-in").
                                shortDesc("Function 3").
                                longDesc("Functional context-dependent action 3").
                                build()
                )
                .also()
                .addSecondaryAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                icon("attachment").
                                shortDesc("Function 4").
                                longDesc("Functional context-dependent action 4").
                                build()
                );
                final IQueryEnhancerSetter<TgPersistentEntityWithProperties> beforeEnhancerConfiguration = (withCalculatedAndCustomProperties ? beforeRenderingCustomiserConfiguration.setCustomPropsValueAssignmentHandler(CustomPropsAssignmentHandler.class) : beforeRenderingCustomiserConfiguration)
                .setRenderingCustomiser(TestRenderingCustomiser.class);

        final IExtraFetchProviderSetter<TgPersistentEntityWithProperties> afterQueryEnhancerConf;
        if (withQueryEnhancer) {
            afterQueryEnhancerConf = beforeEnhancerConfiguration.setQueryEnhancer(DetailsCentreQueryEnhancer.class, context().withMasterEntity().build());
        } else {
            afterQueryEnhancerConf = beforeEnhancerConfiguration;
        }
        // .setQueryEnhancer(TgPersistentEntityWithPropertiesQueryEnhancer.class, context().withCurrentEntity().build())

        final ISummaryCardLayout<TgPersistentEntityWithProperties> scl = afterQueryEnhancerConf.setFetchProvider(EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("status"))
                .setSummaryCardLayoutFor(Device.DESKTOP, Optional.empty(), "['width:350px', [['flex', 'select:property=kount'], ['flex', 'select:property=sum_of_int']],[['flex', 'select:property=max_of_dec'],['flex', 'select:property=min_of_dec']], [['flex', 'select:property=sum_of_dec']]]")
                .setSummaryCardLayoutFor(Device.TABLET, Optional.empty(), "['width:350px', [['flex', 'select:property=kount'], ['flex', 'select:property=sum_of_int']],[['flex', 'select:property=max_of_dec'],['flex', 'select:property=min_of_dec']], [['flex', 'select:property=sum_of_dec']]]")
                .setSummaryCardLayoutFor(Device.MOBILE, Optional.empty(), "['width:350px', [['flex', 'select:property=kount'], ['flex', 'select:property=sum_of_int']],[['flex', 'select:property=max_of_dec'],['flex', 'select:property=min_of_dec']], [['flex', 'select:property=sum_of_dec']]]");

        //                .also()
        //                .addProp("status").order(3).desc().withAction(null)
        //                .also()
        //                .addProp(mkProp("ON", "Defect ON road", "ON")).withAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build())
        //                .also()
        //                .addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(actionOff().build())
        //                .also()
        //                .addProp(mkProp("IS", "In service", "IS")).withAction(null)

//                if (isComposite) {
//                    return scl.addInsertionPoint(
//                            action(TgCentreInvokerWithCentreContext.class)
//                                .withContext(context().withSelectionCrit().withSelectedEntities().build())
//                                .icon("assignment-ind")
//                                .shortDesc("Insertion Point")
//                                .longDesc("Functional context-dependent Insertion Point")
//                                .prefDimForView(mkDim("document.body.clientWidth / 4", "400"))
//                                .withNoParentCentreRefresh()
//                                .build(),
//                            InsertionPoints.RIGHT)
//                            .build();
//                }
        return scl.build();
    }

    private EntityCentre<TgPersistentEntityWithProperties> createEntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final EntityCentreConfig<TgPersistentEntityWithProperties> entityCentreConfig) {
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre = new EntityCentre<>(miType, name, entityCentreConfig, injector(), (centre) -> {
            // ... please implement some additional hooks if necessary -- for e.g. centre.getFirstTick().setWidth(...), add calculated properties through domain tree API, etc.

            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "", 60);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "desc", 200);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "integerProp", 42);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "bigDecimalProp", 68);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "entityProp", 40);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "booleanProp", 49);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dateProp", 130);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "compositeProp", 110);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "stringProp", 50);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "status", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp2", 30);
            //            final int statusWidth = 26; // TODO does not matter below 18px -- still remain 18px, +20+20 as padding
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dR", statusWidth);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iS", statusWidth);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iR", statusWidth);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "oN", statusWidth);
            //            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "sR", statusWidth);

            return centre;
        });
        return entityCentre;
    }

    @Override
    public int getPort() {
        return port;
    }
}
