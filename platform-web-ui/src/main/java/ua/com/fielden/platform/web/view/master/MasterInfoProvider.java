package ua.com.fielden.platform.web.view.master;

import com.google.inject.Inject;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.master.IMasterInfoProvider;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;

public class MasterInfoProvider implements IMasterInfoProvider {

    private final IWebUiConfig webUiConfig;

    @Inject
    public MasterInfoProvider(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    @Override
    public MasterInfo getMasterInfo(final Class<? extends AbstractEntity<?>> type) {
        return webUiConfig.configApp().getOpenMasterAction(type).get().map(entityActionConfig -> {
            final FunctionalActionElement funcElem = new FunctionalActionElement(entityActionConfig, 0, FunctionalActionKind.PRIMARY_RESULT_SET);
            final DomElement actionElement = funcElem.render();
            final MasterInfo  info = new MasterInfo();
            info.setKey(actionElement.getAttr("element-name").toString());
            info.setDesc(actionElement.getAttr("component-uri").toString());
            info.setShouldRefreshParentCentreAfterSave(actionElement.getAttr("should-refresh-parent-centre-after-save") != null);
            info.setShortDescription(actionElement.getAttr("short-desc").toString());
            info.setLongDescription(actionElement.getAttr("long-desc").toString());
            info.setRequireSelectionCriteria(actionElement.getAttr("require-selection-criteria").toString());
            info.setRequireSelectedEntities(actionElement.getAttr("require-selected-entities").toString());
            info.setRequireMasterEntity(actionElement.getAttr("require-master-entity").toString());
            entityActionConfig.prefDimForView.ifPresent(prefDim -> {
                info.setWidth(prefDim.width);
                info.setHeight(prefDim.height);
                info.setWidthUnit(prefDim.widthUnit.value);
                info.setHeightUnit(prefDim.heightUnit.value);
            });
            return info;
        }).orElse(null);
    }

}
