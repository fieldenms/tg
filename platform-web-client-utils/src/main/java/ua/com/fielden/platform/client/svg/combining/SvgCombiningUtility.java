package ua.com.fielden.platform.client.svg.combining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class SvgCombiningUtility {

    public SvgCombiningUtility() {

    }

    private final static String fileBegin = "src/test/resources/SvgCombinerBegin";
    private final static String fileEnd = "src/test/resources/SvgCombinerEnd";

    public static Stream<String> svgCombining(final String[] srcFiles, final String destFile) {

        for(final String s:srcFiles){
            if (s.isEmpty()||!(new File(s).exists())){
                throw new IllegalArgumentException("One or more src file does`n exist!");
            }
        }
        if(!new File(destFile).exists()||destFile.isEmpty()){
            throw new IllegalArgumentException("Dest file doesn`t exist!");
        }
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(fileBegin));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    private static void parse(final Stream<String> lines, final String output) throws IOException {
        final FileWriter fw = new FileWriter(output);
        lines.forEach(packageName-> writeToFile(fw, packageName));
        fw.close();
        lines.close();
    }

    private static void writeToFile(final FileWriter fw, final String packageName) {
        try {
            fw.write(String.format("%s%n", packageName));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws IOException {
        final String[] srcFiles = new String[args.length + 1];
        final int lastFile = args.length - 1;
        final String destFile = new String(args[lastFile]);

        srcFiles[0] = fileBegin;
        srcFiles[args.length] = fileEnd;
        for (int i = 1; i < lastFile; i++) {
            srcFiles[i] = new String(args[i]);
        }

        parse(svgCombining(srcFiles, destFile), args[lastFile]);
       //svgCombining(srcFiles, destFile);
        //new SvgCombiningUtility().parse(svgCombining(srcFiles, destFile), args[lastFile]);
    }
}
