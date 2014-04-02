package alaysisconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderConverter {

    public void convertFolder(final String folderPath) {
        //final IFileConverter converter = new Converter();
        final IFileConverter converter = new LocatorFileConverter();
        final List<File> folders = new ArrayList<File>();
        final File startFile = new File(folderPath);
        if (startFile.exists() && startFile.isDirectory()) {
            folders.add(startFile);
        } else if (startFile.getAbsolutePath().endsWith(".dcf")) {
            System.out.println("parsing file:--------------" + startFile.getAbsolutePath());
            converter.convertFile(startFile.getAbsolutePath());
            return;
        }
        while (!folders.isEmpty()) {
            final File nextFolder = folders.get(0);
            System.out.println("parsing folder:--------------" + nextFolder.getAbsolutePath());
            final File[] files = nextFolder.listFiles();
            for (final File file : files) {
                if (file.exists() && file.isDirectory()) {
                    folders.add(file);
                } else if (file.getAbsolutePath().endsWith(".dcf")) {
                    System.out.println("parsing file:--------------" + file.getAbsolutePath());
                    converter.convertFile(file.getAbsolutePath());
                }
            }
            folders.remove(0);
        }
    }
}
