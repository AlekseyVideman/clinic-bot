import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer

plugins {
    java
    idea
    `java-library`
    groovy
    alias(libs.plugins.shadow)
}

val botVer: String = providers.gradleProperty("botVer").get()

dependencies {
    implementation(libs.bundles.database)
    api(libs.bundles.logging)
    implementation(libs.json.schema.validator)

    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.testing)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    shadowJar {
//        minimize()

        exclude(
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/native-image/**",
            "**/LICENSE*",
            "**/NOTICE*",
            "*.txt",
            "INFO_*",
            "*.csv",
            "README",
            "google/**",
            "draft-*/**"
        )

        transform<AppendingTransformer> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            resource = "org.flywaydb.core.extensibility.Plugin"
        }
        transform<AppendingTransformer> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            resource = "java.sql.Driver"
        }
        mergeServiceFiles() // без этого service-файлы заменяют друг друга
    }

    build {
        dependsOn(shadowJar)
    }
    jar {
        manifest {
            attributes["Main-Class"] = "alekseyvideman.clinic_bot.Bot"
        }
    }
    idea {
        module.isDownloadJavadoc = true
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.isFork = true // compile in a separate and reusable process (fast)
    }
    withType<JavaExec>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
    withType<ProcessResources>().configureEach {
        val map: Map<String, Any> = mapOf(
            "MAX_TOKEN" to System.getenv("MAX_TOKEN"),
            "version" to botVer
        )
        filesMatching("app.properties") { expand(map) }
    }
}