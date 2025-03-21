package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.List;

/**
 * Generator of synthetic models for audit types.
 */
@ImplementedBy(SynAuditModelGenerator.class)
public interface ISynAuditModelGenerator {

    /**
     * Generates a synthetic model for the specified synthetic audit type.
     *
     * @param synAuditType  a subclass of {@link AbstractSynAuditEntity} or a subclass of {@link AbstractSynAuditProp}
     */
    <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> generate(Class<E> synAuditType);

}
