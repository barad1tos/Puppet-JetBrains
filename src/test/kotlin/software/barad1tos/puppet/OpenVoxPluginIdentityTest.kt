package software.barad1tos.puppet

import com.intellij.openapi.extensions.PluginId
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.indexing.ID
import software.barad1tos.puppet.index.OpenVoxDeclarationIndex
import software.barad1tos.puppet.index.OpenVoxHieraIndex
import software.barad1tos.puppet.index.OpenVoxRubyFunctionIndex

class OpenVoxPluginIdentityTest : UsefulTestCase() {
    fun testIndicesCanLoadWhileUpstreamIdsRemainRegistered() {
        val upstream = listOf("declarations", "hiera", "rubyFunctions").flatMap { indexName ->
            UPSTREAM_PLUGIN_IDS.map { pluginId -> UpstreamId("$pluginId.$indexName", pluginId) }
        }
        try {
            val current = listOf(OpenVoxDeclarationIndex().name, OpenVoxHieraIndex().name, OpenVoxRubyFunctionIndex().name)
            current.forEach { newId ->
                assertTrue(newId.name.startsWith("$PLUGIN_ID."))
                upstream.forEach { oldId ->
                    assertNotSame(oldId, newId)
                    assertFalse(oldId.uniqueId == newId.uniqueId)
                }
            }
        } finally {
            upstream.forEach(ID<*, *>::unloadId)
        }
    }

    private class UpstreamId(name: String, pluginId: String) : ID<String, Void>(name, PluginId.getId(pluginId))

    private companion object {
        const val PLUGIN_ID = "software.barad1tos.puppet-tools"
        val UPSTREAM_PLUGIN_IDS = listOf("net.blendbyte.openvox", "net.blendbyte.openvox-intellij")
    }
}
