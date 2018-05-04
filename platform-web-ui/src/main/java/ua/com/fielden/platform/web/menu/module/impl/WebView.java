package ua.com.fielden.platform.web.menu.module.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.menu.CustomView;
import ua.com.fielden.platform.menu.EntityCentreView;
import ua.com.fielden.platform.menu.EntityMasterView;
import ua.com.fielden.platform.menu.View;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class WebView implements IExecutable {

    private final AbstractCustomView customView;
    private final EntityCentre<?> entityCentre;
    private final EntityMaster<?> entityMaster;

    public WebView(final AbstractCustomView customView) {
        this(customView, null, null);
    }

    public WebView(final EntityCentre<?> entityCentre) {
        this(null, entityCentre, null);
    }

    public WebView(final EntityMaster<?> entityMaster) {
        this(null, null, entityMaster);
    }

    private WebView(final AbstractCustomView customView, final EntityCentre<?> entityCentre, final EntityMaster<?> entityMaster) {
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
                    : customView.getViewName());
            final String importUrl = "\"/" + viewUrl + "/" + typeUrl + "\"";
            final String typeName = entityMaster != null ? (entityMaster.getEntityType().getSimpleName() + "-master")
                    : (entityCentre != null ? (entityCentre.getMenuItemType().getSimpleName() + "-centre") : customView.getViewName());
            final String elementName = "\"tg-" + typeName + "\"";
            final String viewType = entityMaster != null ? "\"master\"" : (entityCentre != null ? "\"centre\"" : "\"view\"");
            final StringBuilder attrs = new StringBuilder();
            if (entityMaster != null) {
                // TODO the next piece of code was provided analogously to tg-mobile-app's hardcoded stuff.
                attrs
                .append("{")
                .append("entityId: \"new\",")
//                .append("centreUuid: \"menu\",")
                .append("currentState: \"EDIT\",")
                .append("entityType: \"" + entityMaster.getEntityType().getName() + "\",")
                .append("uuid: \"" + entityMaster.getEntityType().getSimpleName() + "\",")
                .append("}");
            } else if (entityCentre != null) {
            	attrs.append("{");
            	attrs.append(format("uuid: \"%s\",", entityCentre.getName()));
                if (entityCentre.isRunAutomatically()) {
                    attrs.append("autoRun: true,");
                }
                if (entityCentre.shouldEnforcePostSaveRefresh()) {
                    attrs.append("enforcePostSaveRefresh: true,");
                }
                if (entityCentre.eventSourceUri().isPresent()) {
                	attrs.append(format("uri: \"%s\",", entityCentre.eventSourceUri().get()));
                }
                attrs.append("}");
            } else {
                attrs.append("{}");
            }
            final String code = "{ import: " + importUrl + ", "
                    + "elementName: " + elementName + ", "
                    + "type: " + viewType + ", "
                    + "attrs: " + attrs.toString() + "}";
            return new JsCode(code);
        }
    }

    public View getView() {
        if (entityCentre == null && entityMaster == null && customView == null) {
            return null;
        } else {
            final String viewUrl = entityMaster != null ? "master_ui" : (entityCentre != null ? "centre_ui" : "custom_view");
            final String typeUrl = entityMaster != null ? entityMaster.getEntityType().getName() : (entityCentre != null ? entityCentre.getMenuItemType().getName()
                    : customView.getViewName());
            final String importUrl = "/" + viewUrl + "/" + typeUrl;
            final String typeName = entityMaster != null ? (entityMaster.getEntityType().getSimpleName() + "-master")
                    : (entityCentre != null ? (entityCentre.getMenuItemType().getSimpleName() + "-centre") : customView.getViewName());
            final String elementName = "tg-" + typeName + "";
            final String viewType = entityMaster != null ? "master" : (entityCentre != null ? "centre" : "view");

            final View view = new View();
            view.setKey("view");
            view.setHtmlImport(importUrl);
            view.setElementName(elementName);
            view.setViewType(viewType);
            if (entityMaster != null) {
                // TODO the next piece of code was provided analogously to tg-mobile-app's hardcoded stuff.
                view.setAttrs(new EntityMasterView().
                        setEntityId("new").
                        setCentreUuid("menu").
                        setCurrentState("EDIT").
                        setEntityType(entityMaster.getEntityType().getName()).
                        setUuid(entityMaster.getEntityType().getSimpleName()));
            } else if (entityCentre != null) {
                final EntityCentreView entityCentreView = new EntityCentreView();
                entityCentreView.setUuid(entityCentre.getName());
                if (entityCentre.isRunAutomatically()) {
                    entityCentreView.setAutoRun(true);
                }
                if (entityCentre.shouldEnforcePostSaveRefresh()) {
                    entityCentreView.setEnforcePostSaveRefresh(true);
                }
                if (entityCentre.eventSourceUri().isPresent()) {
                    entityCentreView.setUri(entityCentre.eventSourceUri().get());
                }
                view.setAttrs(entityCentreView);
            } else {
                view.setAttrs(new CustomView());
            }
            return view;
        }
    }
}
