/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package example.gradle.plugin;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import example.gradle.plugin.ExampleGradlePluginPlugin;

/**
 * A simple unit test for the 'example.gradle.plugin.greeting' plugin.
 */
class ExampleGradlePluginPluginTest {
    @Test void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        System.out.println(project.getPlugins());
        
        project.getPlugins().apply("example.gradle.plugin.greeting");

        // Verify the result
        assertNotNull(project.getTasks().findByName("greeting"));
    }
}