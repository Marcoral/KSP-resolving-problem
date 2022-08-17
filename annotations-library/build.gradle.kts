plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":api-annotation"))
    ksp(project(":api-processor"))
    implementation(kotlin("reflect"))
}