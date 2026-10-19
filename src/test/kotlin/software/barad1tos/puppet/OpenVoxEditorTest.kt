package software.barad1tos.puppet

import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.lang.LanguageBraceMatching
import com.intellij.lang.findUsages.LanguageFindUsages
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import software.barad1tos.puppet.highlighting.OpenVoxColorSettingsPage
import software.barad1tos.puppet.psi.OpenVoxNamedElement

class OpenVoxEditorTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        // LanguageExtension caches per Language object, and the language objects outlive each test application:
        // a ParsingTestCase run earlier in the JVM leaves a cached "no brace matcher" behind.
        LanguageBraceMatching.INSTANCE.clearCache(OpenVoxLanguage)
    }

    private fun matchedBraceOffset(
        text: String,
        open: Int,
    ): Int {
        myFixture.configureByText("site.pp", text)
        val iterator = (myFixture.editor as EditorEx).highlighter.createIterator(open)
        assertTrue(
            "no matching brace for offset $open",
            BraceMatchingUtil.matchBrace(text, myFixture.file.fileType, iterator, true),
        )
        return iterator.start
    }

    fun testResourceBodyBraceMatchesItsClosingBrace() {
        val text = "file { '/etc/motd':\n  content => { 'a' => 1 },\n}\n"
        assertEquals(text.lastIndexOf('}'), matchedBraceOffset(text, text.indexOf('{')))
    }

    fun testSelectorBraceMatchesItsClosingBrace() {
        val text = "\$enable = \$ensure ? { 'running' => true, default => false }\n"
        assertEquals(text.lastIndexOf('}'), matchedBraceOffset(text, text.indexOf('{')))
    }

    fun testTypingOpeningParenthesisInsertsItsPair() {
        myFixture.configureByText("site.pp", "\$parts = split<caret>\n")
        myFixture.type('(')
        myFixture.checkResult("\$parts = split(<caret>)\n")
    }

    fun testMultiLineHeredocFolds() {
        myFixture.configureByText(
            "site.pp",
            "\$motd = @(END)\n  Managed by Puppet.\n  Do not edit.\n  | END\n",
        )
        EditorTestUtil.buildInitialFoldingsInBackground(myFixture.editor)
        val placeholders =
            myFixture.editor.foldingModel.allFoldRegions
                .map { it.placeholderText }
        assertTrue("heredoc body was not folded: $placeholders", "…" in placeholders)
    }

    fun testEveryHighlightedTokenInTheColourDemoIsCustomisable() {
        val page = OpenVoxColorSettingsPage()
        val customisable = page.attributeDescriptors.map { it.key }.toSet()
        val lexer = page.highlighter.highlightingLexer
        lexer.start(page.demoText)
        val painted = mutableSetOf<TextAttributesKey>()
        while (true) {
            val token = lexer.tokenType ?: break
            painted += page.highlighter.getTokenHighlights(token)
            lexer.advance()
        }
        assertEmpty("colours shown in the demo but missing from the settings page", painted - customisable)
    }

    fun testFindUsagesNamesTheDeclarationKind() {
        myFixture.configureByText("site.pp", "define site::vhost (String \$docroot) { }\n")
        val define = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, OpenVoxNamedElement::class.java))
        val provider = LanguageFindUsages.INSTANCE.forLanguage(OpenVoxLanguage)

        assertEquals("defined type", provider.getType(define))
        assertEquals("site::vhost", provider.getNodeText(define, true))
    }
}
