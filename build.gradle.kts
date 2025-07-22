plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "nl.jeroenvanwissen"
version = "1.0-SNAPSHOT"
val jacksonVersion = "2.15.3"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }

    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.twitch4j:twitch4j:1.25.0")
    implementation("com.github.philippheuer.credentialmanager:credentialmanager:0.3.1")

    compileOnly("com.github.Gypopo:EconomyShopGUI-API:1.8.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

}

configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        force("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        force("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        force("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    }
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// Will remove this later, just added for faster testing
tasks.register<Copy>("copyJar") {
    dependsOn("shadowJar")

    var jarFile =
        tasks
            .named("shadowJar")
            .get()
            .outputs
            .files
            .singleFile
    val userHome = System.getProperty("user.home")

    from(jarFile)
    into("$userHome/Projects/MineCraft/minecraft-farming-server/plugins")
}

tasks.named("shadowJar") {
    finalizedBy("copyJar")
}
