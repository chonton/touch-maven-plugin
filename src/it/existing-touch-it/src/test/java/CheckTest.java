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
    Path file = pwd.getParent().getParent().getParent().resolve("src/it/existing-touch-it/pom.xml");
    Assertions.assertTrue(Files.exists(file));
    FileTime actual = Files.getLastModifiedTime(file);
    FileTime expected = FileTime.from(Instant.EPOCH);
    long diff = Math.abs(expected.toInstant().getEpochSecond() - actual.toInstant().getEpochSecond());
    Assertions.assertTrue(diff < 3);
  }
}