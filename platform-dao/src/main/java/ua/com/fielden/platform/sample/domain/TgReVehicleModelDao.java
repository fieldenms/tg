package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(TgReVehicleModel.class)
public class TgReVehicleModelDao extends CommonEntityDao<TgReVehicleModel> implements ITgReVehicleModel {

    @Inject
    protected TgReVehicleModelDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public Either<Long, TgReVehicleModel> save(final TgReVehicleModel entity, final Optional<fetch<TgReVehicleModel>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
