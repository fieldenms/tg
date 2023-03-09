package ua.com.fielden.platform.processors.verify.annotation;

import java.util.EnumMap;

import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.verify.exceptions.VerifierException;;

/**
 * Verification relaxation policy. The constants of this enumerated class describe the various policies for relaxing verification.
 * They are used along with the {@link RelaxVerification} annotation to specify the degree to which verification should be relaxed.
 *
 * @author homedirectory
 */
public enum RelaxationPolicy {
    /**
     * Report errors as warnings.
     * That is, relax {@link Kind#ERROR} to {@link Kind#MANDATORY_WARNING}.
     */
    WARN(Kind.MANDATORY_WARNING),

    /**
     * Report errors and warnings as informative messages.
     * That is, relax {@link Kind#ERROR}, {@link Kind#WARNING}, {@link Kind#MANDATORY_WARNING} to {@link Kind#NOTE}.
     */
    INFO(Kind.NOTE),

    /**
     * Skip verification entirely.
     */
    SKIP(null);


    private final Kind kind;

    RelaxationPolicy(final Kind kind) {
        this.kind = kind;
    }

    /**
     * Applies the policy represented by this enum constant to the given diagnostic kind. The severity of the returned kind will be {@code <=}
     * than the given one.
     *
     * @param kind
     * @return
     * @throws VerifierExceptioin   if this enum constant is {@link #SKIP}
     */
    public Kind relaxedKind(final Kind kind) {
        if (this == SKIP) {
            throw new VerifierException("Relaxation Policy SKIP cannot be used to relax a diagnostic kind.");
        }
        // if given kind is more severe, then relax it
        return (severityOf(kind) > severityOf(this.kind)) ? this.kind : kind;
    }

    private static final EnumMap<Kind, Integer> KIND_SEVERITY_MAP = new EnumMap<>(Kind.class);

    static {
        KIND_SEVERITY_MAP.put(Kind.ERROR, 2);
        KIND_SEVERITY_MAP.put(Kind.MANDATORY_WARNING, 1);
        KIND_SEVERITY_MAP.put(Kind.WARNING, 1);
        KIND_SEVERITY_MAP.put(Kind.NOTE, 0);
        KIND_SEVERITY_MAP.put(Kind.OTHER, 0);
    }

    private static int severityOf(final Kind kind) {
        return KIND_SEVERITY_MAP.get(kind);
    }

}
