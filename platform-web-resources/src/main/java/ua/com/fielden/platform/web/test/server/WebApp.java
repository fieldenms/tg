package ua.com.fielden.platform.web.test.server;

import org.restlet.Context;
import org.restlet.routing.Router;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesProducer;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.web.WebAppConfig;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.application.AbstractWebApp;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
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

import com.google.inject.Injector;

/**
 * Custom {@link AbstractWebApp} descendant for Web UI Testing Server. Provided in order to configure entity centres, masters and other client specific stuff.
 *
 * @author TG Team
 *
 */
public class WebApp extends AbstractWebApp {

    /**
     * Creates an instance of {@link WebApp} (for more information about the meaning of all this arguments see {@link AbstractWebApp#AbstractWebApp}
     *
     * @param context
     * @param injector
     * @param resourcePaths
     * @param name
     * @param desc
     * @param owner
     * @param author
     * @param username
     */
    public WebApp(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author) {
        super(context, injector, new String[0], name, desc, owner, author);
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

    /**
     * Configures the {@link WebAppConfig} with custom entity centres.
     */
    @Override
    protected void initWebApplication(final IWebApp webApp) {
        // Add entity centres.
        final EntityCentreConfig ecc = new EntityCentreConfig();
        //                new EntityCentreBuilder<TgPersistentEntityWithProperties>()
        //                .forEntity(TgPersistentEntityWithProperties.class)
        //                .addCrit("woType").asMulti().autocompleter().setDefaultValue(multi().string().not().setValues("AG*", "*RTU*D").value())
        //                .also()
        //                .addCrit("booleanFlag").asMulti().bool().setDefaultValue(multi().bool().setIsNotValue(true).canHaveNoValue().value())
        //                .also()
        //                .addCrit("intValue").asRange().integer().withDefaultValueAssigner(null)
        //                .also()
        //                .addCrit("date").asRange().date().setDefaultValue(range().date().prev().monthAndAfter().exclusiveFrom().canHaveNoValue().value())
        //                .also()
        //                .addCrit("intValueCritOnly").asSingle().integer().setDefaultValue(null) // TODO add default value example
        //                .also()
        //                .addCrit("entityCritOnly").asSingle().autocompleter(TgWorkOrder.class).withMatcher(null).setDefaultValue(null) // TODO add default value example
        //                .also()
        //                .addCrit("statusCritOnly").asSingle().autocompleter(TgWorkOrder.class).withMatcher(null)
        //                .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'margin:20px', "
        //                        + "[[mr], [mr], [mr], [mr], [mr]], "
        //                        + "[[mr], [mr], [mr], [mr], [mr]]]"))
        //                .setLayoutFor(Device.TABLET, null, ("['vertical', 'justified', 'margin:20px', "
        //                        + "[[mr], [mr], [mr], [mr], [mr]], "
        //                        + "[[mr], [mr], [mr], [mr], [mr]]]"))
        //                .setLayoutFor(Device.MOBILE, null, ("['vertical', 'justified', 'margin:20px', "
        //                        + "[[mr], [mr], [mr], [mr], [mr]], "
        //                        + "[[mr], [mr], [mr], [mr], [mr]]]"))
        //                .addProp("status").withAction(null)
        //                .also()
        //                .addProp("status").order(3).desc().withAction(null)
        //                .also()
        //                .addProp(mkProp("ON", "Defect ON road", "ON")).withAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build())
        //                .also()
        //                .addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(actionOff().build())
        //                .also()
        //                .addProp(mkProp("IS", "In service", "IS")).withAction(null)
        //                .build();

        webApp.configApp().addCentre(MiTgPersistentEntityWithProperties.class, new EntityCentre<TgPersistentEntityWithProperties>(MiTgPersistentEntityWithProperties.class, "TgPersistentEntityWithProperties", ecc, injector));
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
                .addProp("integerProp").asSpinner()
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

                .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'margin:20px', "
                        + "[[mr], [mr], [mr], [mr], [mr]], "
                        + "[[mr], [mr], [mr], [mr], [mr]],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.TABLET, null, ("['vertical', 'margin:20px',"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], [mr]],"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], [mr]],"
                        + "['horizontal', 'justified', ['flex', 'margin-right: 20px'], ['flex', 'margin-right: 20px'], [mr]],"
                        + "['horizontal', 'justified', ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("mr", mr).replaceAll("actionMr", actionMr))
                .setLayoutFor(Device.MOBILE, null, ("['margin:20px',"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['justified', ['flex', 'margin-right: 20px'], ['flex']],"
                        + "['margin-top: 20px', 'wrap', [actionMr],[actionMr],[actionMr],[actionMr],[actionMr],[actionMr]]"
                        + "]").replaceAll("actionMr", actionMr))
                .done();

        webApp.configApp().
                addMaster(EntityWithInteger.class, new EntityMaster<EntityWithInteger>(EntityWithInteger.class, null, injector)). // efs(EntityWithInteger.class).with("prop")
                addMaster(TgPersistentEntityWithProperties.class, new EntityMaster<TgPersistentEntityWithProperties>(TgPersistentEntityWithProperties.class, TgPersistentEntityWithPropertiesProducer.class, masterConfig, injector.getInstance(ICompanionObjectFinder.class), injector)).
                addMaster(TgPersistentCompositeEntity.class, new EntityMaster<TgPersistentCompositeEntity>(TgPersistentCompositeEntity.class, null, injector)).
                addMaster(TgExportFunctionalEntity.class, new EntityMaster<TgExportFunctionalEntity>(TgExportFunctionalEntity.class, null, injector)).done();
    }

    @Override
    protected void attachFunctionalEntities(final Router router, final Injector injector) {
        // router.attach("/users/{username}/CustomFunction", new FunctionalEntityResourceFactory<CustomFunction, ICustomFunction>(ICustomFunction.class, injector));
    }

}
