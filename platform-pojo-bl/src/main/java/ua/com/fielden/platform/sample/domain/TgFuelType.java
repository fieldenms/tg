package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@DefaultController(ITgFuelType.class)
public class TgFuelType extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected TgFuelType() {
    }
}