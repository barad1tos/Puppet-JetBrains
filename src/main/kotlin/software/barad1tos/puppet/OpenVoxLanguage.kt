package software.barad1tos.puppet

import com.intellij.lang.Language

object OpenVoxLanguage : Language("OpenVox") {
    override fun getDisplayName() = "OpenVox / Puppet"
    override fun isCaseSensitive() = true
}

object EppLanguage : Language(OpenVoxLanguage, "EPP") {
    override fun getDisplayName() = "EPP template"
    override fun isCaseSensitive() = true
}
