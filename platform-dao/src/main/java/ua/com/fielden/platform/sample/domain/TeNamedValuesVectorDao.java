package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TeNamedValuesVector.class)
public class TeNamedValuesVectorDao extends CommonEntityDao<TeNamedValuesVector> implements TeNamedValuesVectorCo {
}
