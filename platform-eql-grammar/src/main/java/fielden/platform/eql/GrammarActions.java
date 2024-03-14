package fielden.platform.eql;

import fielden.platform.bnf.util.BnfToG4;
import fielden.platform.bnf.util.BnfToHtml;
import fielden.platform.bnf.util.BnfToText;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static fielden.platform.bnf.util.BnfVerifier.verifyBnf;
import static fielden.platform.eql.CanonicalEqlGrammar.canonical_bnf;

final class GrammarActions {

    private GrammarActions() {}

    private static void printUsage() {
        System.out.print("""
        Usage:
          print-bnf html FILE -- creates an HTML document with the BNF
          generate antlr4 DIR -- creates an ANTLR4 grammar for the BNF in the given directory
          print-bnf text [FILE] -- prints the BNF to file (or stdout) in human-readable format
          verify -- verifies the BNF for correctness
          """);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        final String command = args[0];
        if ("generate".equals(command)) {
            if (args.length >= 2) {
                String format = args[1];

                if ("antlr4".equals(format)) {
                    if (args.length >= 3) {
                        var result = new BnfToG4(canonical_bnf, "EQL").bnfToG4();
                        final String dir = args[2];
                        BnfToG4.writeResult(result, Path.of(dir));
                        System.out.println("ANTLR4 files generated in %s".formatted(dir));
                        return;
                    }
                }
                else {
                    final PrintStream out;
                    final String outName;
                    if (args.length >= 3) {
                        out = new PrintStream(new FileOutputStream(args[2]));
                        outName = args[2];
                    } else {
                        out = System.out;
                        outName = "stdout";
                    }

                    if ("html".equals(format)) {
                        out.println(new BnfToHtml().bnfToHtml(canonical_bnf));
                    } else {
                        out.println(new BnfToText().bnfToText(canonical_bnf));
                    }

                    System.out.println("Output written to %s".formatted(outName));
                    return;
                }
            }
        }
        else if ("verify".equals(command))  {
            verifyBnf(canonical_bnf);
            return;
        }
        else {
            System.err.println("Unrecognised command: %s".formatted(command));
            return;
        }

        printUsage();
        System.exit(1);
    }

}
