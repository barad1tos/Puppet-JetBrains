package software.barad1tos.puppet.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import software.barad1tos.puppet.EppLanguage
import software.barad1tos.puppet.OpenVoxLanguage
import software.barad1tos.puppet.lexer.EppLexerAdapter
import software.barad1tos.puppet.lexer.OpenVoxLexerAdapter
import software.barad1tos.puppet.psi.EppFile
import software.barad1tos.puppet.psi.OpenVoxFile
import software.barad1tos.puppet.psi.OpenVoxTokenSets
import software.barad1tos.puppet.psi.OpenVoxTypes

class OpenVoxParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = OpenVoxLexerAdapter()

    override fun createParser(project: Project?): PsiParser = OpenVoxParser()

    override fun getFileNodeType(): IFileElementType = OPENVOX_FILE

    override fun getCommentTokens(): TokenSet = OpenVoxTokenSets.PARSER_SKIPPED

    override fun getStringLiteralElements(): TokenSet = OpenVoxTokenSets.STRINGS

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun createElement(node: ASTNode): PsiElement = OpenVoxTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = OpenVoxFile(viewProvider)
}

class EppParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = EppLexerAdapter()

    override fun createParser(project: Project?): PsiParser = EppPsiParser()

    override fun getFileNodeType(): IFileElementType = EPP_FILE

    override fun getCommentTokens(): TokenSet = OpenVoxTokenSets.PARSER_SKIPPED

    override fun getStringLiteralElements(): TokenSet = OpenVoxTokenSets.STRINGS

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun createElement(node: ASTNode): PsiElement = OpenVoxTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = EppFile(viewProvider)
}

private val OPENVOX_FILE = IFileElementType(OpenVoxLanguage)
private val EPP_FILE = IFileElementType(EppLanguage)
