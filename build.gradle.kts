plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
}

group = "dev.hiroine"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }

    shadowJar {
        archiveClassifier.set("all")
        relocate("kotlin", "dev.hiroine.flux.libs.kotlin")
    }
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            // ShadowJar를 배포하도록 설정
            project.shadow.component(this)

            groupId = "dev.hiro"
            artifactId = "flux-api"
            version = "1.0.0"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            // 사용자명/리포지토리명 경로를 정확히 입력하세요
            url = uri("https://maven.pkg.github.com/사용자명/FluxAPI")
            credentials {
                username = System.getenv("GITHUB_ACTOR") // 실행하는 유저 이름 자동 인식
                password = System.getenv("GITHUB_TOKEN") // 실행 환경의 토큰 자동 인식
            }
        }
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
    filesMatching("plugin.yml") {
        expand(props)
    }
}
