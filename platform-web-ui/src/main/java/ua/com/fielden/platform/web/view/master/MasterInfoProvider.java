package ua.com.fielden.platform.web.view.master;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.annotation.RestrictCreationByUsers;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.exceptions.MissingEntityTypeException;

/**
 * Container for utilities to get {@link MasterInfo} from entity type or from its full name.
 * The presence of {@link MasterInfo} object (separate for <b>EDIT</b> and <b>NEW</b>) indicates that entity can be edited / created through master.
 * <p>
 * The type of master can be different from the type of entity.<br>
 * E.g. <i>Technician</i> entity can be opened in more generic <i>Person</i> master. Also, <i>ReWorkActivity</i> synthetic entity can be opened in more generic <i>WorkActivity</i> master.
 *
 * @author TG Team
 *
 */
public class MasterInfoProvider {
    private final IWebUiConfig webUiConfig;

    /**
     * Creates {@link MasterInfoProvider} using {@code webUiConfig}, which contains all registered masters and actions.
     */
    public MasterInfoProvider(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    /**
     * Returns {@link MasterInfo} object (for <b>EDIT</b>ing) that contains all the necessary information to open entity master.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param typeName -- entity type's full name
     */
    public MasterInfo getMasterInfo(final String typeName) {
        return getMasterInfo(getEntityType(typeName));
    }

    /**
     * Returns {@link MasterInfo} object (for <b>EDIT</b>ing) that contains all the necessary information to open entity master.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param type -- entity type
     */
    public MasterInfo getMasterInfo(final Class<? extends AbstractEntity<?>> type) {
        return buildConfiguredMasterActionInfo(webUiConfig.configApp(), type, "");
    }

    /**
     * Returns {@link MasterInfo} object (for creating <b>NEW</b>) that contains all the necessary information to open entity master.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param typeName -- entity type's full name
     */
    public MasterInfo getNewEntityMasterInfo(final String typeName) {
        return getNewEntityMasterInfo(getEntityType(typeName));
    }

    /**
     * Returns {@link MasterInfo} object (for creating <b>NEW</b>) that contains all the necessary information to open entity master.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param type -- entity type
     */
    public MasterInfo getNewEntityMasterInfo(final Class<? extends AbstractEntity<?>> type) {
        return buildConfiguredNewEntityMasterActionInfo(webUiConfig.configApp(), type);
    }

    /**
     * Returns the entity type as class. If {@code entityType} can not be converted to an entity type class then {@link MissingEntityTypeException} exception will be thrown.
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends AbstractEntity<?>> getEntityType(final String entityType) {
        try {
            return (Class<? extends AbstractEntity<?>>) forName(entityType);
        } catch (final ClassNotFoundException | ClassCastException ex) {
            throw new MissingEntityTypeException(format("The entity type class is missing for type: %s", entityType), ex);
        }
    }

    /**
     * Builds {@link MasterInfo} entity for {@code type} entity type.
     *
     * @param type -- entity type
     * @param openerType -- the type of wrapper functional entity to open master inside, e.g. <i>OpenWorkActivityMaster</i> for compound case or <i>EntityEdit/NewAction</i> otherwise
     * @param relativePropertyNameOpt -- optional relative property name (only for <b>EDIT</b>) i.e. the path in the entity type for which the master will be opened (e.g., <i>Technician.person</i> is of <i>Person</i> type and a single composite key member and no master is present for <i>Technician</i> then <i>Person</i> master will be used and <i>'person'</i> will be a relative property path)
     * @param prefDimOpt -- optional preferred dimensions for the opening master
     */
    private static MasterInfo buildMasterInfo(final Class<? extends AbstractEntity<?>> type, final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> openerType, final Optional<String> relativePropertyNameOpt, final Optional<PrefDim> prefDimOpt) {
        final String entityTitle = getEntityTitleAndDesc(type).getKey();
        final MasterInfo info = new MasterInfo();
        info.setKey(format("tg-%s-master", openerType.getSimpleName()));
        info.setDesc(format("/master_ui/%s", openerType.getName()));
        info.setShortDesc(entityTitle); // standard short description contains the title of the entity to open
        info.setLongDesc(format(relativePropertyNameOpt.isPresent() ? "Edit %s" : "Add new %s", entityTitle)); // standard long description contains 'Edit ...' / 'Add new ...'; relativePropertyNameOpt is present only for EDIT case
        info.setShouldRefreshParentCentreAfterSave(false); // opening of master should never be accompanied with parent centre(s) refreshing
        info.setEntityType(openerType.getName());
        info.setEntityTypeTitle(entityTitle);
        info.setRootEntityType(type);
        prefDimOpt.ifPresent(prefDim -> {
            info.setWidth(prefDim.width);
            info.setHeight(prefDim.height);
            info.setWidthUnit(prefDim.widthUnit.value);
            info.setHeightUnit(prefDim.heightUnit.value);
        });
        if (relativePropertyNameOpt.isPresent()) { // relativePropertyNameOpt is present only for EDIT case
            info.setRelativePropertyName(relativePropertyNameOpt.get()); // also require 'relative property name' only for EDIT case
        }
        return info;
    }

    /**
     * Builds {@link MasterInfo} for {@code type} (<b>EDIT</b>ing).
     * <p>
     * First, finds registered <i>OpenMaster</i> action and uses it.<br>
     * If there is no such action, finds registered master and uses it with <i>EntityEditAction</i> wrapper.<br>
     * If there is no such master, analyses the type for inheritance and keys and uses masters for such base types.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param webUiBuilder -- contains all registered masters and actions
     * @param type -- entity type
     * @param relativePropertyName -- relative property name i.e. the path in the entity type for which the master will be opened (e.g., <i>Technician.person</i> is of <i>Person</i> type and a single composite key member and no master is present for <i>Technician</i> then <i>Person</i> master will be used and <i>'person'</i> will be a relative property path)
     */
    private static MasterInfo buildConfiguredMasterActionInfo(final IWebUiBuilder webUiBuilder, final Class<? extends AbstractEntity<?>> type, final String relativePropertyName) {
        try {
            final EntityActionConfig config = webUiBuilder.getOpenMasterAction(type).get().get();
            return buildMasterInfo(type, config.functionalEntity.get(), of(relativePropertyName), config.prefDimForView);
        } catch (final WebUiBuilderException | NoSuchElementException ex) {
            if (webUiBuilder.getMaster(type).isPresent()) {
                return buildMasterInfo(type, EntityEditAction.class, of(relativePropertyName), empty());
            } else {
                // Tries to determine a master for {@code type} in case it is a synthetic type based on some entity that may have a master (e.g. the case of ReWorkActivity extending WorkActivity).
                // If that fails, it tries to check if {@code type} is perhaps an entity with a single composite key member of an entity type that may have a master (e.g. the case of Manager with a single key member of type Person).
                // If none of the above yields anything, the constructed supplier returns {@code null}.
                return getBaseTypeForSyntheticEntity(type)
                    .map(baseType -> buildConfiguredMasterActionInfo(webUiBuilder, baseType, relativePropertyName))
                    .orElseGet(() -> getSingleMemberOfEntityType(type)
                        .map(keyTypeName -> buildConfiguredMasterActionInfo(webUiBuilder, keyTypeName._1, keyTypeName._2))
                        .orElse(null));
            }
        }
    }

    /**
     * Builds {@link MasterInfo} for {@code type} (creating <b>NEW</b>).
     * <p>
     * First, finds registered <i>OpenMaster</i> action and uses it.<br>
     * If there is no such action, finds registered master and uses it with <i>EntityNewAction</i> wrapper.<br>
     * If there is no such master, analyses the type for inheritance and keys and uses masters for such base types.<br>
     * Returns {@code null} in case if there are no suitable entity master.
     *
     * @param webUiBuilder -- contains all registered masters and actions
     * @param type -- entity type
     */
    private static MasterInfo buildConfiguredNewEntityMasterActionInfo(final IWebUiBuilder webUiBuilder, final Class<? extends AbstractEntity<?>> type) {
        if (isAnnotationPresentForClass(RestrictCreationByUsers.class, type)) {
            return null;
        }
        try {
            final EntityActionConfig config = webUiBuilder.getOpenMasterAction(type).get().get();
            return buildMasterInfo(type, AbstractFunctionalEntityToOpenCompoundMaster.class.isAssignableFrom(config.functionalEntity.get()) ? config.functionalEntity.get() : EntityNewAction.class, empty(), config.prefDimForView);
        } catch (final WebUiBuilderException | NoSuchElementException ex) {
            if (webUiBuilder.getMaster(type).isPresent()) {
                return buildMasterInfo(type, EntityNewAction.class, empty(), empty());
            } else {
                // Tries to determine a master for {@code type} in case it is a synthetic type based on some entity that may have a master (e.g. the case of ReWorkActivity extending WorkActivity).
                // If that fails, it tries to check if {@code type} is perhaps an entity with a single composite key member of an entity type that may have a master (e.g. the case of Manager with a single key member of type Person).
                // If none of the above yields anything, the constructed supplier returns {@code null}.
                return getBaseTypeForSyntheticEntity(type)
                    .map(baseType -> buildConfiguredNewEntityMasterActionInfo(webUiBuilder, baseType))
                    .orElseGet(() -> getSingleMemberOfEntityType(type)
                        .map(keyTypeName -> buildConfiguredNewEntityMasterActionInfo(webUiBuilder, keyTypeName._1))
                        .orElse(null));
            }
        }
    }

    /**
     * Returns a non-empty result if {@code type} has a composite key with a single, entity-typed member.
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
     */
    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends AbstractEntity<?>>> getBaseTypeForSyntheticEntity(final Class<? extends AbstractEntity<?>> type) {
        if (isSyntheticBasedOnPersistentEntityType(type)) {
            return of((Class<? extends AbstractEntity<?>>) type.getSuperclass());
        }
        return empty();
    }

}