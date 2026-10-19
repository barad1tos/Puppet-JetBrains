package software.barad1tos.puppet.annotator

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import software.barad1tos.puppet.settings.OpenVoxSettings
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

data class OpenVoxAnnotatorInput(
    val text: String,
)

class OpenVoxExternalAnnotator : ExternalAnnotator<OpenVoxAnnotatorInput, List<OpenVoxExternalMessage>>() {
    override fun collectInformation(
        file: PsiFile,
        editor: Editor,
        hasErrors: Boolean,
    ): OpenVoxAnnotatorInput? {
        if (hasErrors) return null
        val settings = OpenVoxSettings.getInstance()
        if (!settings.runParserValidate && !settings.runPuppetLint) return null
        return OpenVoxAnnotatorInput(editor.document.text)
    }

    override fun doAnnotate(collected: OpenVoxAnnotatorInput?): List<OpenVoxExternalMessage> {
        val input = collected ?: return emptyList()
        val settings = OpenVoxSettings.getInstance()

        val puppet = if (settings.runParserValidate) settings.resolvedPuppet() else null
        val lint = if (settings.runPuppetLint) settings.resolvedPuppetLint() else null
        if (puppet == null && lint == null) return emptyList()
        var temporary: Path? = null
        return try {
            temporary = Files.createTempFile("openvox-", ".pp")
            Files.writeString(temporary, input.text)
            buildList {
                puppet?.let { addAll(runParserValidate(it, temporary.toString())) }
                lint?.let { addAll(runPuppetLint(it, temporary.toString())) }
            }
        } catch (_: IOException) {
            emptyList()
        } finally {
            temporary?.let(::deleteQuietly)
        }
    }

    override fun apply(
        file: PsiFile,
        messages: List<OpenVoxExternalMessage>?,
        holder: AnnotationHolder,
    ) {
        val document = file.viewProvider.document ?: return
        messages.orEmpty().forEach { message ->
            val range = rangeFor(document, message) ?: return@forEach
            holder
                .newAnnotation(message.severity, "${message.source}: ${message.message}")
                .range(range)
                .create()
        }
    }

    private fun rangeFor(
        document: Document,
        message: OpenVoxExternalMessage,
    ): TextRange? {
        val line = (message.line - 1).coerceIn(0, (document.lineCount - 1).coerceAtLeast(0))
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val start = (lineStart + (message.column - 1).coerceAtLeast(0)).coerceIn(lineStart, lineEnd)
        return if (start >= lineEnd) TextRange(lineStart, lineEnd) else TextRange(start, lineEnd)
    }

    private fun runParserValidate(
        executable: String,
        path: String,
    ): List<OpenVoxExternalMessage> =
        runTool(GeneralCommandLine(executable, "parser", "validate", path))
            ?.let(OpenVoxToolOutput::parseValidate)
            .orEmpty()

    private fun runPuppetLint(
        executable: String,
        path: String,
    ): List<OpenVoxExternalMessage> =
        runTool(
            GeneralCommandLine(executable, "--log-format", OpenVoxToolOutput.LINT_LOG_FORMAT, path),
        )?.let(OpenVoxToolOutput::parseLint)
            .orEmpty()

    private fun deleteQuietly(path: Path) {
        try {
            Files.deleteIfExists(path)
        } catch (_: IOException) {
            // Best effort: a leftover file in the system temp directory is harmless.
        }
    }
}

private const val TOOL_TIMEOUT_MS = 10_000

private val LOG = logger<OpenVoxExternalAnnotator>()

/** Executables whose failure is already in the log; the annotator runs on every edit, so each is reported once. */
private val reportedTools: MutableSet<String> = ConcurrentHashMap.newKeySet()

/**
 * Runs [command] for at most [TOOL_TIMEOUT_MS], killing it as soon as [indicator] is cancelled.
 * Returns its combined output, or null when the tool cannot start or times out;
 * either failure is logged once per executable.
 */
internal fun runTool(
    command: GeneralCommandLine,
    indicator: ProgressIndicator = ProgressManager.getGlobalProgressIndicator() ?: EmptyProgressIndicator(),
): String? {
    val output =
        try {
            CapturingProcessHandler(command.withRedirectErrorStream(true))
                .runProcessWithProgressIndicator(indicator, TOOL_TIMEOUT_MS)
        } catch (exception: ExecutionException) {
            reportFailure(command, exception.message ?: exception.javaClass.simpleName)
            return null
        }
    indicator.checkCanceled()
    if (output.isTimeout) {
        reportFailure(command, "no answer within $TOOL_TIMEOUT_MS ms")
        return null
    }
    return output.stdout
}

private fun reportFailure(
    command: GeneralCommandLine,
    reason: String,
) {
    if (reportedTools.add(command.exePath)) {
        LOG.warn("${command.exePath} did not run ($reason); its findings are missing from the editor")
    }
}
