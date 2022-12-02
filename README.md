# Java-Gradle All-in-One Template

The purpose of this project is to demonstrate a first step for when it comes to centralizing CI/CD practices, specifically when it comes to all that build.gradle you are copying and pasting between projects. The objective is to abstract what is commonly being copy and pasted into a series of reusable plugins. However, as in a first step it is unlikely that centralizing artifact publishing has been figured out, the plugins are within the project iself.

The intention is that at some point each of those plugins are broken out into their own repositories and published independently, so that all other projects/repositories can make use of them with almost zero copy/paste.

The initial project was generated using:

```bash
gradle init
```

With the initial selections made for:

1. Java language
2. Library
3. Testing using Juniper Junit
4. Build language of Groovy DSL

# Configuration Theory

## Configuration Theory 1: Maven Local

The problem with this approach is that it relies on having the plugins published to maven local, which means you sometimes have to comment out the plugins in lib/build.gradle to get them to publish in the first place.

The intention is for this project to represent a single library, denoted by the `lib` directory. Instead of having its build.gradle contain hundreds of lines of closures/extentions, the next intention is to represent all of that using a series of plugins. Those plugins also sit in this project, but only temporarily. The concept is to workout exact how these plugins need to work, and then start abstracting them out into their own centralized plugins. This is just the first step.

Getting this to work through required some Gradle configuration magic. Specifically the only way to include a plugin that is build with the same project, is by first publishing it locally.

This is why in plugin/build.gradle, we have to both include the `maven-publish` plugin and give this a default version:

```groovy
plugins {
    id 'maven-publish'
}

version '1.0.0'

gradlePlugin {
    plugins {
        greeting {
            id = 'example.gradle.plugin.greeting'
            implementationClass = 'example.gradle.plugin.ExampleGradlePluginPlugin'
        }
    }
}
```

This is because in order to include a plugin, at least a non-core ones require that in their declaration a version is given.

This then allows you to publish the plugins into the local maven repository using:

```bash
gradle clean publishToMavenLocal -i
```

The next change is with settings.gradle, where we have to specify that we can additionally find local repositories in the local maven repository:

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

You then can apply that plugin via lib/build.gradle:

```groovy
plugins {
    id "example.gradle.plugin.greeting" version "1.0.0"
}
```

Consider that once this plugin is externalized, you don't need anything but this last step (with whatever the appropriate published version is).

## Configuration Theory 2

TBD

# Operation

## CLI

Of course this works, and that is the easy part. However, because we are using plugins with the lib, we have to first publish the plugin locally for this to work:

```bash
gradle clean publishToMavenLocal -i
```

It is at that point that the "lib" project can otherwise actually build:

```bash
gradle clean build -i  
```

...and otherwise demontrate that it can apply the demo plugin known as "greeting":

```bash
% gradle greeting                                                          

> Task :lib:greeting
Hi from plugin 'example.gradle.plugin.greeting'

BUILD SUCCESSFUL in 508ms
1 actionable task: 1 executed

```

## IntelliJ CE

By far the best option, as it just works out-the-box, without any custom hackery.

Just import it as a Gralde projct, and done.

## Eclipse (Spring Tool Suite 4)

This will generally first require that you run the following at the command-line:

```bash
gradle clean publishToMavenLocal -i
gradle clean build -i  
```

Make sure that the default JRE/JDK is set to version 19.

File -> Import -> As Gradle Project

You know that it worked if:

1. You have a list of Gradle Tasks in the Gradle Task View
2. Your Problems view doesn't show any problems

The basic library will allow you to right click and "Run as JUnit" with no issues, meaning lib/src/test/java/com/stuff/LibraryTest.java (assuming you are in the Package Explorer).

### Unit Testing Plugins

If you right-click and run it as a Gradle testing, it works.

However if you attempt to run ExampleGradlePluginPluginTest as JUnit, you will encounter this error:

