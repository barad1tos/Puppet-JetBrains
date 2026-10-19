package software.barad1tos.puppet.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.tree.TokenSet
import software.barad1tos.puppet.psi.OpenVoxTypes as T

class OpenVoxQuoteHandler : SimpleTokenSetQuoteHandler(
    TokenSet.create(T.SQ_STRING, T.DQ_START, T.DQ_END, T.DQ_TEXT),
)
