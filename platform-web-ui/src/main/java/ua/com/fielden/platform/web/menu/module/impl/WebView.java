package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

public class WebView implements IExecutable {

    private final IRenderable customView;
    private final EntityCentre<?> entityCentre;

    //    private EntityMaster<?> entityMaster;

    public WebView(final IRenderable customView) {
        this.entityCentre = null;
        this.customView = customView;
    }

    public WebView(final EntityCentre<?> entityCentre) {
        this.customView = null;
        this.entityCentre = entityCentre;
    }

    //    public WebView entityMaster(final EntityMaster<?> entityMaster) {
    //        this.entityMaster = entityMaster;
    //        return this;
    //    }

    @Override
    public JsCode code() {
        if (entityCentre == null && customView == null) {
            return new JsCode("null");
        } else {
            final String viewUrl = entityCentre != null ? "centre_ui" : "custom_view";
            final String typeUrl = entityCentre != null ? entityCentre.getMenuItemType().getName() : customView.getClass().getName();
            final String importUrl = "\"/" + viewUrl + "/" + typeUrl + "\"";
            final String typeName = entityCentre != null ? (entityCentre.getEntityType().getSimpleName() + "-centre") : (customView.getClass().getSimpleName() + "-view");
            final String elementName = "\"tg-" + typeName + "\"";
            final String viewType = entityCentre != null ? "\"centre\"" : "\"view\"";
            final String attrs;
            if (entityCentre != null) {
                attrs = "{}";
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
