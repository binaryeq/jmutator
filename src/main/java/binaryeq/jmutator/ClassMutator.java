package binaryeq.jmutator;

import com.google.common.base.Preconditions;
import org.json.JSONObject;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mutate all class files within a folder.
 * @author jens dietrich
 */
public class ClassMutator {

    // separate method to facilitate unit testing
    static void mutateClassFiles(File binDir, File mutatedBinDir, String classConversionPattern, String provenanceInfoConversionPattern, boolean verify, Collection<MethodMutatorFactory> mutators, Predicate<MutationDetails> shouldKeepMutation) throws IOException {

        Preconditions.checkArgument(binDir.exists(), "bin folder does not exist: " + binDir.getAbsolutePath());
        Preconditions.checkArgument(binDir.isDirectory(), "bin folder does not a directory: " + binDir.getAbsolutePath());

        Preconditions.checkArgument(mutatedBinDir.exists(), "destination folder for mutated bins does not exist: " + mutatedBinDir.getAbsolutePath());
        Preconditions.checkArgument(mutatedBinDir.isDirectory(), "destination folder for mutated bins does not a directory: " + mutatedBinDir.getAbsolutePath());

        Preconditions.checkArgument(classConversionPattern.contains("$n"), "class conversion pattern does not contains $n -- the class name");
        Preconditions.checkArgument(classConversionPattern.contains("$i"), "class conversion pattern does not contains $i -- the mutation id");

        Preconditions.checkArgument(provenanceInfoConversionPattern.contains("$n"), "provenance info conversion pattern does not contains $n -- the class name");
        Preconditions.checkArgument(provenanceInfoConversionPattern.contains("$i"), "provenance info conversion pattern does not contains $i -- the mutation id");


        System.out.println("Using " + mutators.size() + " mutators:");
        for (MethodMutatorFactory mutator : mutators) {
            System.out.println("    " + mutator.getName());
        }

        final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(i -> true, mutators);
        MutationEngine engine = new GregorMutationEngine(config);
        ClassByteArraySource byteSource = new FileClassByteArraySource(binDir);
        Mutater mutater = engine.createMutator(byteSource);

        List<String> classNames = findClassNames(binDir);
        for (String name:classNames) {
            ClassName className = ClassName.fromString(name);
            List<MutationDetails> mutations = mutater.findMutations(className);
            int iSuccessfulMutation = 0;
            for (int i = 0; i < mutations.size(); i++) {
                MutationDetails mutation = mutations.get(i);
                MutationIdentifier id = mutation.getId();

                String mutatedClassName = instantiateTemplate(classConversionPattern, name, iSuccessfulMutation);
                File mutatedClassFile = new File(mutatedBinDir, mutatedClassName);

                if (shouldKeepMutation.test(mutation)) {
                    byte[] mutatedClassByteCode = mutater.getMutation(id).getBytes();

                    boolean bytecodeVerified = true;
                    if (verify) {
                        bytecodeVerified = Verifier.check(mutatedClassByteCode);
                    }

                    if (bytecodeVerified) {
                        mutatedClassFile.getParentFile().mkdirs();
                        Files.write(mutatedClassFile.toPath(), mutatedClassByteCode);
                        System.out.println("mutated class file written: " + mutatedClassFile);

                        String provenanceInfoFileName = instantiateTemplate(provenanceInfoConversionPattern, name, iSuccessfulMutation);
                        File provenanceInfoFile = new File(mutatedBinDir, provenanceInfoFileName);
                        provenanceInfoFile.getParentFile().mkdirs();

                        JSONObject json = serializeMutationDetails(mutation);
                        try (FileWriter out = new FileWriter(provenanceInfoFile)) {
                            String prettyPrinted = json.toString(4);
                            out.write(prettyPrinted);
                        }
                        System.out.println("provenance info file written: " + provenanceInfoFile);
                        ++iSuccessfulMutation;
                    } else {
                        System.out.println("verification failed for generated bytecode for " + mutatedClassFile + ", result not written");
                    }
                } else {
                    System.out.println("Decided not to keep raw mutation " + i + " for class " + className + " (would have been " + mutatedClassFile + ")");
                }
            }
        }
    }

    static String instantiateTemplate(String template, String name, int index) {
        return template
            .replace("$n", name.replace('.','/'))
            .replace("$i", "" + (index + 1));
    }

    static JSONObject serializeMutationDetails(MutationDetails mutation) {
        JSONObject json = new JSONObject();
        json.put("mutator", mutation.getMutator());
        json.put("description", mutation.getDescription());

        JSONObject location = new JSONObject();
        location.put("class",mutation.getId().getLocation().getClassName());
        location.put("method-name",mutation.getId().getLocation().getMethodName());
        location.put("method-descriptor",mutation.getId().getLocation().getMethodDesc());
        location.put("line", mutation.getLineNumber());
        json.put("location",location);

        return json;
    }

    static List<String> findClassNames(File binDir) throws IOException {
        try (Stream<Path> stream = Files.walk(binDir.toPath())) {
            return stream
                .filter(Files::isRegularFile)
                .filter(f -> f.toString().endsWith(".class"))
                .map(f -> {
                    Path f2 = binDir.toPath().relativize(f);
                    String s = f2.toString().replace('/','.');
                    return s.substring(0,s.length()-".class".length());
                })
                .collect(Collectors.toList());
        }
    }
}
