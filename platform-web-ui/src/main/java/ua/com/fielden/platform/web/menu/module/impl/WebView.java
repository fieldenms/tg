package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class WebView implements IExecutable {

    private final IRenderable customView;
    private final EntityCentre<?> entityCentre;
    private final EntityMaster<?> entityMaster;

    public WebView(final IRenderable customView) {
        this(customView, null, null);
    }

    public WebView(final EntityCentre<?> entityCentre) {
        this(null, entityCentre, null);
    }

    public WebView(final EntityMaster<?> entityMaster) {
        this(null, null, entityMaster);
    }

    private WebView(final IRenderable customView, final EntityCentre<?> entityCentre, final EntityMaster<?> entityMaster) {
        this.customView = customView;
        this.entityCentre = entityCentre;
        this.entityMaster = entityMaster;
    }

    @Override
    public JsCode code() {
        if (entityMaster == null && entityCentre == null && customView == null) {
            return new JsCode("null");
        } else {
            final String viewUrl = entityMaster != null ? "master_ui" : (entityCentre != null ? "centre_ui" : "custom_view");
            final String typeUrl = entityMaster != null ? entityMaster.getEntityType().getName() : (entityCentre != null ? entityCentre.getMenuItemType().getName()
                    : customView.getClass().getName());
            final String importUrl = "\"/" + viewUrl + "/" + typeUrl + "\"";
            final String typeName = entityMaster != null ? (entityMaster.getEntityType().getSimpleName() + "-master")
                    : (entityCentre != null ? (entityCentre.getMenuItemType().getSimpleName() + "-centre") : (customView.getClass().getSimpleName() + "-view"));
            final String elementName = "\"tg-" + typeName + "\"";
            final String viewType = entityMaster != null ? "\"master\"" : (entityCentre != null ? "\"centre\"" : "\"view\"");
            String attrs;
            if (entityMaster != null) {
                // TODO the next piece of code was provided analogously to tg-mobile-app's hardcoded stuff.
                attrs = "{"
                        + "entityId: \"new\","
                        + "centreUuid: \"menu\","
                        + "currentState: \"EDIT\","
                        + "entityType: \"" + entityMaster.getEntityType().getName() + "\","
                        + "uuid: \"" + entityMaster.getEntityType().getSimpleName() + "\","
                        + "}";
            } else if (entityCentre != null) {
                if (entityCentre.isRunAutomatically()) {
                    attrs = "{autoRun: true, uuid: \"" + entityCentre.getName() + "\"}";
                } else {
                    attrs = "{uuid: \"" + entityCentre.getName() + "\"}";
                }
            } else {
                attrs = "null";
            }
            final String code = "{ import: " + importUrl + ", "
                    + "elementName: " + elementName + ", "
                    + "type: " + viewType + ", "
                    + "attrs: " + attrs + "}";
            return new JsCode(code);
        }
    }
}
