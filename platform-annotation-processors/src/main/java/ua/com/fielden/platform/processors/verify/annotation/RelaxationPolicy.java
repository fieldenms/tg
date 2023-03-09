package ua.com.fielden.platform.processors.verify.annotation;

import javax.tools.Diagnostic.Kind;;

/**
 * Verification relaxation policy. The constants of this enumerated class describe the various policies for relaxing verification.
 * They are used along with the {@link RelaxVerification} annotation to specify the degree to which verification should be relaxed.
 *
 * @author homedirectory
 */
public enum RelaxationPolicy {
    /**
     * Report errors as warnings.
     * That is, relax {@link Kind#ERROR} to {@link Kind#WARNING}.
     */
    WARN,

    /**
     * Report errors and warnings as informative messages.
     * That is, relax {@link Kind#ERROR}, {@link Kind#WARNING}, {@link Kind#MANDATORY_WARNING} to {@link Kind#NOTE}.
     */
    INFO,

    /**
     * Skip verification entirely.
     */
    SKIP
}
