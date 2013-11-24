package uk.co.tobybatch.minecraftmanager;

/**
 * Sample code that finds files that match the specified glob pattern. For more
 * information on what constitutes a glob pattern, see
 * http://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
 *
 * The file or directories that match the pattern are printed to standard out.
 * The number of matches is also printed.
 *
 * When executing this application, you must put the glob pattern in quotes, so
 * the shell will not expand any wild cards: java Find . -name "*.java"
 */
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.util.*;

public class Finder
        extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private int numMatches = 0;

    private Vector files;

    Finder(String pattern) {
        this.files = new Vector();
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    public static String[] find(Path startingDir,  String pattern)
            throws IOException {
        Finder finder = new Finder(pattern);
        Files.walkFileTree(startingDir, finder);
        return finder.list();
    }
    
    String [] list() {
        String _list[] = new String[this.files.size()];
        
        ListIterator list = this.files.listIterator();
        int count = 0;
        while (list.hasNext()) {
            _list[count++] = list.next().toString();
        }
        
        return _list;
    }
    
        // Compares the glob pattern against
    // the file or directory name.
    void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(name)) {
            numMatches++;
            this.files.add(file);
        }
    }

        // Prints the total number of
    // matches to standard out.
    void done() {
        ListIterator list = this.files.listIterator();
        while (list.hasNext()) {
            System.out.println(list.next());
        }
        System.out.println("Matched: " + numMatches);
    }

        // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

        // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}
