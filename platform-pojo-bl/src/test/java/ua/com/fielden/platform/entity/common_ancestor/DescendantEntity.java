package ua.com.fielden.platform.entity.common_ancestor;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

public class DescendantEntity extends CommonAncestorEntity {
    @IsProperty
    @CompositeKeyMember(1)
    protected JustEntity property1;

    public JustEntity getProperty1() {
        return property1;
    }

    @Observable
    public void setProperty1(final JustEntity property1) {
        this.property1 = property1;
    }
}
