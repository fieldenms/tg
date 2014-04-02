package alaysisconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;

public class Converter implements IFileConverter {

    @Override
    public void convertFile(final String filePath) {
        final String xmlString = readFile(filePath);
        String convertedString = mergeNodes(xmlString, "analysis", "lifecycleAnalysis");
        convertedString = mergeNodes(convertedString, "visibleAnalysisReports", "visibleLifecycleAnalysisReports");
        convertedString = removeAllAliases(convertedString);
        try {
            writeFile(filePath, convertedString);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String removeAllAliases(final String xmlString) {
        String replacedString = xmlString;
        while (replacedString.contains("<alias>")) {
            final String alias = getCutOf(replacedString, "alias");
            replacedString = replacedString.replaceFirst("<alias>" + alias + "</alias>", "");
        }
        return replacedString;
    }

    private String mergeNodes(final String xmlString, final String placeWhere, final String takeFrom) {
        String convertedXmlString = xmlString;
        final int whereIndex = xmlString.indexOf("<" + placeWhere + "/>");
        final int fromIndex = xmlString.indexOf("<" + takeFrom + "/>");
        if (fromIndex >= 0) {
            convertedXmlString = xmlString.replaceAll("<" + takeFrom + "/>", "");
        } else if (whereIndex >= 0 && fromIndex < 0) {
            convertedXmlString = replace(xmlString, placeWhere, takeFrom);
        } else if (whereIndex < 0 && fromIndex < 0) {
            convertedXmlString = add(xmlString, placeWhere, takeFrom);
        }
        return convertedXmlString;
    }

    private String add(final String xmlString, final String placeWhere, final String takeFrom) {
        String replacedString = xmlString;
        final String cutOfFrom = getCutOf(replacedString, takeFrom);
        if (StringUtils.isEmpty(cutOfFrom)) {
            return replacedString;
        }
        replacedString = replacedString.replaceAll("</" + placeWhere + ">", cutOfFrom + "</" + placeWhere + ">");
        replacedString = replacedString.replaceAll("<" + takeFrom + ">" + cutOfFrom + "</" + takeFrom + ">", "");
        return replacedString;
    }

    private String replace(final String xmlString, final String placeWhere, final String takeFrom) {
        String replacedString = xmlString;
        final String cutOfFrom = getCutOf(replacedString, takeFrom);
        if (StringUtils.isEmpty(cutOfFrom)) {
            return replacedString;
        }
        replacedString = replacedString.replaceAll("<" + placeWhere + "/>", "<" + placeWhere + ">" + cutOfFrom + "</" + placeWhere + ">");
        replacedString = replacedString.replaceAll("<" + takeFrom + ">" + cutOfFrom + "</" + takeFrom + ">", "");
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
