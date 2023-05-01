# Spring Boot Configuration Documentation Maven Plugin 

This maven plugin is intended to convert spring boot's `spring-configuration-metadata.json` used by IDEs into actual documentation. 

The purpose of doing so is for developers to easily distribute their configurable application properties to those who use their product, without duplicating descriptions already written in `@ConfigurationProperties` classes.

## Quickstart

You can easily generate the documentation for your project with one command:
```shell
mvn compile io.github.cjoseflores:spring-boot-config-doc-maven-plugin:generate-documentation
```
This generates a markdown file under `${build.project.directory}`. Note that this also assumes your `spring-configuration-metadata.json` is located in `${project.build.outputDirectory}/META-INF`.

To point to a different input file, use the `-DmetadataDirectory` flag, or to output to a different directory, use the `-DoutputDirectory` flag.

**Example**:
```shell
mvn compile io.github.cjoseflores:spring-boot-config-doc-maven-plugin:generate-documentation -DmetadataDirectory=./metadata -DoutputDirectory=./docs
```

The above example generates markdown in the `./docs` folder from the file `./metadata/spring-configuration-metadata.json`.

## Setup

In your Maven project, add the following to your `pom.xml`:
```xml
<project>
    ...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>io.github.cjoseflores</groupId>
                <artifactId>spring-boot-config-doc-maven-plugin</artifactId>
                <version>${version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-documentation</goal>
                        </goals>
                        <phase>compile</phase>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
    </build>
    ...
</project>
```

## Configuration
The following table describes all configuration options:
|Field|Type|Default|Description|
|-|-|-|-|
|metadataDirectory|string|`${project.build.outputDirectory}/META-INF`|Location of the 'spring-configuration-metadata' file.|
|metadataFileName|string|`spring-configuration-metadata.json`|The name of the 'spring-configuration-metadata' file.|
|outputDirectory|string|`${project.build.directory}`|Location to place the generated markdown file.|
|generatedFileName|string|`${project.artifactId}-spring-properties.md`|The name of the generated markdown file.|
|generatedDocumentationHeader|string|`${project.artifactId} Spring Properties`|The name value for the documentation header (# in markdown).|
|failOnError|boolean|`true`|Whether to fail the build if any errors occur during plugin execution. *(Note: When `failBuildOnMissingMetadata` is set to false, this property is currently overridden to false)*|
|failBuildOnMissingMetadata|boolean|`true`|**Deprecated, use `failOnError` instead**. Whether to fail the build if the 'spring-configuration-metadata' file cannot be loaded, or does not exist. *(Note: When `failOnError` is set to false, this property is now overridden to false)*|

## Artifact Publication
All artifacts are published to maven central.