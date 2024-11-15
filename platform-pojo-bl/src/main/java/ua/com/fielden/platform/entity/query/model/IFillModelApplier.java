package ua.com.fielden.platform.entity.query.model;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

@ImplementedBy(FillModelApplierImpl.class)
@FunctionalInterface
public interface IFillModelApplier {

    <E extends AbstractEntity<?>> E apply(IFillModel model, E entity);

}
