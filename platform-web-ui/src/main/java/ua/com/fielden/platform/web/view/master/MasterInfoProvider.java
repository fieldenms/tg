package ua.com.fielden.platform.web.view.master;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.view.master.exceptions.MissingEntityTypeException;

public class MasterInfoProvider {

    private final IWebUiConfig webUiConfig;

    public MasterInfoProvider(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    public MasterInfo getMasterInfo(final String className) {
        return getMasterInfo(getEntityType(className));
    }

    public MasterInfo getMasterInfo(final Class<? extends AbstractEntity<?>> type) {
        return buildConfiguredMasterActionInfo(type, "");
    }

    /**
     * Returns the entity type as class. If entityType can not be converted to a class then {@link MissingEntityTypeException} exception will be thrown.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<? extends AbstractEntity<?>> getEntityType(final String entityType) {
        try {
            return (Class<? extends AbstractEntity<?>>) Class.forName(entityType);
        } catch (final ClassNotFoundException e) {
            throw new MissingEntityTypeException(String.format("The entity type class is missing for type: %s", entityType), e);
        }
    }

    private MasterInfo buildConfiguredMasterActionInfo(final Class<? extends AbstractEntity<?>> type, final String relativePropertyName) {
        try {
            return webUiConfig.configApp().getOpenMasterAction(type).get().map(entityActionConfig -> {
                final FunctionalActionElement funcElem = new FunctionalActionElement(entityActionConfig, 0, FunctionalActionKind.PRIMARY_RESULT_SET);
                final DomElement actionElement = funcElem.render();
                final MasterInfo info = new MasterInfo();
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
                info.setEntityTypeTitle(getEntityTitleAndDesc(type).getKey());
                info.setRootEntityType(type);
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
            final String entityTitle = getEntityTitleAndDesc(type).getKey();
            final MasterInfo info = new MasterInfo();
            info.setKey("tg-EntityEditAction-master");
            info.setDesc("/master_ui/ua.com.fielden.platform.entity.EntityEditAction");
            info.setShortDesc(format("Edit %s", entityTitle));
            info.setLongDesc(format("Edit %s", entityTitle));
            info.setRequireSelectionCriteria("false");
            info.setRequireSelectedEntities("ONE");
            info.setRequireMasterEntity("false");
            info.setEntityType(EntityEditAction.class.getName());
            info.setRelativePropertyName(relativePropertyName);
            info.setEntityTypeTitle(entityTitle);
            info.setRootEntityType(type);
            return info;
        }).orElseGet(tryOtherMasters(type, relativePropertyName));
    }

    /**
     * Tries to determine a master for {@code type} in case it is a synthetic type based on some entity that may have a master (e.g. the case of ReWorkActivity extending WorkActivity).
     * If that fails, it tries to check if {@code type} is perhaps an entity with a single composite key member of an entity type that may have a master (e.g. the case of Manager with a single key member of type Person).
     * If none of the above yields anything, the constructed supplier returns {@code null}.
     *
     * @param type
     * @param relativePropertyName
     * @return
     */
    private Supplier<? extends MasterInfo> tryOtherMasters(final Class<? extends AbstractEntity<?>> type, final String relativePropertyName) {
         return () -> getBaseTypeForSyntheticEntity(type)
                     .map(baseType -> buildConfiguredMasterActionInfo(baseType, relativePropertyName))
                     .orElseGet(() -> getSingleMemberOfEntityType(type).map(keyTypeName -> buildConfiguredMasterActionInfo(keyTypeName._1, keyTypeName._2)).orElse(null));
    }

    /**
     * Returns a non-empty result if {@code type} has a composite key with a single, entity-typed member.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Optional<T2<Class<? extends AbstractEntity<?>>, String>> getSingleMemberOfEntityType(final Class<? extends AbstractEntity<?>> type) {
        final List<Field> keyMembers = Finder.getKeyMembers(type);
        if (keyMembers.size() == 1) {
            if (isCompositeEntity(type)) {
                return isEntityType(keyMembers.get(0).getType()) ? of(t2((Class<? extends AbstractEntity<?>>) keyMembers.get(0).getType(), keyMembers.get(0).getName())) : empty();
            }
            final Class<? extends Comparable<?>> keyType = getKeyType(type);
            return isEntityType(keyType) ? of(t2((Class<? extends AbstractEntity<?>>) keyType, KEY)) : empty();
        }
        return empty();
    }

    /**
     * Returns a non-empty result if {@code type} is a synthetic entity that is based on some persistent entity type.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends AbstractEntity<?>>> getBaseTypeForSyntheticEntity(final Class<? extends AbstractEntity<?>> type) {
        if (isSyntheticBasedOnPersistentEntityType(type)) {
            return of((Class<? extends AbstractEntity<?>>) type.getSuperclass());
        }
        return empty();
    }
}