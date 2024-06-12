package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgVehicleTechDetails.class)
public class TgVehicleTechDetailsDao extends CommonEntityDao<TgVehicleTechDetails> implements TgVehicleTechDetailsCo {
}
