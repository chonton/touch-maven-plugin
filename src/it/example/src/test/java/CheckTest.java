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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

class CheckTest {

  @Test
  void deepTouch() throws IOException {
    Path pwd = Path.of("").toAbsolutePath();
    Path file = pwd.resolve("path/to/create/touched");
    Assertions.assertTrue(Files.exists(file));
    Assertions.assertEquals(Files.getLastModifiedTime(file), FileTime.from(Instant.EPOCH));
  }
}