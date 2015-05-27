package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.single;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.ITgPersistentStatus;
import ua.com.fielden.platform.sample.domain.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
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
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.test.matchers.ContextMatcher;
import ua.com.fielden.platform.web.test.matchers.SearchAlsoByDescMatcher;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import com.google.inject.Inject;

/**
 * App-specific {@link IWebUiConfig} implementation.
 *
 * @author TG Team
 *
 */
public class WebUiConfig extends AbstractWebUiConfig {

    public WebUiConfig() {
        super("TG SERVER");
    }

    /**
     * Configures the {@link WebUiConfig} with custom centres and masters.
     */
    @Override
    public void initConfiguration() {

        // Add entity centres.

        final String centreMr = "['margin-right: 40px', 'flex']";
        final String centreMrLast = "['flex']";
        final EntityCentreConfig<TgPersistentEntityWithProperties> ecc = EntityCentreBuilder.centreFor(TgPersistentEntityWithProperties.class)
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
                .addCrit("this").asMulti().autocompleter(TgPersistentEntityWithProperties.class).withMatcher(KeyPropValueMatcherForCentre.class, context().withSelectionCrit().withSelectedEntities()./*withMasterEntity().*/build())
                //*    */.setDefaultValue(multi().string().not().setValues("A*", "B*").canHaveNoValue().value())
                .also()
                .addCrit("desc").asMulti().text()
                //*    */.setDefaultValue(multi().string().not().setValues("DE*", "ED*").canHaveNoValue().value())
                .also()
                .addCrit("integerProp").asRange().integer()
                //*    */.setDefaultValue(range().integer().not().setFromValueExclusive(1).setToValueExclusive(2).canHaveNoValue().value())
                .also()
                .addCrit("entityProp").asMulti().autocompleter(TgPersistentEntityWithProperties.class).withMatcher(EntityPropValueMatcherForCentre.class, context().withSelectedEntities()./*withMasterEntity().*/build())
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
                .addCrit("compositeProp").asMulti().autocompleter(TgPersistentCompositeEntity.class).withMatcher(CompositePropValueMatcherForCentre.class, context().withSelectionCrit().withSelectedEntities()./*withMasterEntity().*/build())
                //*    */.setDefaultValue(multi().string().not().setValues("DEFAULT_KEY 10").canHaveNoValue().value())
                .also()
                .addCrit("critOnlyDateProp").asSingle().date()
                /*    */.setDefaultValue(single().date()./* TODO not applicable on query generation level not().*/setValue(new Date(1000000000L))./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("critOnlyEntityProp").asSingle().autocompleter(TgPersistentEntityWithProperties.class).withMatcher(CritOnlySingleEntityPropValueMatcherForCentre.class, context().withSelectionCrit().withSelectedEntities()./*withMasterEntity().*/build())
                /*    */.setDefaultValue(single().entity(TgPersistentEntityWithProperties.class)./* TODO not applicable on query generation level not().*/setValue(injector().getInstance(ITgPersistentEntityWithProperties.class).findByKey("KEY8"))./* TODO not applicable on query generation level canHaveNoValue(). */value())
                .also()
                .addCrit("userParam").asSingle().autocompleter(User.class)
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

                .setLayoutFor(Device.DESKTOP, null,
                        ("[['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mr, mr, mrLast]," +
                                "['center-justified', 'start', mrLast]]")
                                .replaceAll("mrLast", centreMrLast).replaceAll("mr", centreMr)
                )
                .setLayoutFor(Device.TABLET, null,
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
                .setLayoutFor(Device.MOBILE, null,
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
                .addProp("this").withAction(EntityActionConfig.createMasterInvocationActionConfig())
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
                .addProp("integerProp")
                .also()
                .addProp("bigDecimalProp")
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
                //                .also()
                //                .addProp("status")

                //                .also()
                //                .addProp(mkProp("Custom Prop", "Custom property with String type", String.class))
                //                .also()
                //                .addProp(mkProp("Custom Prop 2", "Custom property 2 with concrete value", "OK2"))

                .addPrimaryAction(
                        action(TgFunctionalEntityWithCentreContext.class).
                                withContext(context().withSelectedEntities().build()).
                                icon("assignment-turned-in").
                                shortDesc("Function 2.5").
                                longDesc("Functional context-dependent action 2.5").
                                build()

                ) // EntityActionConfig.createMasterInvocationActionConfig() |||||||||||| actionOff().build()
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
                )
                .setCustomPropsValueAssignmentHandler(CustomPropsAssignmentHandler.class)
                .setRenderingCustomiser(TestRenderingCustomiser.class)
                .setQueryEnhancer(TgPersistentEntityWithPropertiesQueryEnhancer.class, context().withCurrentEntity().build())
                .setFetchProvider(EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("status"))

                //                .also()
                //                .addProp("status").order(3).desc().withAction(null)
                //                .also()
                //                .addProp(mkProp("ON", "Defect ON road", "ON")).withAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build())
                //                .also()
                //                .addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(actionOff().build())
                //                .also()
                //                .addProp(mkProp("IS", "In service", "IS")).withAction(null)
                .build();

        final EntityCentre<TgPersistentEntityWithProperties> entityCentre = new EntityCentre<>(MiTgPersistentEntityWithProperties.class, "TgPersistentEntityWithProperties", ecc, injector(), (centre) -> {
            // ... please implement some additional hooks if necessary -- for e.g. centre.getFirstTick().setWidth(...), add calculated properties through domain tree API, etc.

            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "", 60);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "desc", 200);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "integerProp", 30);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "bigDecimalProp", 30);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "entityProp", 40);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "booleanProp", 30);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dateProp", 130);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "compositeProp", 110);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "stringProp", 50);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "status", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp", 30);
            // centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "customProp2", 30);
            final int statusWidth = 0; // TODO does not matter below 18px -- still remain 18px, +20+20 as padding
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "dR", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iS", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "iR", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "oN", statusWidth);
            centre.getSecondTick().setWidth(TgPersistentEntityWithProperties.class, "sR", statusWidth);
            return centre;
        });

        final EntityCentre<TgFetchProviderTestEntity> fetchProviderTestCentre = new EntityCentre<>(MiTgFetchProviderTestEntity.class, "TgFetchProviderTestEntity",
                EntityCentreBuilder.centreFor(TgFetchProviderTestEntity.class)
                        .addCrit("property").asMulti().autocompleter(TgPersistentEntityWithProperties.class).setDefaultValue(multi().string().setValues("KE*").value()).
                        setLayoutFor(Device.DESKTOP, null, "[[]]")

                        .addProp("property")
                        .setFetchProvider(EntityUtils.fetch(TgFetchProviderTestEntity.class).with("additionalProperty"))
                        // .addProp("additionalProp")
                        .build()
                ,

                injector(), null);

        configApp().addCentre(MiTgFetchProviderTestEntity.class, fetchProviderTestCentre);
        configApp().addCentre(MiTgPersistentEntityWithProperties.class, entityCentre);
        //        app.addCentre(new EntityCentre(MiTimesheet.class, "Timesheet"));
        // Add custom views.
        //        app.addCustomView(new MyProfile(), true);
        //        app.addCustomView(new CustomWebView(new CustomWebModel()));

        final String mr = "'margin-right: 20px', 'width:300px'";
        final String actionMr = "'margin-top: 20px', 'margin-left: 20px', 'width: 110px'";
        // Add entity masters.
        final ISimpleMasterConfig<TgPersistentEntityWithProperties> masterConfig = new SimpleMasterBuilder<TgPersistentEntityWithProperties>().forEntity(TgPersistentEntityWithProperties.class)
                // PROPERTY EDITORS
                .addProp("entityProp").asAutocompleter().withMatcher(ContextMatcher.class)
                /*      */.withAction("#exportEntityProp", TgExportFunctionalEntity.class)
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export entity prop")
                /*      */.longDesc("Export entity property")
                .also()
                .addProp("entityProp.entityProp").asAutocompleter()
                /*      */.withAction("#exportIntegerProp", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.VIEW)
                /*      */.icon("trending-up")
                /*      */.shortDesc("Export integer prop")
                //      */.longDesc("Export integer property") SHORT-CUT
                .also()
                .addProp("critOnlyEntityProp").asAutocompleter().withMatcher(SearchAlsoByDescMatcher.class).byDesc()
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
                .addAction("#export", TgExportFunctionalEntity.class)
                /*      */.preAction(new PreAction("functionalEntity.parentEntity = { val: masterEntity.get('key'), origVal: null };"))
                /*      */.postActionSuccess(new PostActionSuccess(""))
                /*      */.postActionError(new PostActionError(""))
                /*      */.enabledWhen(EnabledState.EDIT)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("Export")
                /*      */.longDesc("Export action")

                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'padding:20px', "
                        + "[[mr], [mr], [mr], [mr], [mr]], "
                        + "[[mr], [mr], [mr], [mr], [mr]],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.TABLET, null, ("['vertical', 'padding:20px',"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px']],"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px']],"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px']],"
                        + "['horizontal', 'justified', ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, null, ("['padding:20px',"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("actionMr", actionMr))
                .done();

        final ISimpleMasterConfig<TgFunctionalEntityWithCentreContext> masterConfigForFunctionalEntity = new SimpleMasterBuilder<TgFunctionalEntityWithCentreContext>().forEntity(TgFunctionalEntityWithCentreContext.class)
                .addProp("valueToInsert").asSinglelineText()
                .also()
                .addProp("withBrackets").asCheckbox()
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("REFRESH2")
                /*      */.longDesc("REFRESH2 action")

                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'margin:20px', "
                        + "[[mr], [mr]], "
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.TABLET, null, ("['vertical', 'margin:20px',"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], [mr]],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, null, ("['margin:20px',"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("actionMr", actionMr))
                .done();

        configApp().
                addMaster(EntityWithInteger.class, new EntityMaster<EntityWithInteger>(
                        EntityWithInteger.class,
                        null,
                        injector())). // efs(EntityWithInteger.class).with("prop")
                addMaster(TgPersistentEntityWithProperties.class, new EntityMaster<TgPersistentEntityWithProperties>(
                        TgPersistentEntityWithProperties.class,
                        TgPersistentEntityWithPropertiesProducer.class,
                        masterConfig,
                        injector())).
                addMaster(TgFunctionalEntityWithCentreContext.class, new EntityMaster<TgFunctionalEntityWithCentreContext>(
                        TgFunctionalEntityWithCentreContext.class,
                        TgFunctionalEntityWithCentreContextProducer.class,
                        masterConfigForFunctionalEntity,
                        injector())).
                addMaster(TgPersistentCompositeEntity.class, new EntityMaster<TgPersistentCompositeEntity>(
                        TgPersistentCompositeEntity.class,
                        null,
                        injector())).
                addMaster(TgExportFunctionalEntity.class, new EntityMaster<TgExportFunctionalEntity>(
                        TgExportFunctionalEntity.class,
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
        configMainMenu().
                addModule("Fleet").
                description("Fleet").
                icon("/resources/images/fleet.svg").
                detailIcon("/resources/images/detailed/fleet.svg").
                bgColor("#00D4AA").
                captionBgColor("#00AA88").
                view(null).
                done().
                addModule("Import utilities").
                description("Import utilities").
                icon("/resources/images/importUtilities.svg").
                detailIcon("/resources/images/detailed/importUtilities.svg").
                bgColor("#5FBCD3").
                captionBgColor("#2C89A0").
                menu().addMenuItem("Entity Centre").description("Entity centre description").centre(entityCentre).done().done().done().
                addModule("Division daily management").
                description("Division daily management").
                icon("/resources/images/divisionalDailyManagment.svg").
                detailIcon("/resources/images/detailed/divisionalDailyManagment.svg").
                bgColor("#CFD8DC").
                captionBgColor("#78909C").
                menu().addMenuItem("Entity Centre").description("Entity centre description").centre(entityCentre).done().done().done().
                addModule("Accidents").
                description("Accidents").
                icon("/resources/images/accidents.svg").
                detailIcon("/resources/images/detailed/accidents.svg").
                bgColor("#FF9943").
                captionBgColor("#C87137").
                view(null).done().
                addModule("Maintenance").
                description("Maintenance").
                icon("/resources/images/maintanance.svg").
                detailIcon("/resources/images/detailed/maintanance.svg").
                bgColor("#00AAD4").
                captionBgColor("#0088AA").
                view(null).done().
                addModule("User").
                description("User").
                icon("/resources/images/user.svg").
                detailIcon("/resources/images/detailed/user.svg").
                bgColor("#FFE680").
                captionBgColor("#FFD42A").
                view(null).done().
                addModule("Online reports").
                description("Online reports").
                icon("/resources/images/onlineReports.svg").
                detailIcon("/resources/images/detailed/onlineReports.svg").
                bgColor("#00D4AA").
                captionBgColor("#00AA88").
                view(null).done().
                addModule("Fuel").
                description("Fuel").
                icon("/resources/images/fuel.svg").
                detailIcon("/resources/images/detailed/fuel.svg").
                bgColor("#FFE680").
                captionBgColor("#FFD42A").
                view(null).done().
                addModule("Organisational").
                description("Organisational").
                icon("/resources/images/organisational.svg").
                detailIcon("/resources/images/detailed/organisational.svg").
                bgColor("#2AD4F6").
                captionBgColor("#00AAD4").
                view(null).done().
                addModule("Preventive maintenance").
                description("Preventive maintenance").
                icon("/resources/images/preventiveMaintenence.svg").
                detailIcon("/resources/images/detailed/preventiveMaintenence.svg").
                bgColor("#F6899A").
                captionBgColor("#D35F5F").
                view(null).done().
                setLayoutFor(Device.DESKTOP, null, "[[[{rowspan: 2,colspan: 2}], [], [], [{colspan: 2}]],[[{rowspan: 2,colspan: 2}], [], []],[[], [], [{colspan: 2}]]]").
                setLayoutFor(Device.TABLET, null, "[[[{rowspan: 2,colspan: 2}], [], []],[[{rowspan: 2,colspan: 2}]],[[], []],[[{rowspan: 2,colspan: 2}], [], []],[[{colspan: 2}]]]").
                setLayoutFor(Device.MOBILE, null, "[[[], []],[[], []],[[], []],[[], []],[[], []]]").minCellWidth(100).minCellHeight(148).done();

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
            System.err.println("CONTEXT IN QUERY ENHANCER == " + context.get().getSelectedEntities());

            if (!context.get().getSelectedEntities().isEmpty()) {
                final Long id = (Long) context.get().getSelectedEntities().get(0).get("id");
                final TgPersistentEntityWithProperties justUpdatedEntity = coEntity.findById(id, fetchOnly(TgPersistentEntityWithProperties.class).with("status"));
                return where.prop("status").eq().val(justUpdatedEntity.getStatus());
            }

            return where.prop("status").eq().val(coStatus.findByKey("IS"));
        }

    }
}
