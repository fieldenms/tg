package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(TgReMaxVehicleReading.class)
public class TgReMaxVehicleReadingDao extends CommonEntityDao<TgReMaxVehicleReading> implements TgReMaxVehicleReadingCo {

    @Override
    public Either<Long, TgReMaxVehicleReading> save(final TgReMaxVehicleReading entity, final Optional<fetch<TgReMaxVehicleReading>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
