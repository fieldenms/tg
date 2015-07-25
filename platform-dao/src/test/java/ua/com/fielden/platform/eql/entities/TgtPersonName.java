package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/**
 * Master entity object.
 * 
 * @author Developers
 * 
 */
@KeyType(String.class)
@MapEntityTo
public class TgtPersonName extends AbstractEntity<String> {

}