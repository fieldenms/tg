package ua.com.fielden.platform.web.view.master;

import static java.lang.String.format;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;

public class MasterInfoProvider {

    private final IWebUiConfig webUiConfig;

    public MasterInfoProvider(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    public MasterInfo getMasterInfo(final Class<? extends AbstractEntity<?>> type) {
        try {
            return webUiConfig.configApp().getOpenMasterAction(type).get().map(entityActionConfig -> {
                final FunctionalActionElement funcElem = new FunctionalActionElement(entityActionConfig, 0, FunctionalActionKind.PRIMARY_RESULT_SET);
                final DomElement actionElement = funcElem.render();
                final MasterInfo  info = new MasterInfo();
                info.setKey(actionElement.getAttr("element-name").value.toString());
                info.setDesc(actionElement.getAttr("component-uri").value.toString());
                info.setShortDesc(actionElement.getAttr("short-desc").value.toString());
                info.setLongDesc(actionElement.getAttr("long-desc").value.toString());
                info.setShouldRefreshParentCentreAfterSave(actionElement.getAttr("should-refresh-parent-centre-after-save") != null);
                info.setRequireSelectionCriteria(actionElement.getAttr("require-selection-criteria").value.toString());
                info.setRequireSelectedEntities(actionElement.getAttr("require-selected-entities").value.toString());
                info.setRequireMasterEntity(actionElement.getAttr("require-master-entity").value.toString());
                info.setEntityType(entityActionConfig.functionalEntity.get().getName());
                entityActionConfig.prefDimForView.ifPresent(prefDim -> {
                    info.setWidth(prefDim.width);
                    info.setHeight(prefDim.height);
                    info.setWidthUnit(prefDim.widthUnit.value);
                    info.setHeightUnit(prefDim.heightUnit.value);
                });
                return info;
            }).orElse(buildDefaultMasterConfiguration(type));
        } catch (final WebUiBuilderException e) {
            return buildDefaultMasterConfiguration(type);
        }
    }

    private MasterInfo buildDefaultMasterConfiguration(final Class<? extends AbstractEntity<?>> type) {
        return webUiConfig.configApp().getMaster(type).map(master -> {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(type).getKey();
            final MasterInfo  info = new MasterInfo();
            info.setKey("tg-EntityEditAction-master");
            info.setDesc("/master_ui/ua.com.fielden.platform.entity.EntityEditAction");
            info.setShortDesc(format("Edit %s", entityTitle));
            info.setLongDesc(format("Edit %s", entityTitle));
            info.setRequireSelectionCriteria("false");
            info.setRequireSelectedEntities("ONE");
            info.setRequireMasterEntity("false");
            info.setEntityType(EntityEditAction.class.getName());
            return info;
        }).orElse(null);
    }

}
