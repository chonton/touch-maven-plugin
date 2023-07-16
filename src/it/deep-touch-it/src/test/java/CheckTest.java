import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CheckTest {

  @Test
  void deepTouch() throws IOException {
    Path pwd = Path.of("").toAbsolutePath();
    Path file = pwd.resolve("target/deep/touch");
    Assertions.assertTrue(Files.exists(file));

    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      Set<PosixFilePermission> expected =
          EnumSet.of(
              PosixFilePermission.OWNER_READ,
              PosixFilePermission.OWNER_WRITE,
              PosixFilePermission.GROUP_READ,
              PosixFilePermission.OTHERS_READ);

      Set<PosixFilePermission> actual = Files.getPosixFilePermissions(file);
      Assertions.assertEquals(expected, actual);

      expected =
          EnumSet.of(
              PosixFilePermission.OWNER_READ,
              PosixFilePermission.OWNER_WRITE,
              PosixFilePermission.OWNER_EXECUTE,
              PosixFilePermission.GROUP_READ,
              PosixFilePermission.GROUP_EXECUTE,
              PosixFilePermission.OTHERS_READ,
              PosixFilePermission.OTHERS_EXECUTE);

      actual = Files.getPosixFilePermissions(file.getParent());
      Assertions.assertEquals(expected, actual);
    }
  }
}