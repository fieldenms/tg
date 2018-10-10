package ua.com.fielden.platform.web.interfaces;


/**
 * A contract for any kind of Java web UI component that can have its "import path".
 *
 * @author TG Team
 *
 */
public interface IImportable {

    /**
     * Returns the import path for this Java web UI component.
     *
     * @return
     */
    String importPath();
}
