package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;

@EntityType(TgVehicle.class)
public class TgVehicleDao extends CommonEntityDao<TgVehicle> implements ITgVehicle {

    @Override
    @SessionRequired
    public TgVehicle save(final TgVehicle vehicle) {
        // Make sure that the vehicle to be saved is valid before we do anything else
        vehicle.isValid().ifFailure(Result::throwRuntime);

        final boolean isNew = !vehicle.isPersisted();
        final TgVehicle savedVehicle = super.save(vehicle);
        if (isNew) {
            final ITgVehicleFinDetails co = co(TgVehicleFinDetails.class);
            final TgVehicleFinDetails finDet = co.new_();
            finDet.setKey(savedVehicle);
            finDet.setCapitalWorksNo("CAP_NO1");
            co.save(finDet);
        }
        // try saving already saved instance after creating fin details
        // the super.save relies on the fetch model reconstruction to re-fetch this instance
        return super.save(savedVehicle);
    }

}
