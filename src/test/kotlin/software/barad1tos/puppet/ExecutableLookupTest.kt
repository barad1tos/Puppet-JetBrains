package software.barad1tos.puppet

import com.intellij.openapi.util.SystemInfo
import junit.framework.TestCase
import software.barad1tos.puppet.settings.ExecutableLookup
import java.io.File
import java.nio.file.Files

class ExecutableLookupTest : TestCase() {
    private lateinit var root: File

    override fun setUp() {
        super.setUp()
        root = Files.createTempDirectory("puppet-tools-path").toFile()
    }

    override fun tearDown() {
        root.deleteRecursively()
        super.tearDown()
    }

    fun testFindsExecutableAndSkipsRelativeEntries() {
        val bin = directory("bin")
        val puppet = file(bin, "puppet", executable = true)
        val path = listOf("relative-bin", bin.path).joinToString(File.pathSeparator)

        assertEquals(puppet, ExecutableLookup(path, null, isWindows = false).find("puppet"))
    }

    fun testSkipsFilesWithoutExecuteBit() {
        if (SystemInfo.isWindows) return // every file is executable on Windows; the PATHEXT tests cover it
        val first = directory("first")
        file(first, "puppet", executable = false)
        val second = directory("second")
        val puppet = file(second, "puppet", executable = true)
        val path = listOf(first.path, second.path).joinToString(File.pathSeparator)

        assertEquals(puppet, ExecutableLookup(path, null, isWindows = false).find("puppet"))
    }

    fun testReturnsNullWhenPathIsEmptyOrNameIsAbsent() {
        val bin = directory("bin")
        file(bin, "puppet-lint", executable = true)

        assertNull(ExecutableLookup(null, null, isWindows = false).find("puppet"))
        assertNull(ExecutableLookup(bin.path, null, isWindows = false).find("puppet"))
    }

    fun testWindowsUsesPathExtSpellingsAndIgnoresBareName() {
        val first = directory("first")
        file(first, "puppet", executable = true)
        val second = directory("second")
        val batch = file(second, "puppet.bat", executable = true)
        val path = listOf(first.path, second.path).joinToString(File.pathSeparator)

        assertEquals(batch, ExecutableLookup(path, ".EXE;.BAT;.CMD", isWindows = true).find("puppet"))
    }

    fun testWindowsFallsBackToDefaultExtensionsWhenPathExtIsUnusable() {
        val bin = directory("bin")
        val exe = file(bin, "puppet.exe", executable = true)

        assertEquals(exe, ExecutableLookup(bin.path, "EXE;garbage", isWindows = true).find("puppet"))
    }

    private fun directory(name: String): File = File(root, name).also { assertTrue(it.mkdir()) }

    private fun file(
        parent: File,
        name: String,
        executable: Boolean,
    ): File =
        File(parent, name).also {
            it.writeText("#!/bin/sh\n")
            assertTrue(it.setExecutable(executable))
        }
}
