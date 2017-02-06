package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * This is an alternative implementation to {@link FileSaverPostAction}, which depends on HTML5 feature <code>a.download</code>. 
 * 
 * @author TG Team
 *
 */
public class FileDownloadPostAction implements IPostAction {

    @Override
    public JsCode build() {
        final JsCode jsCode = new JsCode(
                "var byteCharacters = atob(functionalEntity.data);"
              + "var byteNumbers = new Uint8Array(byteCharacters.length);\n"
              + "for (var index = 0; index < byteCharacters.length; index++) {\n"
              + "     byteNumbers[index] = byteCharacters.charCodeAt(index);\n"
              + "}\n"
              + "var blob = new Blob([byteNumbers], {type: functionalEntity.mime});\n"
              + "var url = URL.createObjectURL(blob);\n"
              + "var a = document.createElement('a');\n"
              + "a.setAttribute('href', url);\n"
              + "a.setAttribute('download', functionalEntity.fileName);\n"
              + "a.click();\n"
              + "URL.revokeObjectURL(url)");
        return jsCode;
    }

}
