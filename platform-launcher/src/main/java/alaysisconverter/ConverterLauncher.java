package alaysisconverter;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.swing.file.ExtensionFileFilter;

public class ConverterLauncher {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new ExtensionFileFilter("Dynamic Criteria File (*.dcf)", "dcf"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                switch (fileChooser.showOpenDialog(null)) {
                case JFileChooser.APPROVE_OPTION:
                    final FolderConverter converter = new FolderConverter();
                    converter.convertFolder(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }

        });
    }
}
