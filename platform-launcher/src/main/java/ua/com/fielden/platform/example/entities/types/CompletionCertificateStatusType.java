package ua.com.fielden.platform.example.entities.types;

import ua.com.fielden.platform.example.entities.CompletionCertificateStatus;
import ua.com.fielden.platform.persistence.types.EnumUserType;

/**
 * This is a Hibernate type for {@link CompletionCertificateStatus}, which should be specified in the mapping instead of the actual type.
 *
 * @author 01es
 *
 */
public class CompletionCertificateStatusType extends EnumUserType<CompletionCertificateStatus> {
    public CompletionCertificateStatusType() {
	super(CompletionCertificateStatus.class);
    }
}
