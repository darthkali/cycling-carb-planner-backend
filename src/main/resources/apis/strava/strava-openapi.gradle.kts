import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.7.0")
    }
}

val build_package_name = "strava-api"
val package_path = "de.darthkali.strava.api"
val spec_file = "$rootDir/src/main/resources/apis/strava/strava-3.0.0.json"
val model_name_prefix = "Strava"
val task_name = "generateOpenApiStrava"

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(task_name) {
    generatorName.set("kotlin")
    inputSpec.set(spec_file)
    outputDir.set("$buildDir/$build_package_name")
    modelPackage.set("$package_path.model")
    apiPackage.set("$package_path.api")
    invokerPackage.set("$package_path.invoker")
    modelNamePrefix.set(model_name_prefix)

    globalProperties.set(
        mapOf(
            "skipFormModel" to "false",
        ),
    )

    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "useTags" to "true",
            "sourceFolder" to "src/main/kotlin",
            "useAbstractionForFiles" to "true",
            "useJakartaEe" to "true",
            "useSpringBoot3" to "true",
            "generateClientAsBean" to "true",
            "packageName" to package_path
        ),
    )

    typeMappings.set(
        mapOf(
            "File" to "org.springframework.core.io.Resource",
        ),
    )
}

tasks.named("compileKotlin") {
    dependsOn(task_name)
}

project.the<SourceSetContainer>()["main"].java {
    srcDir("$buildDir/$build_package_name/src/main/kotlin")
}

