package ua.com.fielden.platform.entity.validation;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;

/**
 * A validator contract that is used for restricting building a reference hierarchy based.
 * This validator is assigned to properties {@code refEntityType} and {@code entityType} of entity {@link ReferenceHierarchy}
 * <p>
 * The default implementation {@link CannotBuildReferenceHierarchyForEntityValidator} is restrictive, prohibiting building a reference hierarchy for any entity type.
 * A less restrictive implementation should be provided and bound in an IoC module at the level of specific applications.
 *
 * @author TG Team
 *
 */
@ImplementedBy(CannotBuildReferenceHierarchyForEntityValidator.class)
public interface ICanBuildReferenceHierarchyForEntityValidator extends IBeforeChangeEventHandler<String> {
}

