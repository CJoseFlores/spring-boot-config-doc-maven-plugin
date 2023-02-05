# Spring Boot Configuration Documentation Maven Plugin 

This maven plugin is intended to convert spring boot's `spring-configuration-metadata.json` used by IDEs into actual documentation. 

The purpose of doing so is for developers to easily distribute their configurable application properties to those who use their product, without duplicating descriptions already written in `@ConfigurationProperties` classes.

## Quickstart

You can easily generate the documentation for your project with one command:
```shell
mvn compile com.github.cjoseflores:spring-boot-config-doc-maven-plugin:generate-documentation
```
This generates a markdown file under `${build.project.directory}`. Note that this also assumes your `spring-configuration-metadata.json` is located in `${project.build.outputDirectory}/META-INF`. To point to a different file, use the `-DmetadataDirectory` flag.

## Setup

In your Maven project, add the following to your `pom.xml`:
```xml
<project>
    ...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.github.cjoseflores</groupId>
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
Other than the phase and the markdown output directory, most defaults should be sufficient. Should you require it however, the following table describes all configuration options:
|Field|Type|Default|Description|
|-|-|-|-|
|metadataDirectory|string|`${project.build.outputDirectory}/META-INF`|Location of the 'spring-configuration-metadata' file.|
|metadataFileName|string|`spring-configuration-metadata.json`|The name of the 'spring-configuration-metadata' file.|
|outputDirectory|string|`${project.build.directory}`|Location to place the generated markdown file.|
|generatedFileName|string|`${project.artifactId}-spring-properties.md`|The name of the generated markdown file.|
|generatedDocumentationHeader|string|`${project.artifactId} Spring Properties`|The name value for the documentation header (# in markdown).|
|failBuildOnMissingMetadata|boolean|`true`|Whether or not to fail the build if the 'spring-configuration-metadata' file cannot be loaded, or does not exist.|
