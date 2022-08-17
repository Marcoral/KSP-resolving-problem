val kspVersion: String by project
val kotlinpoetVersion: String by project

dependencies {
    implementation(project(":api-annotation"))
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinpoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}