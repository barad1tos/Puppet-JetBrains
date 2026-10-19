package software.barad1tos.puppet.project

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import software.barad1tos.puppet.psi.OpenVoxLiteralExpr
import software.barad1tos.puppet.resolve.OpenVoxReferenceContext
import software.barad1tos.puppet.psi.OpenVoxTypes as T

class OpenVoxProjectReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(OpenVoxLiteralExpr::class.java),
            FilePathProvider,
        )
    }
}

private object FilePathProvider : PsiReferenceProvider() {
    private val TEMPLATE_FUNCTIONS = setOf("epp", "template", "inline_epp", "inline_template")
    private val FILE_ATTRIBUTES = setOf("source")

    /** Two quotes and at least one character of path between them. */
    private const val MIN_QUOTED_PATH_LENGTH = 3

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        val quoted = element.firstChild?.takeIf { it.elementType == T.SQ_STRING } ?: return PsiReference.EMPTY_ARRAY
        val text = quoted.text
        if (text.length < MIN_QUOTED_PATH_LENGTH) return PsiReference.EMPTY_ARRAY
        val range = TextRange(1, text.length - 1)

        OpenVoxReferenceContext.parenthesisedCallName(element)?.let { name ->
            if (name in TEMPLATE_FUNCTIONS) {
                return arrayOf(OpenVoxFileReference(element, range, "templates"))
            }
        }
        attributeName(element)?.let { name ->
            if (name in FILE_ATTRIBUTES) {
                return arrayOf(OpenVoxFileReference(element, range, "files"))
            }
        }
        return PsiReference.EMPTY_ARRAY
    }

    private fun attributeName(element: PsiElement): String? {
        val operation =
            generateSequence(element) { it.parent }
                .firstOrNull { it.elementType == T.ATTRIBUTE_OPERATION } ?: return null
        return operation.firstChild?.text
    }
}
