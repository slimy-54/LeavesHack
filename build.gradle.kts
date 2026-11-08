plugins {
    alias(libs.plugins.fabric.loom)
}

base {
    archivesName = properties["archives_base_name"] as String
    version = libs.versions.mod.version.get()
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "Aliyun Maven Central"
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    maven {
        name = "babbaj-repo"
        url = uri("https://babbaj.github.io/maven/")
    }
    maven {
        name = "masa-maven"
        url = uri("https://masa.dy.fi/maven/sakura-ryoko/")
    }
    maven {
        name = "fallenbreath-maven"
        url = uri("https://maven.fallenbreath.me/releases")
    }
}

dependencies {
    // Fabric
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    // Meteor
    implementation(libs.meteor.client)

    // Litematica / MaLiLib are mojmap dev jars -> use implementation, not modImplementation.
    implementation("fi.dy.masa.litematica:litematica-fabric-26.1.2:0.27.14")
    implementation("fi.dy.masa.malilib:malilib-fabric-26.1.2:0.28.12")

    // Required for @WrapOperation / @ModifyExpressionValue
    include(implementation("io.github.llamalad7:mixinextras-fabric:0.5.5")!!)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
    }
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to "~26.1",
            "jdk_version" to libs.versions.jdk.get()
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = libs.versions.jdk.get().toInt()
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:deprecation",
                "-Xlint:unchecked",
                "-Xmaxerrs",
                "10000"
            )
        )
    }
}
