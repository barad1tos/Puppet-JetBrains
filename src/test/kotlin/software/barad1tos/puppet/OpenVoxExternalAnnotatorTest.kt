package software.barad1tos.puppet

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import software.barad1tos.puppet.settings.OpenVoxSettings
import java.io.File
import java.nio.file.Files

class OpenVoxExternalAnnotatorTest : BasePlatformTestCase() {
    private lateinit var tools: File
    private lateinit var saved: OpenVoxSettings

    override fun setUp() {
        super.setUp()
        tools = Files.createTempDirectory("puppet-tools-annotator").toFile()
        saved = OpenVoxSettings().also { it.loadState(OpenVoxSettings.getInstance()) }
    }

    override fun tearDown() {
        try {
            OpenVoxSettings.getInstance().loadState(saved)
            tools.deleteRecursively()
        } finally {
            super.tearDown()
        }
    }

    /** Stands in for `puppet`: prints what `puppet parser validate <file>` prints for a syntax error. */
    private fun fakePuppet(): String {
        val script = File(tools, "puppet")
        script.writeText(
            "#!/bin/sh\n" +
                "echo \"Error: Could not parse for environment production: Syntax error at '}' " +
                "(file: $3, line: 2, column: 3)\"\n",
        )
        script.setExecutable(true)
        return script.absolutePath
    }

    private fun useValidator(executable: String) {
        OpenVoxSettings.getInstance().apply {
            puppetExecutable = executable
            runParserValidate = true
            runPuppetLint = false
        }
    }

    private fun validatorMessages(): List<String> =
        myFixture
            .doHighlighting(HighlightSeverity.ERROR)
            .mapNotNull { it.description }
            .filter { it.startsWith("puppet: ") }

    fun testParserValidateErrorIsShownOnItsLine() {
        useValidator(fakePuppet())
        myFixture.configureByText("site.pp", "class demo {\n  notify { 'x': }\n}\n")

        val infos =
            myFixture.doHighlighting(HighlightSeverity.ERROR).filter {
                it.description?.startsWith("puppet: ") ==
                    true
            }

        assertEquals(
            listOf("puppet: Could not parse for environment production: Syntax error at '}'"),
            infos.map {
                it.description
            },
        )
        val line = myFixture.editor.document.getLineNumber(infos.single().startOffset)
        assertEquals(1, line)
    }

    fun testMissingValidatorShowsNoErrorsInsteadOfFailing() {
        useValidator(File(tools, "absent-puppet").absolutePath)
        myFixture.configureByText("site.pp", "class demo {\n  notify { 'x': }\n}\n")

        assertEmpty(validatorMessages())
    }
}
