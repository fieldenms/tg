package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to instantiate audit-prop entities.
 * <p>
 * This interface belongs to internal API.
 * <p>
 * Cannot be used if auditing is disabled.
 *
 * @param <E>  the audited entity type
 */
interface IAuditPropInstantiator<E extends AbstractEntity<?>> {

    /**
     * Returns a new, initialised instance of this audit-prop entity type.
     *
     * @param auditEntity  the audit entity that will be used to initialise the audit-prop entity instance.
     *                     Must be persisted and non-dirty.
     * @param property  simple name of the property of the audit-entity, which represents a changed property of a corresponding audited entity.
     *                  <p>
     *                  For example, if the audited entity is {@code Timesheet} and the changed property is {@code startDate},
     *                  then the specified property must be the corresponding property of a {@code Timesheet}-audit entity.
     *                  <p>
     *                  It is an error if the specified property doesn't exist in the type of the specified audit entity.
     */
    AbstractAuditProp<E> newAuditProp(AbstractAuditEntity<E> auditEntity, CharSequence property);

    /**
     * An alternative version of {@link #newAuditProp(AbstractAuditEntity, CharSequence)} that gives up validation for speed.
     * Should be used judiciously.
     */
    AbstractAuditProp<E> fastNewAuditProp(AbstractAuditEntity<E> auditEntity, CharSequence property);

}
