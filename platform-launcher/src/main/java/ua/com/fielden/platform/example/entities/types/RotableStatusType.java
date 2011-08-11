package ua.com.fielden.platform.example.entities.types;

import ua.com.fielden.platform.example.entities.RotableStatus;
import ua.com.fielden.platform.persistence.types.EnumUserType;

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
