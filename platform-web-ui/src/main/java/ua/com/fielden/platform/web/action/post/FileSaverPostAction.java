package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// A standard post-action that should be used for saving data to a local file.
/// Its implementation depends on the contract that the underlying functional entity has properties:
///   - mime -- a MIME type for the data being exported
///   - fileName -- a file name including file extension where the data should be saved
///   - data -- base64 string representing a binary array
///
/// @author TG Team
public class FileSaverPostAction implements IPostAction {

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("saveAs", "polymer/lib/file-saver-lib"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            const byteCharacters = atob(functionalEntity.data);
            const byteNumbers = new Uint8Array(byteCharacters.length);
            for (let index = 0; index < byteCharacters.length; index++) {
                 byteNumbers[index] = byteCharacters.charCodeAt(index);
            }
            const data = new Blob([byteNumbers], {type: functionalEntity.mime});
            saveAs(data, functionalEntity.fileName);
            if (self.$.egi && self.$.egi.clearPageSelection) {
                self.$.egi.clearPageSelection();
            }
        """);
    }

}
