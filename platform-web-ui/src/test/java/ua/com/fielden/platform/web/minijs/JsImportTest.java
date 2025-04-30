package ua.com.fielden.platform.web.minijs;

import org.junit.Test;
import ua.com.fielden.platform.web.minijs.exceptions.JsCodeException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.minijs.JsImport.*;

/// Unit tests for [JsImport].
///
/// @author TG Team
public class JsImportTest {

    @Test
    public void named_import_without_alias_can_be_created() {
        final var jsImport = namedImport("openLink", "reflection/tg-polymer-utils");
        assertEquals("openLink", jsImport.name());
        assertEquals("reflection/tg-polymer-utils", jsImport.path());
        assertEquals(empty(), jsImport.alias());
    }

    @Test
    public void named_import_with_alias_can_be_created() {
        final var jsImport = namedImport("openLink", "reflection/tg-polymer-utils", "open");
        assertEquals("openLink", jsImport.name());
        assertEquals("reflection/tg-polymer-utils", jsImport.path());
        assertEquals(of("open"), jsImport.alias());
    }

    @Test
    public void default_import_with_alias_can_be_created() {
        final var jsImport = defaultImport("antlr4", "polymer/lib/antlr-lib");
        assertEquals("default", jsImport.name());
        assertEquals("polymer/lib/antlr-lib", jsImport.path());
        assertEquals(of("antlr4"), jsImport.alias());
    }

    @Test
    public void import_with_blank_name_can_not_be_created() {
        final var ex = assertThrows(JsCodeException.class, () -> namedImport(" ", "reflection/tg-polymer-utils"));
        assertTrue(ex.getMessage().contains("JavaScript import statement name"));
    }

    @Test
    public void import_with_blank_path_can_not_be_created() {
        final var ex = assertThrows(JsCodeException.class, () -> namedImport("openLink", " "));
        assertTrue(ex.getMessage().contains("JavaScript import statement path"));
    }

    @Test
    public void import_with_blank_alias_can_not_be_created() {
        final var ex = assertThrows(JsCodeException.class, () -> namedImport("openLink", "reflection/tg-polymer-utils", " "));
        assertTrue(ex.getMessage().contains("JavaScript import statement alias"));
    }

    @Test
    public void default_import_without_alias_can_not_be_created() {
        // defaultImport("antlr4", "polymer/lib/antlr-lib") should be used instead.
        final var ex = assertThrows(JsCodeException.class, () -> namedImport("default", "polymer/lib/antlr-lib"));
        assertEquals(ERR_JAVA_SCRIPT_DEFAULT_IMPORT_ALIAS_IS_NOT_PROVIDED, ex.getMessage());
    }

}