package ua.com.fielden.platform.audit;

/**
 * This interface defines the generator of audit-entity companion object types.
 */
public interface IAuditEntityCompanionGenerator {

    /**
     * Generates a companion object implementation for the specified audit-entity type.
     */
    Class<?> generateCompanion(Class<? extends AbstractAuditEntity> type);

    /**
     * Generates a companion object implementation for the specified audit-prop entity type.
     */
    Class<?> generateCompanionForAuditProp(Class<? extends AbstractAuditProp> type);

    /**
     * Generates a companion object implementation for the specified synthetic audit-entity type.
     * <p>
     * The generated type will implement {@link ISynAuditEntityDao}.
     */
    Class<?> generateCompanionForSynAuditEntity(Class<? extends AbstractSynAuditEntity> type);

    /**
     * Generates a companion object implementation for the specified synthetic audit-prop entity type.
     */
    Class<?> generateCompanionForSynAuditProp(Class<? extends AbstractSynAuditProp> type);

}
