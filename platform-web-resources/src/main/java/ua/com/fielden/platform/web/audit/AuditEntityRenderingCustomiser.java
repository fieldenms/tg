package ua.com.fielden.platform.web.audit;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/// Standard rendering customiser for synthetic audit-entity types.
///
public class AuditEntityRenderingCustomiser implements IRenderingCustomiser<Map<String, Object>> {

    private static final String LIGHT_BLUE = "#42A5F5";
    private static final Map<String, Map<String, String>> PROP_STYLE =
            ImmutableMap.of("backgroundStyles", ImmutableMap.of("background-color", LIGHT_BLUE));

    /// @param entity  synthetic audit-entity
    ///
    @Override
    public Optional<Map<String, Object>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final var synAudit = (AbstractSynAuditEntity<?>) entity;

        final Map<String, Object> styles = synAudit.getChangedProps().stream()
                .map(prop -> prop.getProperty().getPropertyName())
                .flatMap(prop -> makeStyles(synAudit, prop))
                .collect(T2.toMap(($1, $2) -> $2, HashMap::new));

        return Optional.of(styles);
    }

    /// Specifies how to style `property` that was recorded as changed for audit-record `entity`
    /// (i.e., `entity.getChangedProps().contains(property)`).
    ///
    /// In a typical scenario, the result is a single pair (`property`, `style`).
    /// The result may also be an empty stream or it may contain multiple pairs, and not necessarily include `property`.
    ///
    protected Stream<T2<String, Object>> makeStyles(final AbstractSynAuditEntity<?> entity, final String property) {
        return Stream.of(T2.t2(property, PROP_STYLE));
    }

}
