package software.barad1tos.puppet.psi

import com.intellij.psi.tree.IElementType
import software.barad1tos.puppet.OpenVoxLanguage

class OpenVoxElementType(debugName: String) : IElementType(debugName, OpenVoxLanguage)

class OpenVoxTokenType(debugName: String) : IElementType(debugName, OpenVoxLanguage) {
    override fun toString() = "OpenVoxTokenType." + super.toString()
}
