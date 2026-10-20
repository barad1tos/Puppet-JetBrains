package software.barad1tos.puppet.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import software.barad1tos.puppet.psi.OpenVoxTypes as T

class OpenVoxUnknownFunctionInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != T.FUNCTION_CALL_EXPR) return
                val name = element.firstChild ?: return
                if (name.node?.elementType != T.QUALIFIED_NAME && name.node?.elementType != T.WORD_REF) return
                // `String($x)` is `String.new($x)`: a type name called like a function converts, it is no function.
                if (name.node.firstChildNode?.elementType == T.CLASSREF) return
                if (OpenVoxInspectionSupport.functionExists(element, name.text)) return

                holder.registerProblem(
                    name,
                    "Unknown function '${name.text}'",
                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                )
            }
        }
}
