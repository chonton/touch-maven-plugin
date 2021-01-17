package org.honton.chas.touch.maven.plugin;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PosixAttributesTest {

  @Test
  void standardDirectoryPermissions() {
    Assertions.assertEquals(
        EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE),
        PosixAttributes.toPosixSet("0755"));
  }

  @Test
  void standardFilePermissions() {
    Assertions.assertEquals(
        EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.OTHERS_READ),
        PosixAttributes.toPosixSet("0644"));
  }
}
