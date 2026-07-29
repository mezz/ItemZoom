pluginManagement {
	repositories {
		exclusiveContent {
			forRepository {
				maven("https://maven.neoforged.net/releases")
			}
			filter {
				includeGroupByRegex("net\\.neoforged.*")
			}
		}
		gradlePluginPortal()
	}
}
