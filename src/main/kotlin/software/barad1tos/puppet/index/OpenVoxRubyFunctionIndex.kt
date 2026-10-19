package software.barad1tos.puppet.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

class OpenVoxRubyFunctionIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = RUBY_FUNCTION_INDEX_KEY

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        FileBasedIndex.InputFilter { file ->
            file.extension == "rb" && ("/puppet/functions/" in file.path || "/puppet/parser/functions/" in file.path)
        }

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { content ->
            val text = content.contentAsText
            val names = mutableSetOf<String>()
            MODERN_FUNCTION.findAll(text).forEach { names += it.groupValues[1].lowercase() }
            LEGACY_FUNCTION.findAll(text).forEach { names += it.groupValues[1].lowercase() }
            names.associateWith { null }
        }

    companion object {
        const val INDEX_NAME = "software.barad1tos.puppet-tools.rubyFunctions"
    }
}

val RUBY_FUNCTION_INDEX_KEY: ID<String, Void> = ID.create(OpenVoxRubyFunctionIndex.INDEX_NAME)

private val MODERN_FUNCTION = Regex("""create_function\(\s*:'?([\w:]+)'?""")
private val LEGACY_FUNCTION = Regex("""newfunction\(\s*:(\w+)""")
