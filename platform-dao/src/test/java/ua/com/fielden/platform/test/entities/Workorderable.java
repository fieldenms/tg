package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.Wheelset;

@EntityTitle(value = "Workorderable", desc = "Workorderable entity")
@KeyTitle(value = "Workorderable key")
@DescTitle(value = "Workorderable description", desc = "Description of Workorderable")
public class Workorderable extends AbstractUnionEntity {

    private static final long serialVersionUID = 7362243737334921917L;

    @IsProperty
    @Title("Bogie")
    private Bogie bogie;

    @IsProperty
    @Title("Wheelset")
    private Wheelset wheelset;

    public Bogie getBogie() {
        return bogie;
    }

    public Wheelset getWheelset() {
        return wheelset;
    }

    @Observable
    public void setBogie(final Bogie bogie) {
        this.bogie = bogie;
    }

    @Observable
    public void setWheelset(final Wheelset wheelset) {
        this.wheelset = wheelset;
    }
}
