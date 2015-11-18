package ua.com.fielden.platform.client.svg.combining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class SvgCombiningUtility {

    public SvgCombiningUtility() {

    }

    private final static File fileBegin = new File("src/test/resources/SvgCombinerBegin");
    private final static File fileEnd = new File("src/test/resources/SvgCombinerEnd");

//    public void svgCombining(final File src[], final File dest) throws IOException {
//
//        final File fileBegin = new File("src/test/resources/SvgCombinerBegin");
//        final File fileEnd = new File("src/test/resources/SvgCombinerEnd");
//
//        FileUtils.copyFile(fileBegin, dest);
//        FileUtils.copyFile(src[1], dest);
//        FileUtils.copyFile(fileEnd, dest);
//
//    }



    public static void svgCombining (final File[] srcFiles, final File destFile) {

        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter(destFile, true);
             out = new BufferedWriter(fstream);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        for (final File file : srcFiles) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                final BufferedReader in = new BufferedReader(new InputStreamReader(fis));

                String aLine;
                while ((aLine = in.readLine()) != null) {
                    out.write(aLine);
                    out.newLine();
                }

                in.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        try {
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(final String[] args) {
        final File[] srcFiles = new File[args.length + 1];
        final int lastFile = args.length - 1;
        final File destFile = new File(args[lastFile]);

        srcFiles[0] = fileBegin;
        srcFiles[args.length] = fileEnd;
        for (int i = 1; i < lastFile; i++) {
            srcFiles[i] = new File(args[i]);
        }

        svgCombining(srcFiles, destFile);
    }
}
