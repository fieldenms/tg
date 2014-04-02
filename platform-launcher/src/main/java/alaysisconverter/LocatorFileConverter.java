package alaysisconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LocatorFileConverter implements IFileConverter {

    @Override
    public void convertFile(final String filePath) {
        final String xmlString = readFile(filePath);
        String convertedString = removeAllTags(xmlString, "mode");
        convertedString = removeAllTags(convertedString, "visibleAnalysisReports");
        convertedString = placeStringBeforeTag(convertedString, "<locatorPersistentObject>" + System.getProperty("line.separator") + "<locators/>"
                + System.getProperty("line.separator") + "</locatorPersistentObject>", "autoRun");
        try {
            writeFile(filePath, convertedString);
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    private String removeAllTags(final String xmlString, final String tag) {
        String replacedString = xmlString;
        while (replacedString.contains("<" + tag + ">")) {
            final String alias = getCutOf(replacedString, tag);
            replacedString = replacedString.replaceFirst("<" + tag + ">" + alias + "</" + tag + ">", "");
        }
        replacedString = replacedString.replaceAll("<" + tag + "/>", "");
        return replacedString;
    }

    private String placeStringBeforeTag(final String xmlString, final String stringToPlace, final String tag) {
        String replacedString = xmlString;
        final int index = replacedString.indexOf("<" + tag + ">");
        replacedString = replacedString.substring(0, index) + System.getProperty("line.separator") + stringToPlace + System.getProperty("line.separator")
                + replacedString.substring(index);
        return replacedString;
    }

    private String getCutOf(final String xmlString, final String takeFrom) {
        int beginIndex = xmlString.indexOf("<" + takeFrom + ">");
        if (beginIndex < 0) {
            return "";
        } else {
            beginIndex = beginIndex + ("<" + takeFrom + ">").length();
        }
        final int endIndex = xmlString.indexOf("</" + takeFrom + ">");
        if (endIndex < 0) {
            return "";
        }
        return xmlString.substring(beginIndex, endIndex);
    }

    /**
     * 
     * 
     * @param fileName
     * @return
     */
    private String readFile(final String fileName) {
        final StringBuilder contents = new StringBuilder();
        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            final BufferedReader input = new BufferedReader(new FileReader(fileName));
            try {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky : it returns the content of a line MINUS the newline. it returns null only for the END of the stream. it returns an empty String if
                 * two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();

    }

    private void writeFile(final String fileName, final String content) throws IOException {
        final File aFile = new File(fileName);
        if (aFile == null) {
            throw new IllegalArgumentException("File should not be null.");
        }

        // use buffering
        final Writer output = new BufferedWriter(new FileWriter(aFile));
        try {
            // FileWriter always assumes default encoding is OK!
            output.write(content);
        } finally {
            output.close();
        }
    }

}
