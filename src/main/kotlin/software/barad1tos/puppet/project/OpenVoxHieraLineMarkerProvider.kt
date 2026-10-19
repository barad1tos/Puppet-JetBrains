package software.barad1tos.puppet.project

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import software.barad1tos.puppet.OpenVoxIcons
import software.barad1tos.puppet.index.HIERA_INDEX_KEY
import software.barad1tos.puppet.index.OpenVoxDeclarationKind
import software.barad1tos.puppet.psi.OpenVoxNamedElement
import software.barad1tos.puppet.psi.OpenVoxTypes as T

/** Caps the gutter popup; a parameter set in more data files than this is rare and the list stays readable. */
private const val MAX_HIERA_TARGETS = 20

class OpenVoxHieraLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (element.node.elementType != T.VARIABLE || element.parent.node.elementType != T.PARAMETER) return
        val owner =
            generateSequence(element.parent.parent) { it.parent }
                .filterIsInstance<OpenVoxNamedElement>()
                .firstOrNull { OpenVoxDeclarationKind.of(it) != null } ?: return
        if (OpenVoxDeclarationKind.of(owner) !in
            setOf(OpenVoxDeclarationKind.CLASS, OpenVoxDeclarationKind.DEFINE)
        ) {
            return
        }
        val key = "${owner.name}::${element.text.removePrefix("$")}"
        val project = element.project
        val manager = PsiManager.getInstance(project)
        val targets =
            FileBasedIndex
                .getInstance()
                .getContainingFiles(HIERA_INDEX_KEY, key, GlobalSearchScope.allScope(project))
                .asSequence()
                .filter { "/data/" in it.path.replace('\\', '/') }
                .take(MAX_HIERA_TARGETS)
                .mapNotNull(manager::findFile)
                .toList()
        if (targets.isEmpty()) return
        result +=
            NavigationGutterIconBuilder
                .create(OpenVoxIcons.FILE)
                .setTargets(targets)
                .setTooltipText("Set in Hiera data as $key")
                .createLineMarkerInfo(element)
    }
}
