package binaryeq.jmutator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import java.io.*;

/**
 * Check the validity of mutated bytecode.
 * Based on ASM, see: https://asm.ow2.io/javadoc/org/objectweb/asm/util/CheckClassAdapter.html
 * @author jens dietrich
 */
public class Verifier {

    public static boolean check(File classFile) throws IOException {
        System.out.println("verifying bytecode in " + classFile);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        try (InputStream in = new FileInputStream(classFile)) {
            CheckClassAdapter.verify(new ClassReader(in), false, printWriter);
        }

        if (!stringWriter.toString().isEmpty()) {
            System.out.println("bytecode verification failed for " + classFile + " , details:");
            System.out.println(stringWriter.toString());
        }
        return stringWriter.toString().isEmpty();
    }

    public static boolean check(byte[] bytes) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            CheckClassAdapter.verify(new ClassReader(in), false, printWriter);
        }

        if (!stringWriter.toString().isEmpty()) {
            System.out.println(stringWriter.toString());
        }
        return stringWriter.toString().isEmpty();
    }


}
