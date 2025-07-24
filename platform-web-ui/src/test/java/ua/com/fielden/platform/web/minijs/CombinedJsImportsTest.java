package ua.com.fielden.platform.web.minijs;

import org.junit.Test;
import ua.com.fielden.platform.web.minijs.exceptions.JsCodeException;

import static java.util.Set.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.web.minijs.CombinedJsImports.ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT;
import static ua.com.fielden.platform.web.minijs.JsImport.defaultImport;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// Unit tests for [CombinedJsImports].
///
/// @author TG Team
public class CombinedJsImportsTest {

    @Test
    public void same_import_statements_add_only_once() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.add(namedImport("TgReflector", "/app/tg-reflector"));
        combinedImports.add(namedImport("TgReflector", "/app/tg-reflector"));
        assertEquals(1, combinedImports.size());
    }

    @Test
    public void conflicting_import_statements_cant_be_added() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.add(namedImport("openLink", "reflection/tg-polymer-utils"));
        final var ex = assertThrows(JsCodeException.class, () -> combinedImports.add(namedImport("openLink", "/app/tg-reflector")));
        assertEquals(ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT.formatted(combinedImports), ex.getMessage());
    }

    @Test
    public void conflicting_import_statements_cant_be_added_in_a_batch() {
        final var combinedImports = new CombinedJsImports();
        final var ex = assertThrows(JsCodeException.class, () -> combinedImports.addAll(of(
            namedImport("openLink", "reflection/tg-polymer-utils"),
            namedImport("openLink", "/app/tg-reflector")
        )));
        assertEquals(ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT.formatted(combinedImports), ex.getMessage());
    }

    @Test
    public void aliased_conflicting_import_statements_cant_be_added() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.add(namedImport("openLink", "reflection/tg-polymer-utils"));
        final var ex = assertThrows(JsCodeException.class, () -> combinedImports.add(namedImport("openLinkInNewWindow", "/app/tg-reflector", "openLink")));
        assertEquals(ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT.formatted(combinedImports), ex.getMessage());
    }

    @Test
    public void default_conflicting_import_statements_cant_be_added() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.add(namedImport("openLink", "reflection/tg-polymer-utils"));
        final var ex = assertThrows(JsCodeException.class, () -> combinedImports.add(defaultImport("openLink", "lib/link-opener-lib")));
        assertEquals(ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT.formatted(combinedImports), ex.getMessage());
    }

    @Test
    public void added_import_statement_becomes_aliased() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.add(namedImport("TgReflector", "/app/tg-reflector"));
        assertEquals(of(namedImport("TgReflector", "/app/tg-reflector", "TgReflector")), combinedImports);
    }

    @Test
    public void added_import_statements_convert_to_full_aliased_form_of_code_with_natural_order_by_aliases() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.addAll(of(
                namedImport("openLink", "reflection/tg-polymer-utils"),
                namedImport("TgReflector", "/app/tg-reflector"),
                defaultImport("antlr4", "polymer/lib/antlr-lib")
        ));
        assertEquals("""
        
        import { TgReflector as TgReflector } from '/app/tg-reflector.js';
        import { default as antlr4 } from '/resources/polymer/lib/antlr-lib.js';
        import { openLink as openLink } from '/resources/reflection/tg-polymer-utils.js';""", combinedImports.toString());
    }

    @Test
    public void added_import_statements_convert_to_full_aliased_form_of_code_with_natural_order_by_aliases_with_import_object() {
        final var combinedImports = new CombinedJsImports();
        combinedImports.addAll(of(
            namedImport("openLink", "reflection/tg-polymer-utils"),
            namedImport("TgReflector", "/app/tg-reflector"),
            defaultImport("antlr4", "polymer/lib/antlr-lib")
        ));
        assertEquals("""
        
        import { TgReflector as TgReflector } from '/app/tg-reflector.js';
        import { default as antlr4 } from '/resources/polymer/lib/antlr-lib.js';
        import { openLink as openLink } from '/resources/reflection/tg-polymer-utils.js';
        const mainMenuActionImports = {TgReflector: TgReflector,antlr4: antlr4,openLink: openLink};""", combinedImports.toStringWith("mainMenuActionImports"));
    }

}