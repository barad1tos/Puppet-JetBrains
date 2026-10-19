package software.barad1tos.puppet.completion

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import software.barad1tos.puppet.psi.OpenVoxTypes as T

sealed interface OpenVoxCompletionPlace {
    data class AttributeName(
        val resourceType: String,
    ) : OpenVoxCompletionPlace

    data class AttributeValue(
        val resourceType: String,
        val attributeName: String,
    ) : OpenVoxCompletionPlace

    data object DataType : OpenVoxCompletionPlace

    data object ResourceType : OpenVoxCompletionPlace

    data class FactName(
        val prefix: String,
        val trusted: Boolean,
    ) : OpenVoxCompletionPlace

    data object Statement : OpenVoxCompletionPlace
}

object OpenVoxCompletionContext {
    private val OPENING = setOf(T.LBRACE, T.SELBRACE, T.LBRACK, T.LISTSTART, T.LPAREN, T.WSLPAREN)
    private val CLOSING = setOf(T.RBRACE, T.RBRACK, T.RPAREN)
    private val ARROWS = setOf(T.FARROW, T.PARROW)
    private val SEPARATORS = setOf(T.COMMA, T.COLON, T.SEMIC)
    private val DEFINITION_KEYWORDS = setOf(T.CLASS, T.DEFINE, T.FUNCTION, T.PLAN)
    private val RESOURCE_OWNERS = setOf(T.IDENTIFIER, T.BARE_WORD, T.CLASSREF)
    private val PARENTHESES = setOf(T.LPAREN, T.WSLPAREN)
    private val BRACKETS = setOf(T.LBRACK, T.LISTSTART)

    /** How far back the caret context is searched; bounds the cost on long manifests. */
    private const val LOOKBEHIND_LEAVES = 400

    /** A double-quoted key is three leaves: DQ_START, DQ_TEXT and DQ_END. */
    private const val QUOTED_KEY_LEAVES = 3

    /** What the caret had just passed inside the enclosing bracket: an attribute arrow or a separator. */
    private sealed interface Pending {
        data class Value(
            val attributeName: String,
        ) : Pending

        data object Name : Pending
    }

    /** The unmatched opening bracket around the caret, as an index into the leaves before it. */
    private data class Enclosing(
        val index: Int,
        val pending: Pending?,
    )

    fun placeAt(position: PsiElement): OpenVoxCompletionPlace {
        val leaves = previousMeaningfulLeaves(position).toList()
        return factKey(leaves)
            ?: enclosingBracket(leaves)?.let { placeInside(leaves, it) }
            ?: placeFromTree(position)
    }

    private fun enclosingBracket(leaves: List<PsiElement>): Enclosing? {
        var depth = 0
        var pending: Pending? = null
        for ((index, leaf) in leaves.withIndex()) {
            val type = leaf.elementType ?: continue
            when {
                type in CLOSING -> depth++
                type in OPENING && depth > 0 -> depth--
                type in OPENING -> return Enclosing(index, pending)
                depth > 0 || pending != null -> Unit
                type in ARROWS -> pending = Pending.Value(leaves.getOrNull(index + 1)?.text.orEmpty())
                type in SEPARATORS -> pending = Pending.Name
            }
        }
        return null
    }

    private fun placeInside(
        leaves: List<PsiElement>,
        enclosing: Enclosing,
    ): OpenVoxCompletionPlace? {
        val bracket = leaves[enclosing.index].elementType
        val owner = leaves.getOrNull(enclosing.index + 1)
        val ownerType = owner?.elementType
        return when {
            bracket == T.LBRACE && owner != null && ownerType in RESOURCE_OWNERS -> {
                placeInBody(owner.text.removePrefix("::").lowercase(), enclosing.pending)
            }

            bracket in PARENTHESES && leaves.getOrNull(enclosing.index + 2)?.elementType in DEFINITION_KEYWORDS -> {
                OpenVoxCompletionPlace.DataType
            }

            bracket in BRACKETS && ownerType == T.CLASSREF -> {
                OpenVoxCompletionPlace.DataType
            }

            else -> {
                null
            }
        }
    }

    private fun placeInBody(
        resourceType: String,
        pending: Pending?,
    ): OpenVoxCompletionPlace =
        when (pending) {
            is Pending.Value -> OpenVoxCompletionPlace.AttributeValue(resourceType, pending.attributeName)
            Pending.Name -> OpenVoxCompletionPlace.AttributeName(resourceType)
            null -> OpenVoxCompletionPlace.Statement
        }

    private fun placeFromTree(position: PsiElement): OpenVoxCompletionPlace =
        when {
            inside(position, T.DATA_TYPE) ||
                inside(position, T.DATA_TYPE_PARAMETERS) ||
                inside(position, T.TYPE_ALIAS) ||
                inside(position, T.PARAMETER_LIST) ||
                position.text.firstOrNull()?.isUpperCase() == true -> OpenVoxCompletionPlace.DataType

            inside(position, T.RESOURCE_TYPE) -> OpenVoxCompletionPlace.ResourceType

            else -> OpenVoxCompletionPlace.Statement
        }

    private fun factKey(leaves: List<PsiElement>): OpenVoxCompletionPlace? {
        val bracket = leaves.indexOfFirst { it.elementType == T.LBRACK }
        if (bracket !in 0..1) return null
        var index = bracket + 1
        val path = mutableListOf<String>()
        while (leaves.getOrNull(index)?.elementType == T.RBRACK) {
            when (leaves.getOrNull(++index)?.elementType) {
                T.SQ_STRING -> {
                    path += leaves[index++].text.drop(1).dropLast(1)
                }

                T.DQ_END -> {
                    if (leaves.getOrNull(index + 1)?.elementType != T.DQ_TEXT ||
                        leaves.getOrNull(index + 2)?.elementType != T.DQ_START
                    ) {
                        return null
                    }
                    path += leaves[index + 1].text
                    index += QUOTED_KEY_LEAVES
                }

                else -> {
                    return null
                }
            }
            if (leaves.getOrNull(index++)?.elementType != T.LBRACK) return null
        }
        val subject =
            leaves
                .getOrNull(index)
                ?.text
                ?.removePrefix("$")
                ?.removePrefix("::")
        if (subject != "facts" && subject != "trusted") return null
        val prefix = if (path.isEmpty()) "" else path.asReversed().joinToString(".") + "."
        return OpenVoxCompletionPlace.FactName(prefix, subject == "trusted")
    }

    private fun previousMeaningfulLeaves(position: PsiElement): Sequence<PsiElement> =
        generateSequence(PsiTreeUtil.prevLeaf(position, true)) { PsiTreeUtil.prevLeaf(it, true) }
            .filter { it !is PsiWhiteSpace && it !is PsiComment && it.textLength > 0 }
            .take(LOOKBEHIND_LEAVES)

    private fun inside(
        element: PsiElement,
        type: IElementType,
    ): Boolean = generateSequence(element) { it.parent }.any { it.elementType == type }
}
