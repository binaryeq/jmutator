package binaryeq.jmutator;

import org.pitest.classinfo.ClassByteArraySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Read class definition from a .class file.
 * @author jens dietrich
 */
public class FileClassByteArraySource implements ClassByteArraySource {

    private File folder = null;
    public FileClassByteArraySource(File folder) {
        this.folder = folder;
    }

    @Override
    public Optional<byte[]> getBytes(String className) {
        System.err.println("FileClassByteArraySource.getBytes(" + className + ") called.");     //DEBUG
        File classFile = new File(folder,className.replace('.','/')+".class");
        if (!classFile.exists()) {
            throw new IllegalArgumentException("Class not found in " + folder.getAbsolutePath() + ": " + className);
        }
        try {
            return Optional.ofNullable(Files.readAllBytes(classFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



