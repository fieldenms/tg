package ua.com.fielden.platform.entity.validation;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithMaxLengthValidation;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.validation.SanitiseHtmlValidator.ERR_UNSAFE;

public class StringHtmlSanitisationTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void vulnerable_HTML_cannot_be_assigned_to_String_props() {
        final var maxLength = 52;
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<String> mp = entity.getProperty("stringPropWithoutExplicitMaxLengthValidator");

        // Assert the presence of default validators and their order.
        var validators = mp.getValidators().get(ValidationAnnotation.BEFORE_CHANGE).keySet().stream().toList();
        assertThat(validators).hasSize(2);
        assertThat(validators.getFirst()).isInstanceOf(MaxLengthValidator.class);
        assertThat(validators.getLast()).isInstanceOf(SanitiseHtmlValidator.class);

        // Try to assign vulnerable HTML of the unacceptable length.
        final String longVulnerableHtml = "</div\"'><img src=pentest onerror=alert(003)>{{7+7}}</img>";
        entity.setStringPropWithoutExplicitMaxLengthValidator(longVulnerableHtml);
        assertFalse(mp.isValid());
        assertEquals(MaxLengthValidator.ERR_VALUE_SHOULD_NOT_EXCEED_MAX_LENGTH.formatted(maxLength), mp.getFirstFailure().getMessage());

        // Try to assign vulnerable HTML of the acceptable length.
        final String vulnerableHtml = "</div\"'><img src=pentest onerror=alert(003)>{{7+7}}";
        entity.setStringPropWithoutExplicitMaxLengthValidator(vulnerableHtml);
        assertFalse(mp.isValid());
        assertThat(mp.getFirstFailure().getMessage()).isEqualTo("""
                %s<extended/>Input contains unsafe HTML:
                1. Tag [img] has violating attributes: onerror\
                """.formatted(ERR_UNSAFE));
    }

    @Test
    public void vulnerable_HTML_cannot_be_assigned_to_String_keys() {
        final var entity = factory.newEntity(EntityWithMaxLengthValidation.class);
        final MetaProperty<String> mp = entity.getProperty(KEY);

        // Assert the presence of default validators and their order.
        final var validators = mp.getValidators().get(ValidationAnnotation.BEFORE_CHANGE).keySet().stream().toList();
        assertThat(validators).hasSize(4);
        final var iter = validators.iterator();
        assertThat(iter.next()).isInstanceOf(SanitiseHtmlValidator.class);
        assertThat(iter.next()).isInstanceOf(RestrictNonPrintableCharactersValidator.class);
        assertThat(iter.next()).isInstanceOf(RestrictExtraWhitespaceValidator.class);
        assertThat(iter.next()).isInstanceOf(RestrictCommasValidator.class);

        // Try to assign vulnerable HTML.
        final String vulnerableHtml = "</div\"'><img src=pentest onerror=alert(003)>{{7+7}}";
        entity.setKey(vulnerableHtml);
        assertFalse(mp.isValid());
        assertThat(mp.getFirstFailure().getMessage()).isEqualTo("""
                %s<extended/>Input contains unsafe HTML:
                1. Tag [img] has violating attributes: onerror\
                """.formatted(ERR_UNSAFE));
    }

}
