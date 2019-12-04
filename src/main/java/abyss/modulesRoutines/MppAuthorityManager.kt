package abyss.modulesRoutines

import com.intellij.ProjectTopics
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.caches.project.isMPPModule
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.platform.impl.CommonIdePlatformKind


class MppAuthorityManager {

    fun provideAuthorityZonesForProject(project: Project): Collection<MppAuthorityZone> {
        val modules = ModuleManager.getInstance(project).modules

        return modules.filter{ it.isMPPModule && it.platform?.kind == CommonIdePlatformKind }.map { module ->
            val dependantModules = ModuleManager.getInstance(project).getModuleDependentModules(module)
            val dependantMppModules = dependantModules.filter { it.isMPPModule }

            MppAuthorityZone(module, dependantMppModules)
        }

    }

    fun subscribeForModulesUpdates(project: Project) {
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, object : ModuleListener {
            fun moduleAdded(project: Project, module: Module) {
                // TODO: Handle module routines.
            }
        })

    }
}