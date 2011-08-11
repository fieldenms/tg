package ua.com.fielden.platform.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgWorkOrder extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo()
    private TgVehicle vehicle;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgWorkOrder() {
    }
}
