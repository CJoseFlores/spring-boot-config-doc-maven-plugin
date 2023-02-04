/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.cjoseflores;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.format.MarkdownTable;
import com.vladsch.flexmark.util.format.TableCell;

/**
 * Goal which generates a markdown file from a spring configuration metadata
 * json file.
 */
/*
 * TODO: Does this phase make sense? We need to make sure to run this AFTER the
 * metadata is generated...
 */
@Mojo(name = "generate-documentation", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateDocumentation
        extends AbstractMojo {

    /**
     * Location of the 'spring-configuration-metadata' file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF", property = "metadataDirectory", required = true)
    private String metadataDirectory;
    /**
     * The name of the 'spring-configuration-metadata' file.
     */
    @Parameter(defaultValue = "spring-configuration-metadata.json", property = "metadataFileName", required = true)
    private String metadataFileName;
    /**
     * Location to place the generated markdown file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDirectory", required = true)
    private File outputDirectory;
    /**
     * The name of the generated markdown file.
     */
    @Parameter(defaultValue = "${project.artifactId}-spring-properties.md", property = "generatedFileName", required = true)
    private String generatedFileName;
    /**
     * The name value for the documentation header (# in markdown).
     */
    @Parameter(defaultValue = "${project.artifactId} Spring Properties", property = "generatedDocumentationHeader", required = true)
    private String generatedDocumentationHeader;
    /**
     * Whether or not to fail the build if the 'spring-configuration-metadata' file
     * cannot be loaded, or does not exist.
     */
    @Parameter(defaultValue = "true", property = "failBuildOnMissingMetadata", required = true)
    private boolean failBuildOnMissingMetadata;

    /**
     * TODO: Allow manual markdown to be inserted somewhere in the generated
     * markdown, i.e. perhaps suggest
     * configuration of spring boot starters used by the application?
     * 
     * @throws MojoFailureException
     */

    public void execute() throws MojoExecutionException {
        Optional<ConfigurationMetadata> metadataOpt = loadMetadata(
                new File(metadataDirectory + File.separator + metadataFileName));

        if (metadataOpt.isEmpty()) {
            getLog().warn("No configuration file loaded, skipping documentation generation!");
            return;
        }

        String markdownRepresentation = buildMarkdownString(metadataOpt.get());

        try {
            // TODO: Should we make this directory if it isn't created? It should work as
            // long as defaults are set...
            Files.writeString(Paths.get(outputDirectory + File.separator + generatedFileName), markdownRepresentation);
        } catch (IOException e) {
            getLog().error("Unable to generate documentation! ", e);
        }

    }

    /**
     * Load the spring configuration metadata into memory.
     * 
     * @param metadataFile The file holding the spring configuration metadata.
     * @return An {@link Optional} containing a {@link ConfigurationMetadata}
     *         object, or empty if it could not be loaded.
     * @throws MojoFailureException thrown when the metadata file could not be
     *                              loaded, and the 'failBuildOnMissingMetadata'
     *                              field is set.
     */
    private Optional<ConfigurationMetadata> loadMetadata(File metadataFile) throws MojoExecutionException {
        JsonMarshaller marshaller = new JsonMarshaller();

        try (FileInputStream metadataInputStream = new FileInputStream(metadataFile)) {
            return Optional.of(marshaller.read(metadataInputStream));
        } catch (Exception e) {
            String errorMsg = "Could not load the spring configuration file '" + metadataFile.getAbsolutePath() + "'!";
            if (failBuildOnMissingMetadata) {
                throw new MojoExecutionException(errorMsg, e);
            }
            getLog().error(errorMsg);
            return Optional.empty();
        }
    }

    /**
     * Creates a markdown-formatted string that represents the whole markdown
     * document generated by this mojo.
     * 
     * @param metadata The {@link ConfigurationMetadata} representing the properties
     *                 for the project of interest.
     * @return A string.
     */
    private String buildMarkdownString(ConfigurationMetadata metadata) {
        HtmlWriter out = new HtmlWriter(0, HtmlWriter.F_FORMAT_ALL);
        String documentHeader = "# " + generatedDocumentationHeader;
        String tableHeader = "## Properties";

        // Write this out
        // TODO: Figure out if we can leverage the HTMLWriter to add the header?
        createTable(metadata).appendTable(out);
        StringBuilder finalMarkdown = new StringBuilder();
        finalMarkdown.append(documentHeader);
        finalMarkdown.append("\n\n");
        finalMarkdown.append(tableHeader);
        finalMarkdown.append("\n");
        finalMarkdown.append(out.toString());
        return finalMarkdown.toString();
    }

    /**
     * Creates a markdown table from the loaded configuration metadata.
     * 
     * @param metadata A {@link ConfigurationMetadata} object to turn into a
     *                 markdown table.
     * @return A {@link MarkdownTable}.
     */
    private MarkdownTable createTable(ConfigurationMetadata metadata) {
        MarkdownTable table = new MarkdownTable("", new MutableDataSet());
        table.setHeader();
        table.addCell(createTableCell("Name"));
        table.addCell(createTableCell("Description"));
        table.addCell(createTableCell("Type"));
        table.addCell(createTableCell("Default"));
        table.setBody();

        for (ItemMetadata itemMetadata : metadata.getItems()) {
            if (itemMetadata.isOfItemType(ItemType.GROUP)) {
                getLog().info("Found a group item (skipping): " + itemMetadata.toString());
                continue;
            } else {
                // TODO: Implement strikethrough text on deprecated properties?
                table.addCell(createTableCell(
                        itemMetadata.getDeprecation() == null
                                ? itemMetadata.getName()
                                : itemMetadata.getName() + " (deprecated)"));
                table.addCell(createTableCell(
                        itemMetadata.getDescription() != null
                                ? itemMetadata.getDeprecation() == null
                                        ? itemMetadata.getDescription()
                                        : itemMetadata.getDescription() + " (use: "
                                                + itemMetadata.getDeprecation().getReplacement()
                                                + " instead)"
                                : ""));
                table.addCell(createTableCell(itemMetadata.getType()));
                table.addCell(createTableCell(
                        itemMetadata.getDefaultValue() != null ? itemMetadata.getDefaultValue().toString() : ""));
            }
            table.nextRow();
        }

        table.fillMissingColumns();
        return table;
    }

    private TableCell createTableCell(String cellName) {
        return new TableCell(cellName, 1, 1);
    }
}
