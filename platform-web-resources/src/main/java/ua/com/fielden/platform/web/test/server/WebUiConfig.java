package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.single;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.ITgPersistentStatus;
import ua.com.fielden.platform.sample.domain.MiDetailsCentre;
import ua.com.fielden.platform.sample.domain.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties1;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties2;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties3;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties4;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContextProducer;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMasterProducer;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContextProducer;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesProducer;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntityProducer;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntityProducer;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.app.AbstractWebUiConfig;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithRunConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.resources.webui.EntityResource;
import ua.com.fielden.platform.web.test.matchers.ContextMatcher;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;

/**
 * App-specific {@link IWebUiConfig} implementation.
 *
 * @author TG Team
 *
 */
public class WebUiConfig extends AbstractWebUiConfig {

    private final String domainName;
    private final String path;

    public WebUiConfig(final String domainName, final Workflows workflow, final String path) {
        super("TG Test and Demo Application", workflow, new String[0]);
        if (StringUtils.isEmpty(domainName) || StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Both the domain name and application binding path should be specified.");
        }
        this.domainName = domainName;
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

    /**
     * Configures the {@link WebUiConfig} with custom centres and masters.
     */
    @Override
    public void initConfiguration() {
        // Add entity centres.
        final EntityCentre<TgFetchProviderTestEntity> fetchProviderTestCentre = new EntityCentre<>(MiTgFetchProviderTestEntity.class, "TgFetchProviderTestEntity",
                EntityCentreBuilder.centreFor(TgFetchProviderTestEntity.class)
                        .addCrit("property").asMulti().autocompleter(TgPersistentEntityWithProperties.class).setDefaultValue(multi().string().setValues("KE*").value()).
                        setLayoutFor(Device.DESKTOP, Optional.empty(), "[[]]")

                        .addProp("property")
                        .setFetchProvider(EntityUtils.fetch(TgFetchProviderTestEntity.class).with("additionalProperty"))
                        // .addProp("additionalProp")
                        .build(), injector(), null);

        configApp().addCentre(MiTgFetchProviderTestEntity.class, fetchProviderTestCentre);

        final EntityCentre<TgPersistentEntityWithProperties> detailsCentre = createEntityCentre(MiDetailsCentre.class, "Details Centre", createEntityCentreConfig(false, true, true));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre = createEntityCentre(MiTgPersistentEntityWithProperties.class, "TgPersistentEntityWithProperties", createEntityCentreConfig(true, false, false));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre1 = createEntityCentre(MiTgPersistentEntityWithProperties1.class, "TgPersistentEntityWithProperties 1", createEntityCentreConfig(false, false, false));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre2 = createEntityCentre(MiTgPersistentEntityWithProperties2.class, "TgPersistentEntityWithProperties 2", createEntityCentreConfig(false, false, false));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre3 = createEntityCentre(MiTgPersistentEntityWithProperties3.class, "TgPersistentEntityWithProperties 3", createEntityCentreConfig(false, false, false));
        final EntityCentre<TgPersistentEntityWithProperties> entityCentre4 = createEntityCentre(MiTgPersistentEntityWithProperties4.class, "TgPersistentEntityWithProperties 4", createEntityCentreConfig(false, false, false));

        configApp().addCentre(MiTgPersistentEntityWithProperties.class, entityCentre);
        configApp().addCentre(MiTgPersistentEntityWithProperties1.class, entityCentre1);
        configApp().addCentre(MiTgPersistentEntityWithProperties2.class, entityCentre2);
        configApp().addCentre(MiTgPersistentEntityWithProperties3.class, entityCentre3);
        configApp().addCentre(MiTgPersistentEntityWithProperties4.class, entityCentre4);
        configApp().addCentre(MiDetailsCentre.class, detailsCentre);
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
                .addProp("entityProp").asAutocompleter().withMatcher(ContextMatcher.class)
                .withProps(pair("desc", true),
                        pair("compositeProp", false),
                        pair("booleanProp", false))
                /*      */.withAction("#exportEntityProp", TgExportFunctionalEntity.class)
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export entity prop")
                /*      */.longDesc("Export entity property")
                .also()
                .addProp("entityProp.entityProp").asAutocompleter().withProps(pair("desc", true))
                /*      */.withAction("#exportIntegerProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export integer prop")
                //      */.longDesc("Export integer property") SHORT-CUT
                .also()
                .addProp("key").asSinglelineText()
                /*      */.withAction("#exportCritOnlyEntityProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export crit only entity prop")
                /*      */.longDesc("Export crit only entity property")
                .also()
                .addProp("bigDecimalProp").asDecimal()
                /*      */.withAction("#exportBigDecimalProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export bigDecimal prop")
                /*      */.longDesc("Export bigDecimal property")
                .also()
                .addProp("stringProp").asSinglelineText().skipValidation()
                /*      */.withAction("#exportStringProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export string prop")
                /*      */.longDesc("Export string property")
                .also()
                .addProp("stringProp").asMultilineText()
                /*      */.withAction("#exportMultiStringProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export multi string prop")
                /*      */.longDesc("Export multi string property")
                .also()
                .addProp("dateProp").asDateTimePicker()
                /*      */.withAction("#exportDateProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export date prop")
                /*      */.longDesc("Export date property")
                .also()
                .addProp("booleanProp").asCheckbox()
                /*      */.withAction("#exportBooleanProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export boolean prop")
                /*      */.longDesc("Export boolean property")
                .also()
                .addProp("compositeProp").asAutocompleter()
                /*      */.withAction("#exportCompositeProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export composite prop")
                /*      */.longDesc("Export composite property")
                .also()
                .addProp("requiredValidatedProp").asSpinner()
                /*      */.withAction("#exportRequiredValidatedProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export requiredValidated prop")
                /*      */.longDesc("Export requiredValidated prop")
                .also()

                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("REFRESH2")
                /*      */.longDesc("REFRESH2 action")

                // ENTITY CUSTOM ACTIONS
//                .addAction("#export", TgExportFunctionalEntity.class)
//                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
//                /*      */.postActionSuccess(new PostActionSuccess(""))
//                /*      */.postActionError(new PostActionError(""))
//                /*      */.enabledWhen(EnabledState.EDIT)
//                //      */.icon("trending-up") SHORT-CUT
//                /*      */.shortDesc("Export")
//                /*      */.longDesc("Export action")
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
                        .build())

                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), ("['padding:20px', "
                        + "[[fmr], [fmr], [fmr], [fmr], ['flex']],"
                        + "[[fmr], [fmr], [fmr], [fmr], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.TABLET, Optional.empty(), ("['padding:20px',"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[[fmr], [fmr], ['flex']],"
                        + "[['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, Optional.empty(), ("['padding:20px',"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "[[fmr], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("fmr", fmr).replaceAll("actionMr", actionMr))
                .done();

        final IMaster<TgEntityForColourMaster> masterConfigForColour = new SimpleMasterBuilder<TgEntityForColourMaster>().forEntity(TgEntityForColourMaster.class)
                // PROPERTY EDITORS

                .addProp("colourProp").asColour()
                /*      */.withAction("#exportColourProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.shortDesc("Export colour prop")
                /*      */.longDesc("Export colour property").also()

                .addProp("booleanProp").asCheckbox()
                /*      */.withAction("#exportBooleanProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export boolean prop")
                /*      */.longDesc("Export boolean property").also()

                .addProp("stringProp").asSinglelineText().skipValidation()
                /*      */.withAction("#exportStringProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export string prop")
                /*      */.longDesc("Export string property").also()

                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("REFRESH2")
                /*      */.longDesc("REFRESH2 action")

                // ENTITY CUSTOM ACTIONS
                .addAction("#export", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.EDIT)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("Export")
                /*      */.longDesc("Export action")

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
        final EntityMaster<TgEntityForColourMaster> clourMaster = new EntityMaster<TgEntityForColourMaster>(TgEntityForColourMaster.class, TgEntityForColourMasterProducer.class, masterConfigForColour, injector());
        configApp().
            addMaster(EntityWithInteger.class, new EntityMaster<EntityWithInteger>(EntityWithInteger.class, null, injector())). // efs(EntityWithInteger.class).with("prop")
            addMaster(TgPersistentEntityWithProperties.class, entityMaster).//
            addMaster(TgEntityForColourMaster.class, clourMaster).//

                addMaster(EntityWithInteger.class, new EntityMaster<EntityWithInteger>(
                        EntityWithInteger.class,
                        null,
                        injector())). // efs(EntityWithInteger.class).with("prop")
                addMaster(TgPersistentEntityWithProperties.class, entityMaster).
                addMaster(TgFunctionalEntityWithCentreContext.class, new EntityMaster<TgFunctionalEntityWithCentreContext>(
                        TgFunctionalEntityWithCentreContext.class,
                        TgFunctionalEntityWithCentreContextProducer.class,
                        masterConfigForFunctionalEntity,
                        injector())).
                addMaster(TgCentreInvokerWithCentreContext.class, new EntityMaster<TgCentreInvokerWithCentreContext>(
                        TgCentreInvokerWithCentreContext.class,
                        TgCentreInvokerWithCentreContextProducer.class,
                        new MasterWithCentreBuilder<TgCentreInvokerWithCentreContext>().forEntityWithSaveOnActivate(TgCentreInvokerWithCentreContext.class).withCentre(detailsCentre).done(),
                        injector())).
                addMaster(TgPersistentCompositeEntity.class, new EntityMaster<TgPersistentCompositeEntity>(
                        TgPersistentCompositeEntity.class,
                        null,
                        injector())).
                addMaster(TgExportFunctionalEntity.class, new EntityMaster<TgExportFunctionalEntity>(
                        TgExportFunctionalEntity.class,
                        TgExportFunctionalEntityProducer.class,
                        null,
                        injector())).
                addMaster(TgStatusActivationFunctionalEntity.class, new EntityMaster<TgStatusActivationFunctionalEntity>(
                        TgStatusActivationFunctionalEntity.class,
                        TgStatusActivationFunctionalEntityProducer.class,
                        null,
                        injector())).
                addMaster(TgISStatusActivationFunctionalEntity.class, new EntityMaster<TgISStatusActivationFunctionalEntity>(
                        TgISStatusActivationFunctionalEntity.class,
                        TgISStatusActivationFunctionalEntityProducer.class,
                        null,
                        injector())).
                addMaster(TgIRStatusActivationFunctionalEntity.class, new EntityMaster<TgIRStatusActivationFunctionalEntity>(
                        TgIRStatusActivationFunctionalEntity.class,
                        TgIRStatusActivationFunctionalEntityProducer.class,
                        null,
                        injector())).
                addMaster(TgONStatusActivationFunctionalEntity.class, new EntityMaster<TgONStatusActivationFunctionalEntity>(
                        TgONStatusActivationFunctionalEntity.class,
                        TgONStatusActivationFunctionalEntityProducer.class,
                        null,
                        injector())).
                addMaster(TgSRStatusActivationFunctionalEntity.class, new EntityMaster<TgSRStatusActivationFunctionalEntity>(
                        TgSRStatusActivationFunctionalEntity.class,
                        TgSRStatusActivationFunctionalEntityProducer.class,
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
                .description("User")
                .icon("menu:user")
                .detailIcon("menu-detailed:user")
                .bgColor("#FFE680")
                .captionBgColor("#FFD42A")
                .view(null)
                .done()
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
            // entity.set("customProp", "OK");
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

    private EntityCentreConfig<TgPersistentEntityWithProperties> createEntityCentreConfig(final boolean isComposite, final boolean runAutomatically, final boolean withQueryEnhancer) {
        final String centreMr = "['margin-right: 40px', 'flex']";
        final String centreMrLast = "['flex']";

        final ICentreTopLevelActionsWithRunConfig<TgPersistentEntityWithProperties> partialCentre = EntityCentreBuilder.centreFor(TgPersistentEntityWithProperties.class);
        ICentreTopLevelActions<TgPersistentEntityWithProperties> actionConf = (runAutomatically ? partialCentre.runAutomatically() : partialCentre).hasEventSourceAt("/entity-centre-events");

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
        final IQueryEnhancerSetter<TgPersistentEntityWithProperties> beforeEnhancerConf = actionConf
                .addTopAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                preAction(new IPreAction() {
                                    @Override
                                    public JsCode build() {
                                        return new JsCode("    return confirm('Are you sure you want to proceed?');\n");
                                    }
                                }).
                                icon("assignment-ind").
                                shortDesc("Function 1").
                                longDesc("Functional context-dependent action 1").
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
                .addProp("this").withSummary("kount", "COUNT(SELF)", "Count:Number of entities").withAction(EntityActionConfig.createMasterInDialogInvocationActionConfig())
                .also()
                .addProp("desc").withAction(action(TgFunctionalEntityWithCentreContext.class).
                        withContext(context().withSelectedEntities().build()).
                        icon("assignment-turned-in").
                        shortDesc("Function 5").
                        longDesc("Functional context-dependent action 5").
                        build())

                .also()
                .addProp(mkProp("DR", "Defect Radio", String.class)).withAction(action(TgStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to DR").
                        longDesc("Change Status to DR").
                        build())
                .also()
                .addProp(mkProp("IS", "In Service", String.class)).withAction(action(TgISStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to IS").
                        longDesc("Change Status to IS").
                        build())
                .also()
                .addProp(mkProp("IR", "In Repair", String.class)).withAction(action(TgIRStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to IR").
                        longDesc("Change Status to IR").
                        build())
                .also()
                .addProp(mkProp("ON", "On Road Defect Station", String.class)).withAction(action(TgONStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to ON").
                        longDesc("Change Status to ON").
                        build())
                .also()
                .addProp(mkProp("SR", "Defect Smash Repair", String.class)).withAction(action(TgSRStatusActivationFunctionalEntity.class).
                        withContext(context().withCurrentEntity().build()).
                        icon("assignment-turned-in").
                        shortDesc("Change Status to SR").
                        longDesc("Change Status to SR").
                        build())

                .also()
                .addProp("integerProp").withTooltip("desc").withSummary("sum_of_int", "SUM(integerProp)", "Sum of int. prop:Sum of integer property")
                .also()
                .addProp("bigDecimalProp").withSummary("max_of_dec", "MAX(bigDecimalProp)", "Max of decimal:Maximum of big decimal property")
                .withSummary("min_of_dec", "MIN(bigDecimalProp)", "Min of decimal:Minimum of big decimal property")
                .withSummary("sum_of_dec", "sum(bigDecimalProp)", "Sum of decimal:Sum of big decimal property")
                .also()
                .addProp("entityProp")
                .also()
                .addProp("booleanProp")
                .also()
                .addProp("dateProp")
                .also()
                .addProp("compositeProp")
                .also()
                .addProp("stringProp")
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

                .addPrimaryAction(
                        //                        EntityActionConfig.createMasterInvocationActionConfig()
                        EntityActionConfig.createMasterInDialogInvocationActionConfig()
                //                        action(TgFunctionalEntityWithCentreContext.class).
                //                                withContext(context().withSelectedEntities().build()).
                //                                icon("assignment-turned-in").
                //                                shortDesc("Function 2.5").
                //                                longDesc("Functional context-dependent action 2.5").
                //                                build()

                ) // EntityActionConfig.createMasterInvocationActionConfig() |||||||||||| actionOff().build()
                .also()
                /*.addSecondaryAction(
                        EntityActionConfig.createMasterInDialogInvocationActionConfig()
                ).also()*/
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
                )
                .setCustomPropsValueAssignmentHandler(CustomPropsAssignmentHandler.class)
                .setRenderingCustomiser(TestRenderingCustomiser.class);

        final IExtraFetchProviderSetter<TgPersistentEntityWithProperties> afterQueryEnhancerConf;
        if (withQueryEnhancer) {
            afterQueryEnhancerConf = beforeEnhancerConf.setQueryEnhancer(DetailsCentreQueryEnhancer.class, context().withMasterEntity().build());
        } else {
            afterQueryEnhancerConf = beforeEnhancerConf;
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

            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "", 60);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "desc", 200);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "integerProp", 42);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "bigDecimalProp", 68);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "entityProp", 40);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "booleanProp", 49);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dateProp", 130);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "compositeProp", 110);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "stringProp", 50);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "status", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp2", 30);
            final int statusWidth = 26; // TODO does not matter below 18px -- still remain 18px, +20+20 as padding
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dR", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iS", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iR", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "oN", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "sR", statusWidth);

            return centre;
        });
        return entityCentre;
    }
}
