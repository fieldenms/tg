package ua.com.fielden.platform.swing.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * This is a file filter for common use with JFileChooser, which filters our files based on the provided extensions.
 * 
 * @author 01es
 * 
 */
public class ExtensionFileFilter extends FileFilter {
    private final String description;
    private final String extensions[];

    public ExtensionFileFilter(final String description, final String... extensions) {
        if (description == null) {
            // Since no description, use first extension and # of extensions as description
            this.description = extensions[0] + "{ " + extensions.length + "} ";
        } else {
            this.description = description;
        }
        // Convert array to lowercase
        // Don't alter original entries
        this.extensions = extensions.clone();
        toLower(this.extensions);
    }

    private void toLower(final String array[]) {
        for (int index = 0, n = array.length; index < n; index++) {
            array[index] = array[index].toLowerCase();
        }
    }

    public String getDescription() {
        return description;
    }

    /**
     * Ignore case, always accept directories. Character before extension must be a full stop (i.e. dot).
     */
    public boolean accept(final File file) {
        if (file.isDirectory()) {
            return true;
        } else {
            final String extension = getExtension(file);
            for (final String currExt : extensions) {
                if (currExt.equals(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines the extension of a file.
     */
    public static String getExtension(final File file) {
        String ext = null;
        final String s = file.getName();
        final int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}
