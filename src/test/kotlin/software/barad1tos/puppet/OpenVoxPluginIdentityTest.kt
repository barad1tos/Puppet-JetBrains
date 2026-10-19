package software.barad1tos.puppet

import junit.framework.TestCase
import software.barad1tos.puppet.index.OpenVoxDeclarationIndex
import software.barad1tos.puppet.index.OpenVoxHieraIndex
import software.barad1tos.puppet.index.OpenVoxRubyFunctionIndex

class OpenVoxPluginIdentityTest : TestCase() {
    fun testIndexNamesCarryThePluginIdAndDifferFromUpstream() {
        val names =
            listOf(
                OpenVoxDeclarationIndex.INDEX_NAME,
                OpenVoxHieraIndex.INDEX_NAME,
                OpenVoxRubyFunctionIndex.INDEX_NAME,
            )

        assertEquals("index names must be distinct", names.size, names.toSet().size)
        names.forEach { name ->
            assertTrue(name, name.startsWith("$PLUGIN_ID."))
            UPSTREAM_PLUGIN_IDS.forEach { upstream -> assertFalse(name, name.startsWith("$upstream.")) }
        }
    }

    fun testDescriptorDeclaresBothUpstreamPluginsIncompatible() {
        val descriptor = checkNotNull(javaClass.getResourceAsStream("/META-INF/plugin.xml")).bufferedReader().readText()

        UPSTREAM_PLUGIN_IDS.forEach { upstream ->
            assertTrue(upstream, descriptor.contains("<incompatible-with>$upstream</incompatible-with>"))
        }
    }

    private companion object {
        const val PLUGIN_ID = "software.barad1tos.puppet-tools"
        val UPSTREAM_PLUGIN_IDS = listOf("net.blendbyte.openvox", "net.blendbyte.openvox-intellij")
    }
}
