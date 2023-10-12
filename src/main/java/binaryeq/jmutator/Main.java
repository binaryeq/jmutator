package binaryeq.jmutator;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;
import java.io.File;
import java.io.IOException;

/**
 * Main class to convert a class files within a folder.
 * @author jens dietrich
 */
public class Main {

    public static void main (String[] args) throws IOException, ParseException {

        Options options = new Options();
        options.addRequiredOption("b", "binaries folder", true, "root folder containing .class files");
        options.addRequiredOption("m", "mutated binaries folder", true, "folder to write the mutated  .class files to");
        options.addRequiredOption("p", "conversion pattern", true, "a pattern used to name mutated class files, must contain the strings $n (the original class file name without extension) and $i (the mutation number, and int))");
        options.addRequiredOption("j", "provenance pattern", true, "a pattern used to name json-encoded provenance files, must contain the strings $n (the original class file name without extension) and $i (the mutation number, and int))");
        options.addRequiredOption("v", "verify", false, "verify generated byte code (verification is ASM-based, will result in error if verification fails)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String binDirName = cmd.getOptionValue("b");
        File binDir = new File(binDirName);


        String mutatedBinDirName = cmd.getOptionValue("m");
        File mutatedBinDir = new File(mutatedBinDirName);

        String classConversionPattern = cmd.getOptionValue("p");
        String provenanceInfoConversionPattern = cmd.getOptionValue("j");

        boolean verify = cmd.hasOption("v");

        ClassMutator.mutateClassFiles(binDir,mutatedBinDir,classConversionPattern,provenanceInfoConversionPattern,verify);
    }

}
