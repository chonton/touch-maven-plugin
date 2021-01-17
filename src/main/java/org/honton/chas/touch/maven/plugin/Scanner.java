package org.honton.chas.touch.maven.plugin;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.maven.shared.model.fileset.FileSet;

public class Scanner {
  private final FileSet files;
  private final Path rootPath;
  private final FileSystem fileSystem;
  private final Set<Path> concretePaths = new HashSet<>();
  private final Set<PathMatcher> includePatterns = new HashSet<>();
  private final Set<PathMatcher> excludePatterns;

  public Scanner(FileSet files) throws IOException {
    this.files = files;
    rootPath = getAbsoluteRootPath(files);
    fileSystem = rootPath.getFileSystem();

    excludePatterns =
        files.getExcludes().stream()
            .map(exclude -> fileSystem.getPathMatcher("glob:" + exclude))
            .collect(Collectors.toSet());

    files
        .getIncludes()
        .forEach(
            include -> {
              if (isConcrete(include)) {
                Path includePath = Path.of(include);
                if (!anyMatch(excludePatterns, includePath)) {
                  concretePaths.add(includePath);
                }
              } else {
                includePatterns.add(fileSystem.getPathMatcher("glob:" + include));
              }
            });

    if (files.getIncludes().isEmpty()) {
      includePatterns.add(fileSystem.getPathMatcher("glob:**"));
    }
  }

  private static Path getAbsoluteRootPath(FileSet files) throws IOException {
    String dir = files.getDirectory();
    return Paths.get(dir != null ? dir : System.getProperty("user.dir")).toRealPath();
  }

  static boolean isConcrete(String path) {
    return path.indexOf('*') < 0
        && path.indexOf('?') < 0
        && path.indexOf('[') < 0
        && path.indexOf('{') < 0;
  }

  @FunctionalInterface
  public interface PathConsumer {
    void accept(Path path) throws IOException;
  }

  public void walkTree(PathConsumer consumer) throws IOException {
    Set<Path> toVisit = new HashSet<>(concretePaths);
    Files.walkFileTree(
        rootPath,
        files.isFollowSymlinks() ? Set.of(FileVisitOption.FOLLOW_LINKS) : Set.of(),
        Integer.MAX_VALUE,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
              throws IOException {
            toVisit.remove(path);
            if (anyMatch(includePatterns, path) && !anyMatch(excludePatterns, path)) {
              consumer.accept(path);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
          }
        });
    toVisit.forEach(p -> ignoreIOException(consumer, p));
  }

  @SneakyThrows
  private void ignoreIOException(PathConsumer consumer, Path p) {
    consumer.accept(p);
  }

  private boolean anyMatch(Set<PathMatcher> pathMatchers, Path path) {
    return pathMatchers.stream().anyMatch(e -> e.matches(path));
  }
}
