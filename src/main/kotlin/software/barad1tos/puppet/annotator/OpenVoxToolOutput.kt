package software.barad1tos.puppet.annotator

import com.intellij.lang.annotation.HighlightSeverity

data class OpenVoxExternalMessage(
    val line: Int,
    val column: Int,
    val severity: HighlightSeverity,
    val message: String,
    val source: String,
)

/** Turns the text that `puppet parser validate` and `puppet-lint` print into editor messages. */
object OpenVoxToolOutput {
    /** The `--log-format` that [parseLint] understands. */
    const val LINT_LOG_FORMAT = "%{line}:%{column}:%{kind}:%{message}"
    private const val LINT_FIELD_COUNT = 4

    private val VALIDATE_ERROR = Regex("""Error:\s*(.+?)\s*\(file:[^,]+,\s*line:\s*(\d+),\s*column:\s*(\d+)\)""")

    fun parseValidate(output: String): List<OpenVoxExternalMessage> =
        VALIDATE_ERROR
            .findAll(output)
            .map { match ->
                OpenVoxExternalMessage(
                    line = match.groupValues[2].toIntOrNull() ?: 1,
                    column = match.groupValues[3].toIntOrNull() ?: 1,
                    severity = HighlightSeverity.ERROR,
                    message = match.groupValues[1].trim(),
                    source = "puppet",
                )
            }.toList()

    fun parseLint(output: String): List<OpenVoxExternalMessage> =
        output
            .lineSequence()
            .mapNotNull { line ->
                val parts = line.split(':', limit = LINT_FIELD_COUNT)
                if (parts.size < LINT_FIELD_COUNT) return@mapNotNull null
                OpenVoxExternalMessage(
                    line = parts[0].trim().toIntOrNull() ?: return@mapNotNull null,
                    column = parts[1].trim().toIntOrNull() ?: 1,
                    severity =
                        if (parts[2].trim().equals("error", ignoreCase = true)) {
                            HighlightSeverity.ERROR
                        } else {
                            HighlightSeverity.WARNING
                        },
                    message = parts[3].trim(),
                    source = "puppet-lint",
                )
            }.toList()
}