```bash
org.gradle.api.GradleException: Could not inject synthetic classes.
...
Caused by: java.lang.IllegalAccessException: module java.base does not open java.lang to unnamed module
...
```

You can resolve this by editing the test configuration and adding the VM argument of:

```bash
--add-opens java.base/java.lang=ALL-UNNAMED
```

Trying to handle this via build.gradle does not work, and the JUnit runtime ignores it.

However, you now get this error:

```bash
org.gradle.api.plugins.UnknownPluginException: Plugin with id 'example.gradle.plugin.greeting' not found.
	...
```

If you change this line:

```java
project.getPlugins().apply("example.gradle.plugin.greeting");
```

...to this:

```java
project.getPlugins().apply(ExampleGradlePluginPlugin.class);
```

...it works.

Unfortauntely this means to run these unit tests out of the IDE, you are having to constant add VM arguments and change the way in which you apply plugins.

### Functional Testing Plugins

If you right-click and run it as a Gradle testing, it works.

Now with `ExampleGradlePluginPluginFunctionalTest`, if you try to run this as JUnit, it will also fail:

```bash
org.gradle.testkit.runner.UnexpectedBuildFailure: Unexpected build execution failure in /var/folders/2s/bs17fwfd70132wjmqpw6cv7r0000gn/T/junit6932702913170277471 with arguments [greeting]

Output:

FAILURE: Build failed with an exception.

* Where:
Build file '/private/var/folders/2s/bs17fwfd70132wjmqpw6cv7r0000gn/T/junit6932702913170277471/build.gradle' line: 1

* What went wrong:
Plugin [id: 'example.gradle.plugin.greeting'] was not found in any of the following sources:

```

I suspect it is possible to workaround this my making significant changes to the generated build.gradle, but feel that this would make it to different. The recommendation is run to run these as Gradle tests.

## Visual Studio Code

The following extensions are first required:

1. Debugger for Java (`v0.47.0`*Preview*)
2. Extension Pack for Java (`v0.25.7`*Preview*)
3. Gradle for Java (v3.12.6)
4. Project Manager for Java (`v0.21.1`*Preview*)
5. Test Runner for Java (`v0.37.1`*Preview*)

### Configuring the Java Runtime

Getting the Gralde Plugin to work required that I used Cmd + Shift + P to open the Configure Java Runttime option, where you need to delect a 17 or later version of the JDK to cause the project to be picked up.

I have also had to relaunch VSCode to get this to work.

### Fun With Unit Testing

lib/src/test/java/com/stuff/Library.java will work out-of-the-box, as evident by being able to lick next to line numbers to execute either the entire class or method.

plugin/src/test/java/example/gradle/plugin/ExampleGradlePluginPluginTest will of source not work, and result in this familiar error:

```bash
org.gradle.api.GradleException: Could not inject synthetic classes.
...
Caused by: java.lang.IllegalAccessException: module java.base does not open java.lang to unnamed module
...
```

So we fixed this earlier by adding some JVM args, `--add-opens java.base/java.lang=ALL-UNNAMED`, so how do we do this here?

.vscode/settings.json

```json
{
    "java.test.config": {
        "vmArgs": ["--add-opens", "java.base/java.lang=ALL-UNNAMED"]
    }
}
```



### Functional Testing Plugins

Now with `ExampleGradlePluginPluginFunctionalTest`, if you try to run this, it will also fail:

```bash
org.gradle.testkit.runner.UnexpectedBuildFailure: Unexpected build execution failure in /var/folders/2s/bs17fwfd70132wjmqpw6cv7r0000gn/T/junit6932702913170277471 with arguments [greeting]

Output:

FAILURE: Build failed with an exception.

* Where:
Build file '/private/var/folders/2s/bs17fwfd70132wjmqpw6cv7r0000gn/T/junit6932702913170277471/build.gradle' line: 1

* What went wrong:
Plugin [id: 'example.gradle.plugin.greeting'] was not found in any of the following sources:

```

I am stumped on this one, and had to restort to just run `gradle test` at the CLI.
