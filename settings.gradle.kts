plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "BugUpsert"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val exposed = "0.53.0"
            library("exposed-core", "org.jetbrains.exposed", "exposed-core").version(exposed)
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").version(exposed)
            library("postgresql", "com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
            library("pgsql-container","org.testcontainers:postgresql:1.20.0")

        }
    }
}