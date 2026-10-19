package software.barad1tos.puppet

import com.intellij.openapi.extensions.PluginId
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.indexing.ID
import software.barad1tos.puppet.index.OpenVoxDeclarationIndex
import software.barad1tos.puppet.index.OpenVoxHieraIndex
import software.barad1tos.puppet.index.OpenVoxRubyFunctionIndex

class OpenVoxPluginIdentityTest : UsefulTestCase() {
    fun testIndicesCanLoadWhileLegacyIdsRemainRegistered() {
        val legacy = listOf("declarations", "hiera", "rubyFunctions").map {
            LegacyId("net.blendbyte.openvox.$it")
        }
        try {
            val current = listOf(OpenVoxDeclarationIndex().name, OpenVoxHieraIndex().name, OpenVoxRubyFunctionIndex().name)
            current.zip(legacy).forEach { (newId, oldId) ->
                assertNotSame(oldId, newId)
                assertTrue(newId.name.startsWith("net.blendbyte.openvox-intellij."))
                assertFalse(oldId.uniqueId == newId.uniqueId)
            }
        } finally {
            legacy.forEach(ID<*, *>::unloadId)
        }
    }

    private class LegacyId(name: String) : ID<String, Void>(name, PluginId.getId("net.blendbyte.openvox"))
}
