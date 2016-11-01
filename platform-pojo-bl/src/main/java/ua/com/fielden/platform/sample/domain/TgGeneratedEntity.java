package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
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
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgGeneratedEntity.class)
@MapEntityTo
public class TgGeneratedEntity extends AbstractPersistentEntity<String> implements WithCreatedByUser<TgGeneratedEntity> {
    private static final long serialVersionUID = 1L;

}