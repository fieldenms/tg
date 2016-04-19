package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * A standard post-action that should be used for saving data to a local file.
 * Its implementation depends on the contract that the underlying functional entity has properties:
 * <ul>
 * <li>mime -- a MIME type for the data being exported
 * <li>fileName -- a file name including file extension where the data should be saved
 * <li>data -- base64 string representing a binary array
 * </ul>
 * 
 * @author TG Team
 *
 */
public class FileSaverPostAction implements IPostAction {

    @Override
    public JsCode build() {
        final JsCode jsCode = new JsCode(
                "var byteCharacters = atob(functionalEntity.data);"
              + "var byteNumbers = new Uint8Array(byteCharacters.length);\n"
              + "for (var index = 0; index < byteCharacters.length; index++) {\n"
              + "     byteNumbers[index] = byteCharacters.charCodeAt(index);\n"
              + "}\n"
              + "var data = new Blob([byteNumbers], {type: functionalEntity.mime});\n"
              + "saveAs(data, functionalEntity.fileName);\n");
        return jsCode;
    }

}
