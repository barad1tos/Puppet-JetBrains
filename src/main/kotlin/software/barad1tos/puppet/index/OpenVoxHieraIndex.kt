package software.barad1tos.puppet.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

class OpenVoxHieraIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = HIERA_INDEX_KEY

    override fun getVersion(): Int = 3

    override fun dependsOnFileContent(): Boolean = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        FileBasedIndex.InputFilter { it.extension == "yaml" || it.extension == "yml" }

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { content -> hieraKeys(content.contentAsText).associateWith { null } }

    companion object {
        const val INDEX_NAME = "software.barad1tos.puppet-tools.hiera"
    }
}

val HIERA_INDEX_KEY: ID<String, Void> = ID.create(OpenVoxHieraIndex.INDEX_NAME)

private const val PARAMETER_PATH = "[a-z]\\w*(?:::[a-zA-Z_]\\w*)+"

/** A namespaced YAML mapping key: YAML ends a key only at a colon followed by a blank or the line end. */
private val HIERA_KEY_PATTERN =
    Regex(
        "^[ \\t]*(?:($PARAMETER_PATH)|\"($PARAMETER_PATH)\"|'($PARAMETER_PATH)')" +
            ":(?=[ \\t]|$)",
    )

/** A `|` or `>` block scalar header in value position, with optional chomping, indentation and comment. */
private val BLOCK_SCALAR_START = Regex("""(?::|^[ \t]*-|[ \t]-)[ \t]+[|>][1-9+-]{0,2}[ \t]*(?:#.*)?$""")

/**
 * Namespaced mapping keys in a Hiera data file. Lines inside a block scalar are text, not keys:
 * a scalar runs over blank lines and lines indented deeper than the line that opened it.
 */
internal fun hieraKeys(text: CharSequence): Set<String> {
    val keys = mutableSetOf<String>()
    var scalarIndent: Int? = null
    for (line in text.lineSequence()) {
        val indent = line.indexOfFirst { it != ' ' && it != '\t' }
        val isScalarText = indent < 0 || scalarIndent?.let { indent > it } == true
        if (!isScalarText) {
            scalarIndent = indent.takeIf { BLOCK_SCALAR_START.containsMatchIn(line) }
            HIERA_KEY_PATTERN.find(line)?.let { match -> keys += match.groupValues.drop(1).first { it.isNotEmpty() } }
        }
    }
    return keys
}
