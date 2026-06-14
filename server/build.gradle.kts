plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.gantlab.satori"
version = "1.0.0"
application {
    mainClass.set("com.gantlab.satori.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverRequestValidation)
    implementation(libs.ktor.serverSwagger)
    implementation(libs.ktor.serverOpenapi)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.postgres)
    implementation(libs.hikaricp)
    implementation(libs.bcrypt)
    implementation(libs.koinServer)
    implementation(libs.google.auth)
    implementation(libs.google.auth.gson)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serializationKotlinxJson)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
