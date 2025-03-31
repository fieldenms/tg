package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/**
 * A standard post-action that should be used for saving data to a local file.
 * Its implementation depends on the contract that the underlying functional entity has properties:
 * <ul>
 * <li>mime -- a MIME type for the data being exported
 * <li>fileName -- a file name including file extension where the data should be saved
 * <li>data -- base64 string representing a binary array
 * </ul>
 * 
 * See also an alternative implementation {@link FileDownloadPostAction}.
 * 
 * @author TG Team
 *
 */
public class FileSaverPostAction implements IPostAction {

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("saveAs", "polymer/lib/file-saver-lib"));
    }

    @Override
    public JsCode build() {
        final JsCode jsCode = new JsCode(
                "const byteCharacters = atob(functionalEntity.data);"
              + "const byteNumbers = new Uint8Array(byteCharacters.length);\n"
              + "for (let index = 0; index < byteCharacters.length; index++) {\n"
              + "     byteNumbers[index] = byteCharacters.charCodeAt(index);\n"
              + "}\n"
              + "const data = new Blob([byteNumbers], {type: functionalEntity.mime});\n"
              + "saveAs(data, functionalEntity.fileName);\n"
              + "if (self.$.egi && self.$.egi.clearPageSelection) {\n"
              + "    self.$.egi.clearPageSelection();\n"
              + "}\n");
        return jsCode;
    }

}
