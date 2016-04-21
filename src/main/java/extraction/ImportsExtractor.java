package extraction;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportsExtractor {
    static final int CLASS = 7;
    static final int FIELD = 9;
    static final int METH = 10;
    static final int IMETH = 11;
    static final int STR = 8;
    static final int INT = 3;
    static final int FLOAT = 4;
    static final int LONG = 5;
    static final int DOUBLE = 6;
    static final int NAME_TYPE = 12;
    static final int UTF8 = 1;
    static final int MTYPE = 16;
    static final int HANDLE = 15;
    static final int INDY = 18;

    private final PrintStream out;
    private byte[] classBuf = new byte[1024];

    HashedStringCounter hashTable = new HashedStringCounter();
    boolean classesOnly;

    public ImportsExtractor(PrintStream out, boolean classesOnly) {
        this.out = out;
        this.classesOnly = classesOnly;
    }

    public static class ExtractImportVisitor extends SimpleFileVisitor<Path> {
        final ImportsExtractor extractor;
        final LastMavenVersionDetector detector;
        int nJars;

        public ExtractImportVisitor(ImportsExtractor extractor, LastMavenVersionDetector detector) {
            this.extractor = extractor;
            this.detector = detector;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                throws IOException {
            if (path.getFileName().toString().endsWith(".jar")) {
                File file = path.toFile();
                if (detector == null || detector.isLastVersion(file.getParentFile())) {
                    nJars++;
                    extractor.parseJar(file);
                }
            }

            return super.visitFile(path, attrs);
        }
    }

    public static void main(String[] args) {
        ImportsExtractor ex = new ImportsExtractor(System.out, false);

        try {
            long start = System.nanoTime();
            Path path;
            if (args.length == 0) {
                path = Paths.get(System.getenv("HOME"), ".m2", "repository");
                System.out.println("Taking .m2 home as base scanning path");
            } else {
                path = Paths.get(args[0]);
            }

            if (!Files.exists(path)) {
                System.out.println("Provide existing path with JARs as first argument");
                System.out.println("e.g.: java extraction.ImportsExtractor path");
                return;
            }
            System.out.println("Scanning " + path);
            LastMavenVersionDetector detector = new LastMavenVersionDetector();

            ExtractImportVisitor visitor = new ExtractImportVisitor(ex, detector);
            Files.walkFileTree(path, visitor);
            System.out.println("Read " + visitor.nJars + " JARs " + ex.hashTable.bucketsFilled + " imports found");
            ex.report();
            System.out.println("Time: " + (System.nanoTime() - start) / 1e9 + " s");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void report() {
        TopReporter.to(System.out)
                   .onlyFirst(50)
                   .report(hashTable);

        String filename = "top-classes.txt";
        System.out.println("... (all results in " +  filename + ")");

        try (PrintStream out = new PrintStream(filename)) {
            TopReporter.to(out)
                       .onlyFirst(1000)
                       .report(hashTable);
        } catch (IOException ex) {
            System.err.println("Error writing result to " + filename + " : " + ex);
        }
    }

    public void parseJar(File jar) throws IOException {
        try (InputStream fileIn = new FileInputStream(jar);
                    BufferedInputStream bufIn = new BufferedInputStream(fileIn);
                    ZipInputStream zipIn = new ZipInputStream(bufIn)) {
            parseZip(zipIn);
        }
    }

    private void parseZip(ZipInputStream zipIn)
            throws IOException {
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.endsWith(".class")) {
                int off = 0, r;
                while ((r = zipIn.read(classBuf, off, classBuf.length - off)) > 0) {
                    off += r;
                    if (classBuf.length - off < 1024) {
                        classBuf = Arrays.copyOf(classBuf, classBuf.length * 2);
                    }
                }
                parseClassBytes(classBuf);
            }
            zipIn.closeEntry();
        }
    }


    private void parseClassBytes(byte[] buf) throws IOException {
        int nConstantPoolItems = readUnsignedShort(8, buf);
        int []constantPoolItems = new int[nConstantPoolItems];

        readConstantPoolItems(buf, constantPoolItems);
        countClassNames(buf, constantPoolItems);
    }

    private void countClassNames(byte[] buf, int[] constantPoolItems) {
        int nConstantPoolItems = constantPoolItems.length;
        for (int i = 1; i < nConstantPoolItems; i++) {
            int anyItemOff = constantPoolItems[i];
            if (anyItemOff == 0) {
                continue;
            }
            int constPoolItemType = buf[anyItemOff - 1];

            if (constPoolItemType == CLASS) {
                int nameItem = readUnsignedShort(anyItemOff, buf);

                int strOff = constantPoolItems[nameItem];
                int strType = buf[strOff - 1];
                if (strType == UTF8) {
                    int strLen = readUnsignedShort(strOff, buf);
                    strOff += 2;
                    if (buf[strOff] != '[') {
                        if (classesOnly) {
                            hashTable.add(new ByteString(buf, strOff, strLen));
                        } else {
                            for (int j = 0; j <= strLen; j++) {
                                if (j == strLen ||
                                    buf[strOff + j] == '/') {
                                    hashTable.add(new ByteString(buf, strOff, j));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void readConstantPoolItems(byte[] buf, int[] constantPoolItems) {
        int nConstantPoolItems = constantPoolItems.length;

        int ptr = 10;
        for (int i = 1; i < nConstantPoolItems; i++) {
            constantPoolItems[i] = ptr + 1;
            int size;
            int type = buf[ptr];
            switch (type) {
                case FIELD: case METH: case IMETH: case INT:
                case FLOAT: case NAME_TYPE: case INDY:
                    size = 5;
                    break;
                case LONG: case DOUBLE:
                    size = 9;
                    i++;
                    break;
                case UTF8:
                    size = 3 + readUnsignedShort(ptr + 1, buf);
                    break;
                case HANDLE:
                    size = 4;
                    break;
                default: // CLASS, STR, MTYPE
                    size = 3;
                    break;
            }
            ptr += size;
        }
    }

    private int readUnsignedShort(int offset, byte[] data) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }
}
