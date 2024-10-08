package ua.com.fielden.platform.entity.query.model;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

@ImplementedBy(FillModelApplierImpl.class)
@FunctionalInterface
public interface FillModelApplier {

    <E extends AbstractEntity<?>> E apply(FillModel model, E entity);

}
