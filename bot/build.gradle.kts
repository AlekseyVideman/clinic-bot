plugins {
    java
    idea
    application
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.db.scheduler)
    implementation(libs.bundles.max)
    implementation(libs.ez.vcard)

    compileOnly(libs.jspecify)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    shadowJar {
        archiveBaseName.set(rootProject.name)
//        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    jar {
        manifest {
            attributes["Main-Class"] = "nelbiasoftware.clinic_bot.bot.Bot"
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
}