package org.honton.chas.touch.maven.plugin;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Change modification time of specified files.
 */
@Mojo(name = "touch", threadSafe = true)
public class TouchMojo extends AbstractMojo {

  private static final Pattern IS_POSITIVE_INTEGER = Pattern.compile("\\d+");
  /**
   * Skip touching
   */
  @Parameter(property = "touch.skip", defaultValue = "false")
  private boolean skipTouch;
  /**
   * Modification timestamp to use. If numeric, the number of seconds in the Unix Epoch. If
   * non-numeric, an ISO8601 timestamp.
   */
  @Parameter(property = "touch.time", required = true)
  private String modificationTime;
  /**
   * A <code>fileSet</code> selecting files and directories to touch.
   */
  @Parameter(required = true)
  private FileSet files;

  private static FileAttribute<?>[] getPosixAttrs(FileSystem fileSystem, String mode) {
    if (fileSystem.supportedFileAttributeViews().contains("posix")) {
      return new FileAttribute[] {PosixAttributes.getFilePermission(mode)};
    } else {
      return new FileAttribute[0];
    }
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipTouch) {
      getLog().info("skipping touch");
      return;
    }
    FileTime fileTime = getEpochTime();
    try {
      Path root = getRootPath(files);

      FileSystem fileSystem = root.getFileSystem();
      FileAttribute<?>[] dirAttrs = getPosixAttrs(fileSystem, files.getDirectoryMode());
      FileAttribute<?>[] fileAttrs = getPosixAttrs(fileSystem, files.getFileMode());

      if (files.getDirectory() != null) {
        Files.createDirectories(root, dirAttrs);
      }

      new Scanner(files, root).walkTree(path -> {
        if (Files.exists(path)) {
          Files.setLastModifiedTime(path, fileTime);
        } else {
          Path parent = path.getParent();
          if (parent == null) {
            getLog().info("No parent for " + path);
          } else {
            Files.createDirectories(path.getParent(), dirAttrs);
          }
          Files.createFile(path, fileAttrs);
          Files.setLastModifiedTime(path, fileTime);
        }
      });
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private static Path getRootPath(FileSet files) {
    String dir = files.getDirectory();
    return dir == null
        ? Paths.get(System.getProperty("user.dir"))
        : Paths.get(dir).toAbsolutePath().normalize();
  }

  FileTime getEpochTime() {
    if (IS_POSITIVE_INTEGER.matcher(modificationTime).matches()) {
      return FileTime.from(Long.parseLong(modificationTime), TimeUnit.SECONDS);
    }
    return FileTime.from(ZonedDateTime.parse(modificationTime).toInstant());
  }
}
