# touch-maven-plugin
Create file or set file modification time.

# Plugin
Plugin reports available at [plugin info](https://chonton.github.io/touch-maven-plugin/plugin-info.html).

There is a single goal: [touch](https://chonton.github.io/touch-maven-plugin/touch-mojo.html),
which does not have a default bound phase.  This goal modifies the timestamp of all files matching
the [FileSet](https://maven.apache.org/shared/file-management/fileset.html) specification.  The 
FileSet includes and excludes support [posix style globs](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String))

Any FileSet include which is not a glob will force creation of the necessary directories and create 
a zero byte file.

## Configuration
| Parameter        | Property      | Default | Description                                                                                                       |
|------------------|---------------|---------|-------------------------------------------------------------------------------------------------------------------|
| skipTouch        | ${touch.skip} | false   | Skip modifying file timestamps                                                                                    |
| files            |               |         | The fileset to update last modification time                                                                      |
| modificationTime |               | now     | The timestamp. Either a positive integer of number of seconds since the Unix Epoch, or an ISO8601 zoned date time |

## Filset Defaults
The following attributes of FileSet are used.  Any attribute not in this table is ignored.

| Attribute      | Default     | Description                                                                                                          |
|----------------|-------------|----------------------------------------------------------------------------------------------------------------------|
| directory      | ${user.dir} | The root directory to walk                                                                                           |
| includes       | **          | The files to include                                                                                                 |
| excludes       |             | The files to exclude.  Exclude takes precedence over include                                                         |
| followSymlinks | false       | If true, evaluate soft links                                                                                         |
| fileMode       | 0644        | The [posix octal notation](https://en.wikipedia.org/wiki/File-system_permissions#Numeric_notation) for created files |
| directoryMode  | 0755        | The posix octal notation for created directories                                                                     |

# Examples

## Typical Use
```xml
  <build>
    <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.honton.chas</groupId>
            <artifactId>touch-maven-plugin</artifactId>
            <version>0.0.3</version>
          </plugin>
        </plugins>
    </pluginManagement>

    <plugins>
      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>touch-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>touch</goal>
            </goals>
            <phase>validate</phase>
          </execution>
        </executions>
        <configuration>
          <modificationTime>0</modificationTime>
          <files>
            <directory>some/relative/path</directory>
            <includes>
              <include>**/*</include>
            </includes>
            <excludes>
              <exclude>**/log.log</exclude>
            </excludes>
          </files>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

## Create a file
Create a file and its parent directories:
```xml
    <configuration>
      <modificationTime>${maven.build.timestamp}</modificationTime>
      <files>
        <directory>path/to/create</directory>
        <includes>
          <include>.gitignore</include>
        </includes>
      </files>
    </configuration>
```
