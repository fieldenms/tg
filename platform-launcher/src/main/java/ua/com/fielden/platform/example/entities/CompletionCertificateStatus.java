/**
 *
 */
package ua.com.fielden.platform.example.entities;

import java.util.EnumSet;

/**
 * Represents {@link CompletionCertificate} status. Should be used instead of {@link CompletionCertificate#getAtContractorWorkshop()} property.
 *
 * @author Yura
 */
public enum CompletionCertificateStatus {

    E("Entered") {
	public CompletionCertificateStatus next() {
	    return R;
	}
    },
    R("PNL Review") {
	public CompletionCertificateStatus next() {
	    return A;
	}
    },
    A("Accepted by PNL") {
	public CompletionCertificateStatus next() {
	    return C;
	}
    },
    C("Completed") {
	public CompletionCertificateStatus next() {
	    return A;
	}
    };

    private final String desc;

    CompletionCertificateStatus(final String desc) {
	this.desc = desc;
    }

    public String toString() {
	return desc;
    }

    /**
     * Defines transition between statuses.
     * @return
     */
    public abstract CompletionCertificateStatus next();

    /**
     * Return a set of values that can be referred to as active.
     *
     * @return
     */
    public static EnumSet<CompletionCertificateStatus> getActiveStatuses() {
	return EnumSet.range(E, A);
    }
}
