package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(ActivatableUnionOwner.class)
public class ActivatableUnionOwnerDao extends CommonEntityDao<ActivatableUnionOwner> implements ActivatableUnionOwnerCo {}
