package ua.com.fielden.platform.test.types;

import ua.com.fielden.platform.persistence.types.EnumUserType;
import ua.com.fielden.platform.test.domain.entities.RotableStatus;

/**
 * This is a Hibernate type for {@link RotableStatus}, which should be specified in the mapping instead of the actual type.
 * 
 * @author 01es
 * 
 */
public class RotableStatusType extends EnumUserType<RotableStatus> {
    public RotableStatusType() {
        super(RotableStatus.class);
    }
}
