package ua.com.fielden.platform.processors.verify.annotation;

import org.junit.Test;

import javax.tools.Diagnostic.Kind;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.*;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.processors.verify.annotation.RelaxationPolicy.INFO;
import static ua.com.fielden.platform.processors.verify.annotation.RelaxationPolicy.WARN;

;

/**
 * Tests covering the {@link RelaxationPolicy} enum.
 *
 * @author TG Team
 */
public class RelaxationPolicyTest {

    @Test
    public void policy_WARN_relaxes_ERROR_to_MANDATORY_WARNING() {
        assertEquals(MANDATORY_WARNING, WARN.relaxedKind(ERROR));
    }

    @Test
    public void policy_WARN_does_not_relax_anything_except_ERROR() {
        final Set<Kind> butError = EnumSet.allOf(Kind.class);
        butError.remove(ERROR);

        for (final var kind: butError)
        assertEquals(kind, WARN.relaxedKind(kind));
    }

    @Test
    public void policy_INFO_relaxes_ERROR_and_WARNING_and_MANDATORY_WARNING_to_OTHER() {
        for (final var kind: List.of(ERROR, WARNING, MANDATORY_WARNING)) {
            assertEquals(OTHER, INFO.relaxedKind(kind));
        }
    }

}
