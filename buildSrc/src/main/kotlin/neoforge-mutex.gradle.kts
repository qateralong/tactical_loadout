import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

// Decompiling Minecraft for several versions at once can exhaust memory, so these tasks run one at a time.
interface NeoForgeMutex : BuildService<BuildServiceParameters.None>

val mutex = gradle.sharedServices.registerIfAbsent("createMinecraftArtifactsMutex", NeoForgeMutex::class.java) {
    maxParallelUsages.set(1)
}

tasks.named { it == "createMinecraftArtifacts" }.configureEach {
    usesService(mutex)
}
