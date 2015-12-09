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
        final Set<String> srcFiles = new HashSet();
        srcFiles.add("src/test/resources/icons/icon.svg");
        srcFiles.add("src/test/resources/icons/icon.svg");
        final String outputFile = "src/test/resources/combiningSvgResult.html";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", "1000");
        iconsetUtility.createSvgIconset(srcFolder, outputFile);
        final String contentOutputFile = Files.toString(new File(outputFile), Charsets.UTF_8);

        for (final String file : srcFiles) {
            final String contentFile = Files.toString(new File(file), Charsets.UTF_8);
            assertTrue(contentOutputFile.contains(contentFile));
        }
    }

    @org.junit.Ignore
    @Test
    public void file_wich_does_not_exist_can_not_be_an_argument() throws IOException {

        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", "1000");
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        try {
            iconsetUtility.createSvgIconset(srcFolder, outputFile);
            fail();
        }catch (final IllegalArgumentException e){

        }
    }

    @Test
    public void wrong_size_can_not_be_an_argument() throws IOException {

        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", "a100");
        final String srcFolder = "src/test/resources/icons";
        final String outputFile = "src/test/resources/combiningSvgResult.html";

        try {
            iconsetUtility.createSvgIconset(srcFolder, outputFile);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
    }

    @Test
    public void file_wich_is_not_able_to_be_writed_can_not_be_an_argument() throws IOException {

        final String srcFolder = "src/test/resources/icons";
        final IronIconsetUtility iconsetUtility = new IronIconsetUtility("testName", "1000");
        final String outputFile = "src/test/resources/fileNotForWriting.html";

        try {
            iconsetUtility.createSvgIconset(srcFolder, outputFile);
            fail();
        } catch (final IOException ex) {
        }
    }

}
