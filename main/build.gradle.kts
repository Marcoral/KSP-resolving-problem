plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":api-annotation"))
    implementation(project("::annotations-library"))
    ksp(project(":api-processor"))
}