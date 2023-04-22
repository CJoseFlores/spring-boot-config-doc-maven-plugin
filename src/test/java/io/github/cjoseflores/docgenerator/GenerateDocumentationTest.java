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

package io.github.cjoseflores.docgenerator;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class GenerateDocumentationTest {
    @Rule
    public final MojoRule rule = new MojoRule();

    @Test
    public void testFullGeneration()
            throws Exception {
        File pom = new File("target/test-classes/properly-configured-project/");
        assertNotNull(pom);
        assertTrue("'" + pom.getAbsoluteFile() + "' does not exist!", pom.exists());

        GenerateDocumentation myMojo = (GenerateDocumentation) rule.lookupConfiguredMojo(pom, "generate-documentation");
        assertNotNull(myMojo);
        myMojo.execute();

        // FIXME: Add validation with the generated markdown
    }

    @Test
    public void testFailureToLoadMetadata() throws Exception {
        File pom = new File("target/test-classes/missing-metadata-failure-project/");
        assertNotNull(pom);
        assertTrue("'" + pom.getAbsoluteFile() + "' does not exist!", pom.exists());

        GenerateDocumentation myMojo = (GenerateDocumentation) rule.lookupConfiguredMojo(pom, "generate-documentation");
        assertNotNull(myMojo);

        String metadataDirectory = (String) rule.getVariableValueFromObject(myMojo, "metadataDirectory");
        String metadataFileName = (String) rule.getVariableValueFromObject(myMojo, "metadataFileName");
        assertNotNull(metadataDirectory);
        assertNotNull(metadataFileName);

        String metadataAbsolutePath = new File(metadataDirectory + File.separator + metadataFileName).getAbsolutePath();
        String expectedMessage = String.format("Could not load the spring configuration file '%s'!", metadataAbsolutePath);
        MojoExecutionException exception = Assert.assertThrows(
                "Expected to fail loading metadata!",
                MojoExecutionException.class,
                myMojo::execute);
        assertEquals(
                "Exception message for failing to load does not match!",
                expectedMessage,
                exception.getMessage());
    }

    @Test
    public void testSkippingMojoOnFailureToLoadMetadata() throws Exception {
        File pom = new File("target/test-classes/missing-metadata-skip-project/");
        assertNotNull(pom);
        assertTrue("'" + pom.getAbsoluteFile() + "' does not exist!", pom.exists());

        GenerateDocumentation myMojo = (GenerateDocumentation) rule.lookupConfiguredMojo(pom, "generate-documentation");
        assertNotNull(myMojo);

        String metadataDirectory = (String) rule.getVariableValueFromObject(myMojo, "metadataDirectory");
        String metadataFileName = (String) rule.getVariableValueFromObject(myMojo, "metadataFileName");
        assertNotNull(metadataDirectory);
        assertNotNull(metadataFileName);
        myMojo.execute();

        // FIXME: Add an assertion to make sure that a file was not generated!
    }
}
