package ua.com.fielden.platform.web.view.master;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
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
        return buildConfiguredMasterActionInfo(type, "");
    }

    private MasterInfo buildConfiguredMasterActionInfo(final Class<? extends AbstractEntity<?>> type, final String relativePropertyName) {
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
                info.setRelativePropertyName(relativePropertyName);
                entityActionConfig.prefDimForView.ifPresent(prefDim -> {
                    info.setWidth(prefDim.width);
                    info.setHeight(prefDim.height);
                    info.setWidthUnit(prefDim.widthUnit.value);
                    info.setHeightUnit(prefDim.heightUnit.value);
                });
                return info;
            }).orElse(buildDefaultMasterConfiguration(type, relativePropertyName));
        } catch (final WebUiBuilderException e) {
            return buildDefaultMasterConfiguration(type, relativePropertyName);
        }
    }

    private MasterInfo buildDefaultMasterConfiguration(final Class<? extends AbstractEntity<?>> type, final String relativePropertyName) {
        return webUiConfig.configApp().getMaster(type).map(master -> {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(type).getKey();
            final MasterInfo  info = new MasterInfo();
            info.setKey(format("tg-%s-master", type.getSimpleName()));
            info.setDesc(format("/master_ui/%s", type.getName()));
            info.setKey("tg-EntityEditAction-master");
            info.setDesc("/master_ui/ua.com.fielden.platform.entity.EntityEditAction");
            info.setShortDesc(format("Edit %s", entityTitle));
            info.setLongDesc(format("Edit %s", entityTitle));
            info.setRequireSelectionCriteria("false");
            info.setRequireSelectedEntities("ONE");
            info.setRequireMasterEntity("false");
            info.setEntityType(type.getName());
            info.setEntityType(EntityEditAction.class.getName());
            info.setRelativePropertyName(relativePropertyName);
            return info;
        })
                .orElseGet(() -> getBaseOfSyntheticType(type).map(baseType -> buildConfiguredMasterActionInfo(baseType, relativePropertyName))
                .orElseGet(() -> getSingleEntityKey(type).map(keyTypeName -> buildConfiguredMasterActionInfo(keyTypeName._1, keyTypeName._2)).orElse(null)));
    }

    @SuppressWarnings("unchecked")
    private Optional<T2<Class<? extends AbstractEntity<?>>, String>> getSingleEntityKey(final Class<? extends AbstractEntity<?>> type) {
        final List<Field> keyMembers = Finder.getKeyMembers(type);
        if (keyMembers.size() == 1) {
            if (isCompositeEntity(type)) {
                return isEntityType(keyMembers.get(0).getType()) ? of(t2((Class<? extends AbstractEntity<?>>)keyMembers.get(0).getType(), keyMembers.get(0).getName())) : empty();
            }
            return isEntityType(getKeyType(type)) ? of(t2((Class<? extends AbstractEntity<?>>)getKeyType(type), "key")) : empty();
        }
        return empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<Class<? extends AbstractEntity<?>>> getBaseOfSyntheticType(final Class<? extends AbstractEntity<?>> type) {
        if (EntityUtils.isSyntheticBasedOnPersistentEntityType(type)) {
            return of((Class<? extends AbstractEntity<?>>)type.getSuperclass());
        }
        return empty();
    }
}
