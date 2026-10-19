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

    override fun getVersion(): Int = 2

    override fun dependsOnFileContent(): Boolean = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        FileBasedIndex.InputFilter { it.extension == "yaml" || it.extension == "yml" }

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { content ->
            HIERA_KEY_PATTERN
                .findAll(content.contentAsText)
                .map { match ->
                    match.groupValues.drop(1).first { it.isNotEmpty() }
                }.associateWith { null }
        }

    companion object {
        const val INDEX_NAME = "software.barad1tos.puppet-tools.hiera"
    }
}

val HIERA_INDEX_KEY: ID<String, Void> = ID.create(OpenVoxHieraIndex.INDEX_NAME)

private const val PARAMETER_PATH = "[a-z]\\w*(?:::[a-zA-Z_]\\w*)+"

/** A namespaced YAML mapping key: YAML ends a key only at a colon followed by a blank or the line end. */
private val HIERA_KEY_PATTERN =
    Regex(
        "(?m)^[ \\t]*(?:($PARAMETER_PATH)|\"($PARAMETER_PATH)\"|'($PARAMETER_PATH)')" +
            ":(?=[ \\t]|$)",
    )
