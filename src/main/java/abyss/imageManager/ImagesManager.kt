package abyss.imageManager

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon


class CustomIcons {

    companion object {
        private fun load(path: String): Icon {
            return IconLoader.getIcon(path)
        }
    }

    public final class Nodes {
        companion object {
            val Root = AllIcons.Nodes.Folder
            val File = load("/abyss/file.png")
            val Annotation = load("/abyss/annotation.png")
            val Property = load("/abyss/property.png")
            val Function = load("/abyss/func.png")
            val Class = load("/abyss/class.png")
            val Object = load("/abyss/obj.png")
            val Expect = load("/abyss/expect.png")
            val Actual = load("/abyss/actual.png")
        }
    }

    public final class Actions {
        companion object {
            val Refresh = AllIcons.Actions.Refresh
        }
    }
}
