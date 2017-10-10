package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(IEntityNewAction.class)
public class EntityNewAction extends AbstractEntityManipulationAction {
    
}