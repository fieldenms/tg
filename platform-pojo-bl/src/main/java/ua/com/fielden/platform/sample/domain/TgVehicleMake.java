package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@DefaultController2(ITgVehicleMake2.class)
public class TgVehicleMake extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicleMake() {
    }
}
