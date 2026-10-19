package software.barad1tos.puppet

import junit.framework.TestCase
import software.barad1tos.puppet.index.hieraKeys

class HieraKeysTest : TestCase() {
    fun testReadsPlainAndQuotedKeys() {
        val text = "a::b: 1\n\"c::d\": 2\n'e::f':\n  - x\nplain: 3\ng::h:value\n"
        assertEquals(setOf("a::b", "c::d", "e::f"), hieraKeys(text))
    }

    fun testSkipsBlockScalarWithHeaderOptionsAndComment() {
        val text = "a::motd: |2- # banner\n  x::y: z\n\n    deeper::key: z\nb::c: 1\n"
        assertEquals(setOf("a::motd", "b::c"), hieraKeys(text))
    }

    fun testSkipsBlockScalarInSequenceItem() {
        val text = "a::list:\n  - >\n    x::y: z\n  - plain\nb::c: 1\n"
        assertEquals(setOf("a::list", "b::c"), hieraKeys(text))
    }

    fun testPipeInsidePlainValueIsNotABlockScalar() {
        val text = "a::cmd: grep x | wc\n  b::c: 1\n"
        assertEquals(setOf("a::cmd", "b::c"), hieraKeys(text))
    }

    fun testHandlesWindowsLineEnds() {
        assertEquals(setOf("a::b", "c::d"), hieraKeys("a::b: |\r\n  x::y: z\r\nc::d: 1\r\n"))
    }
}
