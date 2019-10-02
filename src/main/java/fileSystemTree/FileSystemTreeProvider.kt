package fileSystemTree

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.ex.FileSystemTreeImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import javax.swing.JTree

class FileSystemTreeProvider {
    fun createFileSystemTree(project: Project): JTree {
        val defaultTree = Tree()
        val fileChooserDescriptor = FileChooserDescriptorFactory.createMultipleFoldersDescriptor()

        val fileSystemTree = FileSystemTreeImpl(
            project,
            fileChooserDescriptor,
            defaultTree,
            null,
            null,
            null
        ).tree

        return fileSystemTree
    }
}