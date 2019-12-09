package abyss.model.tree.nodes

import abyss.model.DeclarationType
import com.intellij.psi.stubs.Stub
import java.net.URL


// TODO: remove `stub` from an interface

class RootSharedStubModel(
    override val stub: Stub,
    override val url: URL,
    override val sharedChildren: Collection<SharedStubModelInterface> = mutableListOf()
) : SharedStubModelInterface {


}

class ElementSharedStubModel(
    override val stub: Stub,
    override val url: URL,
    override val sharedChildren: Collection<SharedStubModelInterface> = mutableListOf(),
    val declarationType: DeclarationType
) : SharedStubModelInterface {


}