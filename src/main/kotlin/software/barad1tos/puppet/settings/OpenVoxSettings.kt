package software.barad1tos.puppet.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.util.SystemInfo
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
        return candidates.firstNotNullOfOrNull { findOnPath(it)?.absolutePath }
    }

    private fun findOnPath(baseName: String): File? {
        val names =
            if (SystemInfo.isWindows) {
                windowsExecutableExtensions().map { baseName + it } + baseName
            } else {
                listOf(baseName)
            }
        val directories = pathEntries("PATH", File.pathSeparator)
        val candidates = directories.flatMap { directory -> names.map { File(directory, it) } }
        return candidates.firstOrNull { it.isFile && it.canExecute() }
    }

    private fun windowsExecutableExtensions(): List<String> = pathEntries("PATHEXT", ";").map { it.lowercase() }

    private fun pathEntries(
        variable: String,
        separator: String,
    ): List<String> = (System.getenv(variable) ?: "").split(separator).filter { it.isNotBlank() }

    companion object {
        fun getInstance(): OpenVoxSettings = service()
    }
}
