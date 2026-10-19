package software.barad1tos.puppet.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.File

@Service(Service.Level.APP)
@State(name = "OpenVoxSettings", storages = [Storage("openvox.xml")])
class OpenVoxSettings : PersistentStateComponent<OpenVoxSettings> {
    var puppetExecutable: String = ""
    var puppetLintExecutable: String = ""
    var runParserValidate: Boolean = true
    var runPuppetLint: Boolean = true

    override fun getState(): OpenVoxSettings = this

    override fun loadState(state: OpenVoxSettings) = XmlSerializerUtil.copyBean(state, this)

    fun resolvedPuppet(): String? = resolve(puppetExecutable, "puppet", "openvox")

    fun resolvedPuppetLint(): String? = resolve(puppetLintExecutable, "puppet-lint")

    private fun resolve(
        configured: String,
        vararg candidates: String,
    ): String? {
        if (configured.isNotBlank()) return configured
        // EnvironmentUtil carries the login-shell environment. System.getenv() of an IDE started from the
        // Dock or a desktop launcher is the bare launcher PATH, without /opt/puppetlabs/bin or Homebrew.
        val lookup =
            ExecutableLookup(
                pathValue = EnvironmentUtil.getValue("PATH"),
                pathExtValue = EnvironmentUtil.getValue("PATHEXT"),
                isWindows = SystemInfo.isWindows,
            )
        return candidates.firstNotNullOfOrNull { lookup.find(it)?.absolutePath }
    }

    companion object {
        fun getInstance(): OpenVoxSettings = service()
    }
}

/**
 * Finds executables on a PATH value the way the platform does: only absolute directories are searched,
 * and on Windows only the PATHEXT spellings count, so an extensionless shim never shadows `puppet.bat`.
 *
 * Local instead of `PathEnvironmentVariableUtil`: `findExecutableInPathOnAnyOS` is scheduled for removal
 * and its replacement `findFirst` only exists from 2026.3, while the plugin supports 2025.2 (since-build 252).
 */
internal class ExecutableLookup(
    pathValue: String?,
    pathExtValue: String?,
    private val isWindows: Boolean,
) {
    private val directories: List<File> =
        pathValue
            .orEmpty()
            .split(File.pathSeparator)
            .map { File(it.trim().removeSurrounding("\"")) } // installers sometimes quote Windows entries
            .filter { it.isAbsolute && it.isDirectory }

    private val windowsExtensions: List<String> =
        pathExtValue
            .orEmpty()
            .split(';')
            .map { it.trim().lowercase() }
            .filter { it.startsWith('.') }
            .ifEmpty { DEFAULT_WINDOWS_EXTENSIONS }

    fun find(baseName: String): File? {
        val names = if (isWindows) windowsExtensions.map { baseName + it } else listOf(baseName)
        val candidates = directories.flatMap { directory -> names.map { File(directory, it) } }
        return candidates.firstOrNull { it.isFile && (isWindows || it.canExecute()) }
    }

    private companion object {
        val DEFAULT_WINDOWS_EXTENSIONS = listOf(".exe", ".bat", ".cmd", ".com")
    }
}
