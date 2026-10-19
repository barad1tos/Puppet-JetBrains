package software.barad1tos.puppet.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import software.barad1tos.puppet.resolve.OpenVoxReferenceContext
import software.barad1tos.puppet.resolve.OpenVoxScope
import software.barad1tos.puppet.psi.OpenVoxTypes as T

/** Declarations whose parameters are only visible inside their own body. */
private val LOCAL_SCOPES = TokenSet.create(T.DEFINE_DEFINITION, T.FUNCTION_DEFINITION, T.LAMBDA, T.PLAN_DEFINITION)

class OpenVoxUnusedParameterInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != T.PARAMETER) return
                val variable = element.node?.findChildByType(T.VARIABLE)?.psi ?: return

                val owner =
                    generateSequence(element) { it.parent }
                        .firstOrNull { it.node?.elementType in LOCAL_SCOPES } ?: return
                val name = OpenVoxScope.normalise(variable.text)

                val used =
                    PsiTreeUtil
                        .findChildrenOfType(owner, PsiElement::class.java)
                        .any { isUsageOf(element, name, it) }
                if (used) return

                holder.registerProblem(
                    variable,
                    "Parameter '${variable.text}' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                )
            }
        }

    private fun isUsageOf(
        parameter: PsiElement,
        name: String,
        candidate: PsiElement,
    ): Boolean {
        val isVariable =
            candidate.node?.elementType == T.VARIABLE_EXPR ||
                OpenVoxReferenceContext.isInterpolationVariable(candidate)
        return isVariable &&
            OpenVoxScope.normalise(candidate.text) == name &&
            OpenVoxScope.resolve(candidate, name) === parameter
    }
}
