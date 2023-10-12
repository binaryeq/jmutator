package binaryeq.jmutator;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ClassMutatorTest {

    private static File mutatedBinDir = null;
    private static File binDir = null;

    private static String mutatedClassNameTemplate = "$n-$i.class";
    private static String provenanceInfoNameTemplate = "$n-$i.json";

    @BeforeAll
    public static void setupTmp() {
        mutatedBinDir = new File(".tmp");
        if (!mutatedBinDir.exists()) {
            mutatedBinDir.mkdirs();
        }
        binDir = new File(ClassMutatorTest.class.getResource("/testproject/target/classes").getFile());
    }

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(mutatedBinDir.exists());
        Assumptions.assumeTrue(binDir.exists());
        for (File f:mutatedBinDir.listFiles()) {
            f.delete();
        }
    }


    @Test
    public void testMutations () throws IOException {
        ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,provenanceInfoNameTemplate,false);
        String className = "com.foo.PrimeNumberChecker";
        // files exist
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,0)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,1)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,2)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,3)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,4)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(mutatedClassNameTemplate,className,5)).exists());
    }

    @Test
    public void testValidityOfValidBytecode1 () throws IOException {
        ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,provenanceInfoNameTemplate,true);
        List<File> mutatedClassFiles = Files.walk(mutatedBinDir.toPath())
            .filter(Files::isRegularFile)
            .filter(f -> f.toString().endsWith(".class"))
            .map(p -> p.toFile())
            .collect(Collectors.toList());

        Assumptions.assumeFalse(mutatedClassFiles.isEmpty());

        // done via API option
    }

    @Test
    public void testValidityOfValidBytecode2 () throws IOException {
        ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,provenanceInfoNameTemplate,false);
        List<File> mutatedClassFiles = Files.walk(mutatedBinDir.toPath())
            .filter(Files::isRegularFile)
            .filter(f -> f.toString().endsWith(".class"))
            .map(p -> p.toFile())
            .collect(Collectors.toList());

        Assumptions.assumeFalse(mutatedClassFiles.isEmpty());
        for (File mutatedClassFile:mutatedClassFiles) {
            assertTrue(Verifier.check(mutatedClassFile));
        }
    }

    @Test
    public void testMutationProvenance () throws IOException {
        ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,provenanceInfoNameTemplate,false);
        String className = "com.foo.PrimeNumberChecker";
        // files exist
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,0)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,1)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,2)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,3)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,4)).exists());
        assertTrue(new File(mutatedBinDir,ClassMutator.instantiateTemplate(provenanceInfoNameTemplate,className,5)).exists());
    }

    @Test
    public void testFindClassNames () throws IOException {
        List<String> classes = ClassMutator.findClassNames(binDir);
        assertEquals(1,classes.size());
        assertTrue(classes.contains("com.foo.PrimeNumberChecker"));
    }

    @Test
    public void testMutatedClassNameTemplate1 () {
        // $n missing
        assertThrows(IllegalArgumentException.class, () -> ClassMutator.mutateClassFiles(binDir,mutatedBinDir,"foo-$i.class",provenanceInfoNameTemplate,false));
    }

    @Test
    public void testMutatedClassNameTemplate2 () {
        // $i missing
        assertThrows(IllegalArgumentException.class, () -> ClassMutator.mutateClassFiles(binDir,mutatedBinDir,"foo-$n.class",provenanceInfoNameTemplate,false));
    }

    @Test
    public void testMutationProvenanceFileNameTemplate1 () {
        // $n missing
        assertThrows(IllegalArgumentException.class, () -> ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,"foo-$i.json",false));
    }

    @Test
    public void testMutationProvenanceFileNameTemplate2 () {
        // $i missing
        assertThrows(IllegalArgumentException.class, () -> ClassMutator.mutateClassFiles(binDir,mutatedBinDir,mutatedClassNameTemplate,"foo-$n.json",false));
    }

}
