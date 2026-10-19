package software.barad1tos.puppet

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import software.barad1tos.puppet.completion.OpenVoxCompletionContext
import software.barad1tos.puppet.completion.OpenVoxCompletionPlace

class OpenVoxCompletionContextTest : BasePlatformTestCase() {
    private fun placeAt(text: String): OpenVoxCompletionPlace {
        val offset = text.indexOf(CARET)
        myFixture.configureByText("site.pp", text.replace(CARET, DUMMY))
        val leaf = requireNotNull(myFixture.file.findElementAt(offset)) { "no leaf at the caret" }
        return OpenVoxCompletionContext.placeAt(leaf)
    }

    fun testFirstAttributeName() {
        assertEquals(OpenVoxCompletionPlace.AttributeName("file"), placeAt("file { '/etc/motd':\n  <caret>\n}"))
    }

    fun testAttributeNameAfterComma() {
        assertEquals(
            OpenVoxCompletionPlace.AttributeName("file"),
            placeAt("file { '/etc/motd':\n  ensure => file,\n  <caret>\n}"),
        )
    }

    fun testAttributeNameAfterNestedHashValue() {
        assertEquals(
            OpenVoxCompletionPlace.AttributeName("file"),
            placeAt("file { '/etc/motd':\n  content => epp('t', { 'a' => 1 }),\n  <caret>\n}"),
        )
    }

    fun testAttributeValue() {
        assertEquals(
            OpenVoxCompletionPlace.AttributeValue("service", "ensure"),
            placeAt("Service { 'nginx':\n  ensure => <caret>\n}"),
        )
    }

    fun testClassBodyIsStatement() {
        assertEquals(OpenVoxCompletionPlace.Statement, placeAt("class profile::web {\n  <caret>\n}"))
    }

    fun testParameterListExpectsDataType() {
        assertEquals(OpenVoxCompletionPlace.DataType, placeAt("define profile::vhost (<caret>) { }"))
    }

    fun testTypeParameterExpectsDataType() {
        assertEquals(OpenVoxCompletionPlace.DataType, placeAt("\$x = Optional[<caret>]"))
    }

    fun testTopLevelIsStatement() {
        assertEquals(OpenVoxCompletionPlace.Statement, placeAt("<caret>"))
    }

    fun testFactRoot() {
        assertEquals(OpenVoxCompletionPlace.FactName("", trusted = false), placeAt("\$x = \$facts[<caret>]"))
    }

    fun testNestedTrustedFact() {
        assertEquals(
            OpenVoxCompletionPlace.FactName("extensions.", trusted = true),
            placeAt("\$x = \$trusted['extensions'][<caret>]"),
        )
    }

    fun testDoubleQuotedFactKey() {
        assertEquals(
            OpenVoxCompletionPlace.FactName("os.", trusted = false),
            placeAt("\$x = \$facts[\"os\"][<caret>]"),
        )
    }

    fun testArrayElementIsStatement() {
        assertEquals(OpenVoxCompletionPlace.Statement, placeAt("\$packages = ['nginx', <caret>]"))
    }

    fun testFactPathWithVariableKeyIsNotAFactName() {
        assertFalse(placeAt("\$x = \$facts[\$key][<caret>]") is OpenVoxCompletionPlace.FactName)
    }

    private companion object {
        const val CARET = "<caret>"
        const val DUMMY = "openvoxdummy"
    }
}
