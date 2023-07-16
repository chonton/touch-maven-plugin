package org.honton.chas.touch.maven.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScannerTest {

  private static final String SRC_MAIN = "src/main/java/org/honton/chas/touch/maven/plugin";
  private static final String SRC_TEST = "src/test/java/org/honton/chas/touch/maven/plugin";

  @Test
  void isConcrete() {
    Assertions.assertTrue(Scanner.isConcrete("ringo"));
    Assertions.assertFalse(Scanner.isConcrete("ringo.*"));
    Assertions.assertFalse(Scanner.isConcrete("[grl]ingo"));
    Assertions.assertFalse(Scanner.isConcrete("in.{html,htm}"));
  }

  private Set<Path> walkFileSet(List<String> includes, List<String> excludes) throws IOException {
    FileSet files = new FileSet();
    files.setIncludes(includes);
    files.setExcludes(excludes);

    Set<Path> actual = new HashSet<>();
    new Scanner(files, Path.of(System.getProperty("user.dir"))).walkTree(actual::add);
    return actual;
  }

  @Test
  void walkAll() throws IOException {
    Assertions.assertFalse(walkFileSet(List.of(), List.of()).isEmpty());
  }

  @Test
  void walkJava() throws IOException {
    Set<Path> actual = walkFileSet(List.of("src/**/*.java"), List.of("src/it/**"));
    Set<Path> expected =
        Set.of(
            getPath(SRC_MAIN, "PosixAttributes.java"),
            getPath(SRC_MAIN, "Scanner.java"),
            getPath(SRC_MAIN, "TouchMojo.java"),
            getPath(SRC_TEST, "PosixAttributesTest.java"),
            getPath(SRC_TEST, "ScannerTest.java"));
    Assertions.assertEquals(expected, actual);
  }

  private Path getPath(String dir, String file) {
    return Path.of(dir, file).toAbsolutePath();
  }

  @Test
  void concreteMatch() throws IOException {
    String scannerTest = "src/test/java/org/honton/chas/touch/maven/plugin/ScannerTest.java";
    Set<Path> actual = walkFileSet(List.of(scannerTest), List.of());
    Set<Path> expected = Set.of(Path.of(System.getProperty("user.dir"), scannerTest));
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void concreteMiss() throws IOException {
    String missing = "src/NotThere";
    Set<Path> actual = walkFileSet(List.of(missing), List.of());
    Set<Path> expected = Set.of(Path.of(System.getProperty("user.dir"), missing));
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void excludeConcrete() throws IOException {
    Set<Path> actual = walkFileSet(List.of("src/not/there"), List.of("src/**"));
    Assertions.assertEquals(Set.of(), actual);
  }
}
