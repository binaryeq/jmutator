package binaryeq.jmutator;

import org.pitest.classinfo.ClassByteArraySource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Read class definition from a .class file inside a jar or zip.
 * @author jens dietrich
 */
public class JarByteArraySource implements ClassByteArraySource {

    private File jar = null;
    public JarByteArraySource(File jar) {
        this.jar = jar;
    }

    @Override
    public Optional<byte[]> getBytes(String className) {
        try {
            ZipFile zip = new ZipFile(jar);
            ZipEntry zipEntry = zip.getEntry(className.replace('.','/')+".class");
            if (zipEntry==null) {
                throw new IllegalArgumentException("Class not found in " + jar.getAbsolutePath() + ": " + className);
            }
            InputStream in = zip.getInputStream(zipEntry);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return Optional.of(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



