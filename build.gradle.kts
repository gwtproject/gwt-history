

plugins {
    id("java-library")
    id("maven")
    id("net.ltgt.errorprone") version "1.3.0"
    id("com.diffplug.spotless") version "5.10.2"
    id("local.maven-publish")
}

buildscript {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}
dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

group = "org.gwtproject.user.history"

repositories {
    mavenCentral()
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.5.1")
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")

    api("org.gwtproject.event:gwt-logical-event:1.0.0-RC1")
    implementation("org.gwtproject.user.window:gwt-window:1.0.0-RC2")
    implementation("com.google.elemental2:elemental2-dom:1.1.0")
    implementation("com.google.elemental2:elemental2-core:1.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.gwt:gwt-user:2.9.0")
    testImplementation("com.google.gwt:gwt-dev:2.9.0")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(arrayOf("-Werror", "-Xlint:all"))
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    }
}

sourceSets {
    test {
        java {
            srcDir("src/test/gwt2")
        }
    }
}

tasks {
    jar {
        from(sourceSets.main.map { it.allJava })
    }

    test {
        val warDir = file("$buildDir/gwt/www-test")
        val workDir = file("$buildDir/gwt/work")
        val cacheDir = file("$buildDir/gwt/cache")
        doFirst {
            mkdir(warDir)
            mkdir(workDir)
            mkdir(cacheDir)
        }

        classpath += sourceSets.main.get().allJava.sourceDirectories + sourceSets.test.get().allJava.sourceDirectories
        include("**/*Suite.class")
        systemProperty(
            "gwt.args",
            "-ea -draftCompile -batch module -war \"$warDir\" -workDir \"$workDir\" " +
                "-runStyle ${project.findProperty("test.gwt.runStyle") ?: "HtmlUnit:Chrome"}"
        )
        systemProperty("gwt.persistentunitcachedir", cacheDir)
        testLogging {
            events("STANDARD_OUT")
        }
    }

    val validateGwtModule by registering(JavaExec::class) {
        val workDir = file("$buildDir/gwt/work")
        val cacheDir = file("$buildDir/gwt/cache")
        val outFile = file("$buildDir/gwt/validateGwtModule")
        doFirst {
            mkdir(workDir)
            mkdir(cacheDir)

            standardOutput = outFile.outputStream()
            errorOutput = standardOutput
        }
        doLast {
            standardOutput.close()
        }

        inputs.files(sourceSets.main.map { it.allJava })
        outputs.file(outFile)

        main = "com.google.gwt.dev.Compiler"
        classpath = sourceSets.test.get().runtimeClasspath + sourceSets.main.get().allJava.sourceDirectories
        args("-strict", "-validateOnly", "-workDir", workDir, "org.gwtproject.user.history.History")
        systemProperty("gwt.persistentunitcachedir", cacheDir)
    }
    check { dependsOn(validateGwtModule) }

    javadoc {
        (options as CoreJavadocOptions).apply {
            addBooleanOption("Xdoclint:all,-missing", true)
            if (JavaVersion.current().isJava9Compatible) {
                addBooleanOption("html5", true)
            }
            // Workaround for https://github.com/gradle/gradle/issues/5630
            addStringOption("sourcepath", "")
        }
    }
}

spotless {
    java {
        target(sourceSets.map { it.allJava }, fileTree("src/test/j2cl") { include("**/*.java") })
        googleJavaFormat("1.7")
        licenseHeaderFile("LICENSE.header")
    }
    kotlinGradle {
        ktlint("0.40.0")
    }
}

//
// J2Cl tests
//
// Because there's only Maven tooling for J2Cl (and specifically tests), we publish
// the JAR to the local Maven repository under a fixed (non-snapshot) version, and
// then fork a Maven build.
//
val j2clTestPublication = publishing.publications.create<MavenPublication>("j2clTest") {
    from(components["java"])
    version = "LOCAL"
}
tasks {
    val j2clTest by registering(Exec::class) {
        shouldRunAfter(test)
        dependsOn("publishJ2clTestPublicationToMavenLocal")
        inputs.files(sourceSets.main.map { it.runtimeClasspath }).withNormalizer(ClasspathNormalizer::class)
        inputs.file("pom-j2cl-test.xml")
        inputs.dir("src/test/java")
        inputs.dir("src/test/j2cl")
        outputs.dir("target")

        val webdriver = findProperty("j2clTest.webdriver") ?: "htmlunit"
        inputs.property("webdriver", webdriver)

        commandLine("mvn", "-V", "-B", "-ntp", "-U", "-e", "-f", "pom-j2cl-test.xml", "verify", "-Dwebdriver=$webdriver")
    }

    check {
        dependsOn(j2clTest)
    }

    withType<PublishToMavenRepository>().configureEach {
        onlyIf { publication != j2clTestPublication }
    }
}
