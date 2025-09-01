import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationCommonPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.android.application")

        project.extensions.configure<AppExtension> {
            compileSdk = libs.versions.compileSdk.get().toInt()

            defaultConfig {
                minSdk = libs.versions.minSdk.get().toInt()
                targetSdk = libs.versions.targetSdk.get().toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                val javaVersion = JavaVersion.toVersion(libs.versions.javaVersion.get())
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }

            buildFeatures.viewBinding = true
        }
    }
}