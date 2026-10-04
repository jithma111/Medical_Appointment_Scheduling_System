package repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Small helper used by every repository to read/write the .txt files in the "data" folder. */
public final class FileStore {
    private static final Path DIR = Paths.get("data");

    private FileStore() { }

    public static synchronized List<String> read(String file) {
        try {
            Path p = DIR.resolve(file);
            if (!Files.exists(p)) return new ArrayList<>();
            List<String> out = new ArrayList<>();
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8))
                if (!line.trim().isEmpty()) out.add(line);
            return out;
        } catch (IOException e) { throw new RuntimeException("Cannot read " + file, e); }
    }

    public static synchronized void write(String file, List<String> lines) {
        try {
            Files.createDirectories(DIR);
            Files.write(DIR.resolve(file), lines, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new RuntimeException("Cannot write " + file, e); }
    }

    /** nextId("P", ["P001","P004"]) -> "P005" */
    public static String nextId(String prefix, List<String> ids) {
        int max = 0;
        for (String id : ids) {
            try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); }
            catch (RuntimeException ignored) { }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    /** Removes characters that would break the file format. */
    public static String clean(String s) {
        return s == null ? "" : s.replaceAll("[|;~\\r\\n]", " ").trim();
    }
}
