package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgDeletionTestEntity.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgDeletionTestEntity extends AbstractPersistentEntity<String> {

    private static final long serialVersionUID = 1L;
}