repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.19.4-R0.1-SNAPSHOT")
    compileOnly(group = "org.popcraft", name = "chunky-common", version = "1.3.67")
    implementation(group = "net.kyori", name = "adventure-api", version = "4.13.1")
    implementation(group = "net.kyori", name = "adventure-text-minimessage", version = "4.13.1")
    implementation(group = "net.kyori", name = "adventure-platform-bukkit", version = "4.3.0")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.1")
    implementation(project(":bolt-common"))
    implementation(project(":bolt-paper"))
    implementation(project(":bolt-folia"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name.capitalize(),
                "version" to project.version,
                "group" to project.group,
                "author" to project.property("author"),
                "description" to project.property("description"),
            )
        }
    }
    shadowJar {
        minimize {
            exclude(project(":bolt-common"))
            exclude(project(":bolt-paper"))
            exclude(project(":bolt-folia"))
        }
        relocate("net.kyori", "${project.group}.${rootProject.name}.lib.net.kyori")
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.org.bstats")
    }
}
