package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/**
 * Type of common property from {@link TgUnion} types.
 * 
 * @author TG Team
 *
 */
@EntityTitle("TG Union Common Type")
@KeyType(String.class)
@KeyTitle("Key")
@DescTitle("Description") // desc property is present
@CompanionObject(TgUnionCommonTypeCo.class)
@MapEntityTo
public class TgUnionCommonType extends AbstractPersistentEntity<String> {

}