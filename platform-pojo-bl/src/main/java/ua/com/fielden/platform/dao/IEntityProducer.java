package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to be implemented for uniform instantiation of entities of a certain type. The original intention of this interface is its use in {@link UmCustomEntityCentre},
 * {@link UmEntityCentre} and {@link UmMasterWithCrud} for instantiation of new entities.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IEntityProducer<T extends AbstractEntity> {
    T newEntity();
}
