package software.barad1tos.puppet

import com.intellij.openapi.fileTypes.LanguageFileType

object OpenVoxFileType : LanguageFileType(OpenVoxLanguage) {
    override fun getName() = "OpenVox"
    override fun getDescription() = "Puppet manifest"
    override fun getDefaultExtension() = "pp"
    override fun getIcon() = OpenVoxIcons.FILE
}

object EppFileType : LanguageFileType(EppLanguage) {
    override fun getName() = "EPP"
    override fun getDescription() = "Embedded Puppet template"
    override fun getDefaultExtension() = "epp"
    override fun getIcon() = OpenVoxIcons.FILE
}
