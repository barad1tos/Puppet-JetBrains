package software.barad1tos.puppet.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import software.barad1tos.puppet.EppFileType
import software.barad1tos.puppet.EppLanguage
import software.barad1tos.puppet.OpenVoxFileType
import software.barad1tos.puppet.OpenVoxLanguage

class OpenVoxFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, OpenVoxLanguage) {
    override fun getFileType() = OpenVoxFileType
    override fun toString() = "OpenVox manifest"
}

class EppFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, EppLanguage) {
    override fun getFileType() = EppFileType
    override fun toString() = "EPP template"
}
