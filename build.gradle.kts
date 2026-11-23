import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.0"
}

group = "com.korenevskiy"
version = providers.gradleProperty("pluginVersion").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        if (localIdePath.isNullOrBlank()) {
            intellijIdea(providers.gradleProperty("platformVersion"))
        } else {
            local(localIdePath)
        }
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
        }
        changeNotes = """
            <h3>1.0.0</h3>
            <ul>
              <li>Initial release: classes annotated with <code>@Order(n)</code> are listed first in the Project view,
                  followed by non-annotated classes.</li>
              <li>Supports literal and constant-expression values, and any annotation named <code>Order</code>.</li>
            </ul>
        """.trimIndent()
    }

    // Credentials come from the environment so they never end up in the repository.
    // See https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    // Marketplace token: https://plugins.jetbrains.com/author/me/tokens
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    // `./gradlew verifyPlugin` checks binary compatibility against the IDE releases recommended for sinceBuild.
    pluginVerification {
        ides {
            recommended()
        }
    }
}

// `./gradlew runIde` opens the bundled sample project for manual testing.
tasks.runIde {
    args(layout.projectDirectory.dir("sample-project").asFile.absolutePath)
}
