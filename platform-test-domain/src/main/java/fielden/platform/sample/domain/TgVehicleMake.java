package fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgVehicleMake extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicleMake() {
    }
}
