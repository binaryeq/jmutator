package binaryeq.jmutator;

import com.google.common.base.Preconditions;
import foo.SomeClass;
import org.apache.commons.cli.*;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.DefaultMutationEngineConfiguration;
import org.pitest.mutationtest.engine.gregor.config.Mutator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Main class to convert a class files within a folder.
 * @author jens dietrich
 */
public class Main {

    public static void main (String[] args) throws IOException, ParseException {

        Options options = new Options();
        options.addRequiredOption("s", "source folder", true, "root folder containing .class files");
        options.addRequiredOption("d", "destination folder", true, "folder to write the mutated  .class files to");
        options.addRequiredOption("p", "conversion pattern", true, "a pattern used to name mutated class files, must contain the strings $n (the original class file name without extension) and $i (the mutation number, and int))");
        options.addRequiredOption("j", "provenance pattern", true, "a pattern used to name json-encoded provenance files, must contain the strings $n (the original class file name without extension) and $i (the mutation number, and int))");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String srcDirName = cmd.getOptionValue("s");
        File srcDir = new File(srcDirName);
        Preconditions.checkArgument(srcDir.exists(), "source folder does not exist: " + srcDir.getAbsolutePath());
        Preconditions.checkArgument(srcDir.isDirectory(), "source folder does not a directory: " + srcDir.getAbsolutePath());

        String destDirName = cmd.getOptionValue("d");
        File destDir = new File(destDirName);
        Preconditions.checkArgument(destDir.exists(), "destination folder does not exist: " + destDir.getAbsolutePath());
        Preconditions.checkArgument(destDir.isDirectory(), "destination folder does not a directory: " + destDir.getAbsolutePath());

        String classConversionPattern = cmd.getOptionValue("p");
        Preconditions.checkArgument(classConversionPattern.contains("$n"), "class conversion pattern does not contains $n -- the class name");
        Preconditions.checkArgument(classConversionPattern.contains("$i"), "class conversion pattern does not contains $i -- the mutation id");

        String provenanceInfoConversionPattern = cmd.getOptionValue("p");
        Preconditions.checkArgument(provenanceInfoConversionPattern.contains("$n"), "provenance info conversion pattern does not contains $n -- the class name");
        Preconditions.checkArgument(provenanceInfoConversionPattern.contains("$i"), "provenance info conversion pattern does not contains $i -- the mutation id");

        mutateClassFiles(srcDir,destDir,classConversionPattern,provenanceInfoConversionPattern);
    }

    // separate method to facilitate unit testing
    static void mutateClassFiles(File srcDir, File destDir, String classConversionPattern, String provenanceInfoConversionPattern) throws IOException {
        // use only default PITEST mutator -- TODO make switch to use all incl experimental
        System.out.println("Using default pitest mutators");
        final Collection<MethodMutatorFactory> mutators = Mutator.newDefaults();

        final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(i -> true, mutators);
        MutationEngine engine = new GregorMutationEngine(config);
        ClassByteArraySource byteSource = new FileClassByteArraySource(srcDir);
        Mutater mutater = engine.createMutator(byteSource);

        List<String> classNames = findClassNames(srcDir);
        for (String name:classNames) {
            ClassName className = ClassName.fromString(name);
            List<MutationDetails> mutations = mutater.findMutations(className);
            for (int i = 0; i < mutations.size(); i++) {
                MutationDetails mutation = mutations.get(i);
                MutationIdentifier id = mutation.getId();
                byte[] mutatedClassByteCode = mutater.getMutation(id).getBytes();

                String mutatedClassName = classConversionPattern
                    .replace("$n", name)
                    .replace("$i", "" + (i + 1));

                File mutatedClassFile = new File(destDir, mutatedClassName);
                Files.write(mutatedClassFile.toPath(), mutatedClassByteCode);
                System.out.println("File written: " + mutatedClassFile);
            }
        }
    }

    static List<String> findClassNames(File srcDir) throws IOException {
        try (Stream<Path> stream = Files.walk(srcDir.toPath())) {
            return stream
                .filter(Files::isRegularFile)
                .filter(f -> f.toString().endsWith(".class"))
                .map(f -> f.toString().replace("/","."))
                .collect(Collectors.toList());
        }
    }
}
