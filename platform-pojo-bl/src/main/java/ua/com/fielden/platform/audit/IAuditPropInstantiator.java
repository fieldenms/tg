package ua.com.fielden.platform.audit;

/**
 * A contract to instantiate audit-prop entities.
 *
 * @param <AE>  the audit-entity type
 * @param <AP>  the audit-prop entity type
 */
public interface IAuditPropInstantiator<AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>> {

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
    AP newAuditProp(AE auditEntity, CharSequence property);

}
