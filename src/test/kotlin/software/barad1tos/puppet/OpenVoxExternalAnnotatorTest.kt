package software.barad1tos.puppet

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.concurrency.AppExecutorUtil
import software.barad1tos.puppet.annotator.OpenVoxToolOutput
import software.barad1tos.puppet.annotator.runTool
import software.barad1tos.puppet.settings.OpenVoxSettings
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

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

    private fun script(
        name: String,
        body: String,
    ): String {
        val script = File(tools, name)
        script.writeText("#!/bin/sh\n$body\n")
        script.setExecutable(true)
        return script.absolutePath
    }

    /** Stands in for `puppet`: prints what `puppet parser validate <file>` prints for a syntax error. */
    private fun fakePuppet(): String =
        script(
            "puppet",
            "echo \"Error: Could not parse for environment production: Syntax error at '}' " +
                "(file: $3, line: 2, column: 3)\"",
        )

    /** Stands in for `puppet-lint`: answers only when called with the log format the parser expects. */
    private fun fakePuppetLint(): String =
        script(
            "puppet-lint",
            "[ \"$2\" = '${OpenVoxToolOutput.LINT_LOG_FORMAT}' ] && " +
                "echo '2:3:warning:indentation of => is not properly aligned'",
        )

    private fun useTools(
        puppet: String? = null,
        lint: String? = null,
    ) {
        OpenVoxSettings.getInstance().apply {
            puppetExecutable = puppet.orEmpty()
            puppetLintExecutable = lint.orEmpty()
            runParserValidate = puppet != null
            runPuppetLint = lint != null
        }
    }

    private fun toolMessages(
        severity: HighlightSeverity,
        source: String,
    ): List<HighlightInfo> =
        myFixture
            .doHighlighting(severity)
            .filter { it.description?.startsWith("$source: ") == true }

    /** Line and column, both zero-based, where the annotation starts. */
    private fun HighlightInfo.position(): Pair<Int, Int> {
        val document = myFixture.editor.document
        val line = document.getLineNumber(startOffset)
        return line to startOffset - document.getLineStartOffset(line)
    }

    fun testParserValidateErrorIsShownAtItsPosition() {
        useTools(puppet = fakePuppet())
        myFixture.configureByText("site.pp", "class demo {\n  notify { 'x': }\n}\n")

        val infos = toolMessages(HighlightSeverity.ERROR, "puppet")

        assertEquals(
            listOf("puppet: Could not parse for environment production: Syntax error at '}'"),
            infos.map { it.description },
        )
        assertEquals(1 to 2, infos.single().position())
    }

    fun testPuppetLintWarningIsShownAtItsPosition() {
        useTools(lint = fakePuppetLint())
        myFixture.configureByText("site.pp", "class demo {\n  notify { 'x': }\n}\n")

        val infos = toolMessages(HighlightSeverity.WARNING, "puppet-lint")

        assertEquals(listOf("puppet-lint: indentation of => is not properly aligned"), infos.map { it.description })
        assertEquals(HighlightSeverity.WARNING, infos.single().severity)
        assertEquals(1 to 2, infos.single().position())
    }

    fun testCancelledPassStopsTheToolPromptly() {
        val slowTool = script("slow-puppet", "sleep 30")
        val indicator = EmptyProgressIndicator()
        AppExecutorUtil.getAppScheduledExecutorService().schedule({ indicator.cancel() }, 300, TimeUnit.MILLISECONDS)
        val started = System.nanoTime()

        assertThrows(ProcessCanceledException::class.java) { runTool(GeneralCommandLine(slowTool), indicator) }

        val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
        assertTrue("the cancelled tool kept running for $elapsedMs ms", elapsedMs < 5_000)
    }

    fun testMissingValidatorShowsNoErrorsInsteadOfFailing() {
        useTools(puppet = File(tools, "absent-puppet").absolutePath)
        myFixture.configureByText("site.pp", "class demo {\n  notify { 'x': }\n}\n")

        assertEmpty(toolMessages(HighlightSeverity.ERROR, "puppet"))
    }
}
