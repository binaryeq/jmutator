package binaryeq.jmutator;

import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Read class definition from a .class file.
 * @author jens dietrich
 */
public class FileClassByteArraySource implements ClassByteArraySource {
    private static final int CACHE_SIZE = 12;       // Copied from pitest's MutationTestMinion.java
    private File folder = null;
    private final ClassByteArraySource fallbackByteSource;

    public FileClassByteArraySource(File folder) {
        this.folder = folder;

        //HACK: Set up a fallback ClassByteArraySource to load bytecode for classes outside the bin folder.
        // Mimicks what pitest's own MutationTestMinion.java does. Fragile.
        final ClassLoader loader = IsolationUtils.getContextClassLoader();
        fallbackByteSource = new CachingByteArraySource(new ClassloaderByteArraySource(loader), CACHE_SIZE);
    }

    @Override
    public Optional<byte[]> getBytes(String className) {
        System.err.println("FileClassByteArraySource.getBytes(" + className + ") called.");     //DEBUG
        File classFile = new File(folder,className.replace('.','/')+".class");
        if (!classFile.exists()) {
//            throw new IllegalArgumentException("Class not found in " + folder.getAbsolutePath() + ": " + className);
            System.err.println("FileClassByteArraySource.getBytes(" + className + "): Class file " + classFile + " not present, so falling back.");
            return fallbackByteSource.getBytes(className);
        }
        try {
            return Optional.ofNullable(Files.readAllBytes(classFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



