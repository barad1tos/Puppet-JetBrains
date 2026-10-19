package software.barad1tos.puppet

import com.intellij.lang.annotation.HighlightSeverity
import junit.framework.TestCase
import software.barad1tos.puppet.annotator.OpenVoxExternalMessage
import software.barad1tos.puppet.annotator.OpenVoxToolOutput

class OpenVoxToolOutputTest : TestCase() {
    fun testParsesValidateErrorPosition() {
        val output =
            "Error: Could not parse for environment production: Syntax error at '}' " +
                "(file: /tmp/openvox-1.pp, line: 3, column: 7)\n"

        assertEquals(
            listOf(
                OpenVoxExternalMessage(
                    line = 3,
                    column = 7,
                    severity = HighlightSeverity.ERROR,
                    message = "Could not parse for environment production: Syntax error at '}'",
                    source = "puppet",
                ),
            ),
            OpenVoxToolOutput.parseValidate(output),
        )
    }

    fun testValidateWithoutErrorsYieldsNothing() {
        assertEquals(emptyList<OpenVoxExternalMessage>(), OpenVoxToolOutput.parseValidate(""))
    }

    fun testParsesLintKindsAndKeepsColonsInMessage() {
        val output = "2:5:warning:indentation of => is not properly aligned\n4:1:error:found 'x': unquoted\n"

        val messages = OpenVoxToolOutput.parseLint(output)

        assertEquals(
            listOf(2 to HighlightSeverity.WARNING, 4 to HighlightSeverity.ERROR),
            messages.map { it.line to it.severity },
        )
        assertEquals("found 'x': unquoted", messages[1].message)
    }

    fun testSkipsLintLinesWithoutLineNumber() {
        assertEquals(emptyList<OpenVoxExternalMessage>(), OpenVoxToolOutput.parseLint("WARNING: plugin x:y:z\n\n"))
    }
}
