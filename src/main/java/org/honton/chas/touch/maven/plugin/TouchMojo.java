package org.honton.chas.touch.maven.plugin;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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

/** Change modification time of specified files. */
@Mojo(name = "touch", threadSafe = true)
public class TouchMojo extends AbstractMojo {

  /** Skip touching */
  @Parameter(property = "touch.skip", defaultValue = "false")
  private boolean skipTouch;

  /**
   * Modification timestamp to use. If numeric, the number of seconds in the Unix Epoch. If
   * non-numeric, an ISO8601 timestamp.
   */
  @Parameter(property = "touch.time", required = true)
  private String modificationTime;

  /** A <code>fileSet</code> selecting files and directories to touch. */
  @Parameter(required = true)
  private FileSet files;

  private static final Pattern IS_POSITIVE_INTEGER = Pattern.compile("\\d+");

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipTouch) {
      getLog().info("skipping touch");
      return;
    }
    FileTime fileTime = getEpochTime();
    try {
      FileAttribute<?>[] dirAttrs = getPosixAttrs(files.getDirectoryMode());
      FileAttribute<?>[] fileAttrs = getPosixAttrs(files.getFileMode());

      new Scanner(files)
          .walkTree(
              path -> {
                if (Files.exists(path)) {
                  Files.setLastModifiedTime(path, fileTime);
                } else {
                  Files.createDirectories(path.getParent(), dirAttrs);
                  Files.createFile(path, fileAttrs);
                }
              });
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private FileAttribute<?>[] getPosixAttrs(String mode) {
    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      return new FileAttribute[] {PosixAttributes.getFilePermission(mode)};
    } else {
      return new FileAttribute[0];
    }
  }

  FileTime getEpochTime() {
    if (IS_POSITIVE_INTEGER.matcher(modificationTime).matches()) {
      return FileTime.from(Long.parseLong(modificationTime), TimeUnit.SECONDS);
    }
    return FileTime.from(ZonedDateTime.parse(modificationTime).toInstant());
  }
}
