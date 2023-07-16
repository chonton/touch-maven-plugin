package org.honton.chas.touch.maven.plugin;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

/** Change modification time of specified files. */
@Mojo(name = "touch", threadSafe = true)
public class TouchMojo extends AbstractMojo {

  private static final Pattern IS_POSITIVE_INTEGER = Pattern.compile("\\d+");
  /** Skip touching */
  @Parameter(property = "touch.skip", defaultValue = "false")
  private boolean skipTouch;

  /**
   * Modification timestamp to use. If numeric, the number of seconds in the Unix Epoch. If
   * non-numeric, an ISO8601 timestamp.
   */
  @Parameter(property = "touch.time")
  private String modificationTime;

  /** A <code>fileSet</code> selecting files and directories to touch. */
  @Parameter(required = true)
  private FileSet files;

  private static Set<PosixFilePermission> getPosixAttrs(FileSystem fileSystem, String mode) {
    if (fileSystem.supportedFileAttributeViews().contains("posix")) {
      return PosixAttributes.getFilePermission(mode);
    } else {
      return Set.of();
    }
  }

  private static Path getRootPath(FileSet files) {
    String dir = files.getDirectory();
    return dir == null
        ? Paths.get(System.getProperty("user.dir"))
        : Paths.get(dir).toAbsolutePath().normalize();
  }

  private void createDirs(Path path, Set<PosixFilePermission> dirAttrs) throws IOException {
    if (Files.notExists(path)) {
      createDirs(path.getParent(), dirAttrs);

      if (getLog().isDebugEnabled()) {
        getLog()
            .debug(
                "Creating "
                    + path
                    + " with permissions "
                    + PosixFilePermissions.toString(dirAttrs));
      }
      Files.createDirectory(path);
      if (!dirAttrs.isEmpty()) {
        Files.setPosixFilePermissions(path, dirAttrs);
      }
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
      Set<PosixFilePermission> dirAttrs = getPosixAttrs(fileSystem, files.getDirectoryMode());
      Set<PosixFilePermission> fileAttrs = getPosixAttrs(fileSystem, files.getFileMode());

      if (files.getDirectory() != null) {
        createDirs(root, dirAttrs);
      }

      new Scanner(files, root).walkTree(path -> touch(path, fileTime, dirAttrs, fileAttrs));
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void touch(
      Path path,
      FileTime fileTime,
      Set<PosixFilePermission> dirAttrs,
      Set<PosixFilePermission> fileAttrs) {
    try {
      if (Files.exists(path)) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("Setting modified time of " + path + " to " + fileTime);
        }
      } else {
        Path parent = path.getParent();
        if (parent == null) {
          getLog().info("No parent for " + path);
        } else {
          createDirs(path.getParent(), dirAttrs);
        }
        if (getLog().isDebugEnabled()) {
          getLog()
              .debug(
                  "Creating "
                      + path
                      + " with permissions "
                      + PosixFilePermissions.toString(fileAttrs)
                      + " and time "
                      + fileTime);
        }
        Files.createFile(path);
        if (!fileAttrs.isEmpty()) {
          Files.setPosixFilePermissions(path, fileAttrs);
        }
      }
      Files.setLastModifiedTime(path, fileTime);
    } catch (IOException e) {
      getLog().warn(e.getMessage());
    }
  }

  FileTime getEpochTime() {
    if (modificationTime == null) {
      return FileTime.from(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    if (IS_POSITIVE_INTEGER.matcher(modificationTime).matches()) {
      return FileTime.from(Long.parseLong(modificationTime), TimeUnit.SECONDS);
    }
    return FileTime.from(ZonedDateTime.parse(modificationTime).toInstant());
  }
}
