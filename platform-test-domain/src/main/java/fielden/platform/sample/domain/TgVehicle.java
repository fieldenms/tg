package fielden.platform.sample.domain;

import java.util.Date;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgVehicle extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo()
    private Date initDate;

    @IsProperty @MapTo()
    private TgVehicle replacedBy;

    @IsProperty @MapTo(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
    }
}
