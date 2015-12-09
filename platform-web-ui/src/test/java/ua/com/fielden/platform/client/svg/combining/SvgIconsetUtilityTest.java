package ua.com.fielden.platform.client.svg.combining;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import ua.com.fielden.platform.svg.combining.IronIconsetUtility;

public class SvgIconsetUtilityTest {

    @Test
    public void output_file_should_contain_each_src_file() throws IOException {

        final String srcFolder = "src/test/resources/icons";
        final Set<String> srcFiles = new HashSet<String>();
        srcFiles.add("src/test/resources/icons/icon.svg");
        srcFiles.add("src/test/resources/icons/icon.svg");
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000);
        iconsetUtility.createSvgIconset(srcFolder, outputFile);
        final String contentOutputFile = Files.toString(new File(outputFile), Charsets.UTF_8);

        for (final String file : srcFiles) {
            final String contentFile = Files.toString(new File(file), Charsets.UTF_8);
            assertTrue(contentOutputFile.contains(contentFile));
        }
    }
    @Test
    public void output_file_should_contain_correct_begin_and_end() throws IOException {

        final String srcFolder = "src/test/resources/icons";
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final String fileBegin = String.format("<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\"> \n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\"> \n <iron-iconset-svg name=\"%s\" size=\"%d\"> \n <svg> \n <defs>; \n", "testName", 1000);
        final String fileEnd = "</defs> \n </svg> \n </iron-iconset-svg>";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000);
        iconsetUtility.createSvgIconset(srcFolder, outputFile);
        final String contentOutputFile = Files.toString(new File(outputFile), Charsets.UTF_8);
        assertTrue(contentOutputFile.startsWith(fileBegin)&&contentOutputFile.endsWith(fileEnd));

    }


    @org.junit.Ignore
    @Test
    public void wrong_size_cannot_be_an_argument() throws IOException {

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 100);
        final String srcFolder = "src/test/resources/icons";
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        try {
            iconsetUtility.createSvgIconset(srcFolder, outputFile);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
    }

    @Test
    public void file_not_able_to_be_written_cannot_be_an_argument() throws IOException {

        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", 1000);
        final String outputFile = "src/test/resources/fileNotForWriting.html";

        try {
            iconsetUtility.createSvgIconset(srcFolder, outputFile);
            fail();
        } catch (final IOException ex) {
        }
    }

}
