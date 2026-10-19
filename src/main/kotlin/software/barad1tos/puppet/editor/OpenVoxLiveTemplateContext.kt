package software.barad1tos.puppet.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import software.barad1tos.puppet.OpenVoxLanguage

class OpenVoxLiveTemplateContext : TemplateContextType("OpenVox") {
    override fun isInContext(context: TemplateActionContext): Boolean =
        context.file.language.isKindOf(OpenVoxLanguage)
}
