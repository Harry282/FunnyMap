pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net") }
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "net.minecraftforge.gradle.forge" -> useModule("com.github.asbyth:ForgeGradle:${requested.version}")
                "org.spongepowered.mixin" -> useModule("com.github.xcfrg:MixinGradle:${requested.version}")
            }
        }
    }
}
