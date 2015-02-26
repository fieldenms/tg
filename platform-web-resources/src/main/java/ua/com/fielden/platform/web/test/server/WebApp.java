package ua.com.fielden.platform.web.test.server;

import org.restlet.Context;
import org.restlet.routing.Router;

import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesProducer;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.web.WebAppConfig;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.application.AbstractWebApp;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterConfig;

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
            final String author,
            final String username) {
        super(context, injector, new String[0], name, desc, owner, author, username);
    }

    /**
     * Configures the {@link WebAppConfig} with custom entity centres.
     */
    @Override
    protected void initWebApplication(final IWebApp webApp) {
        // Add entity centres.
        //        app.addCentre(new EntityCentre(MiPerson.class, "Personnel"));
        //        app.addCentre(new EntityCentre(MiTimesheet.class, "Timesheet"));
        // Add custom views.
        //        app.addCustomView(new MyProfile(), true);
        //        app.addCustomView(new CustomWebView(new CustomWebModel()));

        // Add entity masters.
        final SimpleMasterConfig sm = new SimpleMasterConfig();
        final IRenderable masterRenderable = sm.forEntity(TgPersistentEntityWithProperties.class)
                // PROPERTY EDITORS
                .addProp("stringProp").asSinglelineText()
                .withAction("#validateDesc", TgPersistentEntityWithProperties.class)
                .preAction(new IPreAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionSuccess(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionError(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).enabledWhen(EnabledState.ANY).icon("trending-up")
                .also()

                .addProp("stringProp").asMultilineText()
                .also()
                .addProp("dateProp").asDateTimePicker().skipValidation()
                .also()
                .addProp("booleanProp").asCheckbox().skipValidation()
                .also()
                .addProp("bigDecimalProp").asDecimal().skipValidation()
                .also()
                .addProp("integerProp").asSpinner().skipValidation()
                .also()

                // ENTITY CUSTOM ACTIONS
                .addAction("#export", TgPersistentEntityWithProperties.class)
                .preAction(new IPreAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionSuccess(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionError(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).enabledWhen(EnabledState.VIEW).shortDesc("Export")
                .setLayoutFor(Device.DESKTOP, null, "[[]]")
                .setLayoutFor(Device.TABLET, null, "[[]]")
                .setLayoutFor(Device.TABLET, null, "[[]]")
                .done();
        webApp.configApp().
                addMaster(EntityWithInteger.class, new EntityMaster<EntityWithInteger>(EntityWithInteger.class, null)). // efs(EntityWithInteger.class).with("prop")
                addMaster(TgPersistentEntityWithProperties.class, new EntityMaster<TgPersistentEntityWithProperties>(TgPersistentEntityWithProperties.class, TgPersistentEntityWithPropertiesProducer.class, masterRenderable)).
                addMaster(TgPersistentCompositeEntity.class, new EntityMaster<TgPersistentCompositeEntity>(TgPersistentCompositeEntity.class, null)).
                addMaster(TgExportFunctionalEntity.class, new EntityMaster<TgExportFunctionalEntity>(TgExportFunctionalEntity.class, null)).done();
    }

    @Override
    protected void attachFunctionalEntities(final Router router, final Injector injector) {
        // router.attach("/users/{username}/CustomFunction", new FunctionalEntityResourceFactory<CustomFunction, ICustomFunction>(ICustomFunction.class, injector));
    }

}
