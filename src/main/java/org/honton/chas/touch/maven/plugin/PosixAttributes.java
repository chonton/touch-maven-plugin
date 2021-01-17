package org.honton.chas.touch.maven.plugin;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PosixAttributes {

  public static FileAttribute<Set<PosixFilePermission>> getFilePermission(String mode) {
    return PosixFilePermissions.asFileAttribute(toPosixSet(mode));
  }

  public static Set<PosixFilePermission> toPosixSet(String mode) {
    return toPosixSet(Integer.parseInt(mode, mode.charAt(0)=='0' ?8 :10));
  }

  public static Set<PosixFilePermission> toPosixSet(int mode) {
    Set<PosixFilePermission> set = EnumSet.noneOf(PosixFilePermission.class);

    // user, group, others (read, write, execute)
    int mask = 0b100_000_000;
    for (PosixFilePermission permission : PosixFilePermission.values()) {
      if ((mode & mask) != 0) {
        set.add(permission);
      }
      mask >>= 1;
    }

    return set;
  }
}
