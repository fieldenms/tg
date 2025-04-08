package ua.com.fielden.platform.security.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.Test;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test case to ensure correct generation of security tokens for audit types.
 *
 * @author TG Team
 */
public class AuditSecurityTokensTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory() {
                @Override protected AuditingMode auditingMode() {
                    return AuditingMode.ENABLED;
                }
            })
            .add(new AbstractModule() {
                @Override protected void configure() {
                    bindConstant().annotatedWith(Names.named("tokens.path")).to("target/test-classes");
                    bindConstant().annotatedWith(Names.named("tokens.package")).to("ua.com.fielden.platform.security.provider");
                }
            })
            .getInjector();

    @Test
    public void synthetic_audit_entity_type_has_READ_and_READ_MODEL_tokens() {
        final var provider = injector.getInstance(ISecurityTokenProvider.class);
        final var auditTypeFinder = injector.getInstance(IAuditTypeFinder.class);
        final var appDomain = injector.getInstance(IApplicationDomainProvider.class);

        final var auditedTypes = appDomain.entityTypes().stream().filter(AuditUtils::isAudited).toList();
        assertThat(auditedTypes).isNotEmpty();
        assertThat(auditedTypes)
                .allSatisfy(ty -> {
                    final var synAuditType = auditTypeFinder.navigate(ty).synAuditEntityType();
                    assertThat(provider.getTokenByName(Template.READ.forClassName().formatted(synAuditType.getSimpleName())))
                            .isPresent();
                    assertThat(provider.getTokenByName(Template.READ_MODEL.forClassName().formatted(synAuditType.getSimpleName())))
                            .isPresent();
                });
    }

}
