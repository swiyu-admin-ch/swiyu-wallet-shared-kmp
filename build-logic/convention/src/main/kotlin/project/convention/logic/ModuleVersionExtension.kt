package project.convention.logic

import org.gradle.api.provider.Property

abstract class ModuleVersionExtension {
    abstract val moduleVersion: Property<String>
}
