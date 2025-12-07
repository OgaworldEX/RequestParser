plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.portswigger.burp.extensions:montoya-api:2023.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.jsoup:jsoup:1.17.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// ShadowJar 設定
tasks.shadowJar {
    archiveBaseName.set("RequestParser_vx.x.x")
    archiveClassifier.set("")  // -all を消す
    archiveVersion.set("")

    mergeServiceFiles()

    manifest {
        attributes(
            "Main-Class" to "burp.BurpExtender"  // Burp 拡張のエントリポイント
        )
    }
}

// build タスクに shadowJar を紐付け
tasks.build {
    dependsOn(tasks.shadowJar)
}
