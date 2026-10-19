package software.barad1tos.puppet.resolve

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import software.barad1tos.puppet.index.DECLARATION_INDEX_KEY
import software.barad1tos.puppet.index.OpenVoxDeclarationKind
import software.barad1tos.puppet.index.OpenVoxDeclarations
import software.barad1tos.puppet.index.RUBY_FUNCTION_INDEX_KEY
import software.barad1tos.puppet.psi.OpenVoxNamedElement

object OpenVoxResolver {
    fun find(
        project: Project,
        kind: OpenVoxDeclarationKind,
        name: String,
        scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
    ): List<OpenVoxNamedElement> {
        val normalised = name.removePrefix("::").lowercase()
        val key = OpenVoxDeclarations.keyFor(kind, normalised)
        val index = FileBasedIndex.getInstance()
        val manager = PsiManager.getInstance(project)

        return index
            .getContainingFiles(DECLARATION_INDEX_KEY, key, scope)
            .mapNotNull { manager.findFile(it) }
            .flatMap { OpenVoxDeclarations.declarationsIn(it) }
            .filter {
                OpenVoxDeclarationKind.of(it) == kind &&
                    it.name?.removePrefix("::")?.lowercase() == normalised
            }
    }

    fun rubyFunctionExists(
        project: Project,
        name: String,
    ): Boolean =
        FileBasedIndex
            .getInstance()
            .getContainingFiles(
                RUBY_FUNCTION_INDEX_KEY,
                name.removePrefix("::").lowercase(),
                GlobalSearchScope.allScope(project),
            ).isNotEmpty()

    fun allNames(
        project: Project,
        kind: OpenVoxDeclarationKind,
    ): List<String> {
        val prefix = "${kind.prefix}:"
        val names = mutableListOf<String>()
        FileBasedIndex.getInstance().processAllKeys(
            DECLARATION_INDEX_KEY,
            { key ->
                if (key.startsWith(prefix)) names += key.removePrefix(prefix)
                true
            },
            project,
        )
        return names
    }
}
