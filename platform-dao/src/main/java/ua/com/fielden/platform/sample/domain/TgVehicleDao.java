package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

@EntityType(TgVehicle.class)
public class TgVehicleDao extends CommonEntityDao<TgVehicle> implements ITgVehicle {

    @Override
    @SessionRequired
    public Either<Long, TgVehicle> save(final TgVehicle vehicle, final Optional<fetch<TgVehicle>> maybeFetch) {
        // Make sure that the vehicle to be saved is valid before we do anything else
        vehicle.isValid().ifFailure(Result::throwRuntime);

        final boolean isNew = !vehicle.isPersisted();
        if (isNew) {
            final ITgVehicleFinDetails co = co(TgVehicleFinDetails.class);
            final var savedVehicle = super.save(vehicle, Optional.of(co.getFetchProvider().<TgVehicle>fetchFor(KEY).fetchModel())).asRight().value();
            final TgVehicleFinDetails finDet = co.new_();
            finDet.setKey(savedVehicle);
            finDet.setCapitalWorksNo("CAP_NO1");
            co.save(finDet);
            return super.save(savedVehicle, maybeFetch);
        }
        else {
            return super.save(vehicle, maybeFetch);
        }
    }

    @Override
    protected IFetchProvider<TgVehicle> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
