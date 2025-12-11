package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Locates audit types.
 * <p>
 * {@code navigate} methods accept either an audited type or an audit type, and the resulting {@link Navigator} can be used
 * to "navigate" to some other type in the context.
 * For each audited type {@code E}, there is a context represented by the set of {@code E}'s audit types and {@code E} itself.
 * <p>
 * Cannot be used if auditing is disabled, all methods will throw {@link AuditingModeException}.
 */
public interface IAuditTypeFinder {

    /**
     * Navigates to the specified entity type, which must be audited.
     *
     * @see Navigator
     */
    <E extends AbstractEntity<?>> Navigator<E> navigate(Class<E> type);

    /**
     * Navigates to the specified persistent audit-entity type.
     *
     * @see Navigator
     */
    <E extends AbstractEntity<?>> Navigator<E> navigateAudit(Class<AbstractAuditEntity<E>> type);

    /**
     * Navigates to the specified persistent audit-prop type.
     *
     * @see Navigator
     */
    <E extends AbstractEntity<?>> Navigator<E> navigateAuditProp(Class<AbstractAuditProp<E>> type);

    /**
     * Navigates to the specified synthetic audit-entity type.
     *
     * @see Navigator
     */
    <E extends AbstractEntity<?>> Navigator<E> navigateSynAudit(Class<AbstractSynAuditEntity<E>> type);

    /**
     * Navigates to the specified synthetic audit-prop type.
     *
     * @see Navigator
     */
    <E extends AbstractEntity<?>> Navigator<E> navigateSynAuditProp(Class<AbstractSynAuditProp<E>> type);

    /**
     * Provides access to audited type {@code E} and its audit types.
     * Handling of exceptional situations depends on the {@linkplain AuditingMode auditing mode} in use.
     * In general, if the auditing mode is {@link AuditingMode#ENABLED}, all expected audit types must exist.
     * If the auditing mode is {@link AuditingMode#GENERATION}, all or some audit types may not exist.
     *
     * @param <E>  the audited type
     */
    interface Navigator<E extends AbstractEntity<?>> {

        /**
         * Returns the audited type.
         */
        Class<E> auditedType();

        /**
         * Returns the synthetic audit-entity type, which must exist.
         */
        Class<AbstractSynAuditEntity<E>> synAuditEntityType();

        /**
         * Returns the synthetic audit-entity type, which may not exist.
         */
        Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType();

        /**
         * Returns the synthetic audit-prop type, which must exist.
         */
        Class<AbstractSynAuditProp<E>> synAuditPropType();

        /**
         * Returns the synthetic audit-prop type, which may not exist.
         */
        Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropType();

        /**
         * Returns all persistent audit-entity types.
         * If the auditing mode is {@link AuditingMode#ENABLED}, the returned collection is never empty.
         * If the auditing mode is {@link AuditingMode#GENERATION}, the returned collection may be empty.
         */
        Collection<Class<AbstractAuditEntity<E>>> allAuditEntityTypes();

        /**
         * Returns the latest persistent audit-entity type (i.e., the one with the greatest version), which must exist.
         */
        Class<AbstractAuditEntity<E>> auditEntityType();

        /**
         * Returns the latest persistent audit-entity type (i.e., the one with the greatest version), which may not exist.
         */
        Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType();

        /**
         * Returns an audit-entity type with the specified version, which must exist.
         *
         * @see AuditUtils#getAuditTypeVersion(Class)
         */
        Class<AbstractAuditEntity<E>> auditEntityType(int version);

        /**
         * Returns an audit-entity type with the specified version, which may not exist.
         *
         * @see AuditUtils#getAuditTypeVersion(Class)
         */
        Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(int version);

        /**
         * If the auditing mode is {@link AuditingMode#ENABLED}, the returned collection is never empty.
         * If the auditing mode is {@link AuditingMode#GENERATION}, the returned collection may be empty.
         */
        Collection<Class<AbstractAuditProp<E>>> allAuditPropTypes();

        /**
         * Returns the latest persistent audit-prop type (i.e., the one with the greatest version), which must exist.
         */
        Class<AbstractAuditProp<E>> auditPropType();

        /**
         * Returns the latest persistent audit-prop type (i.e., the one with the greatest version), which may not exist.
         */
        Optional<Class<AbstractAuditProp<E>>> findAuditPropType();

        /**
         * Returns an audit-prop type with the specified version, which must exist.
         * 
         * @see AuditUtils#getAuditTypeVersion(Class)
         */
        Class<AbstractAuditProp<E>> auditPropType(int version);

        /**
         * Returns an audit-prop type with the specified version, which may not exist.
         *
         * @see AuditUtils#getAuditTypeVersion(Class)
         */
        Optional<Class<AbstractAuditProp<E>>> findAuditPropType(int version);

        /**
         * Returns all persistent audit types.
         * The result is equivalent to the union of {@link #allAuditEntityTypes()} and {@link #allAuditPropTypes()}.
         * For convenience, the result is sorted by audit type version ascending (e.g., `Entity_a3t_1`, `Entity_a3t_1_Prop`, `Entity_a3t_2`, ...).
         */
        List<Class<? extends AbstractEntity<?>>> allPersistentAuditTypes();

        /**
         * Returns all audit types.
         */
        Collection<Class<? extends AbstractEntity<?>>> allAuditTypes();

    }

}
