package software.barad1tos.puppet.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import software.barad1tos.puppet.OpenVoxIcons
import software.barad1tos.puppet.OpenVoxLanguage
import software.barad1tos.puppet.index.OpenVoxDeclarationKind
import software.barad1tos.puppet.knowledge.OpenVoxKnowledge
import software.barad1tos.puppet.resolve.OpenVoxResolver
import software.barad1tos.puppet.resolve.OpenVoxScope

class OpenVoxCompletionContributor : CompletionContributor() {
    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = DUMMY_IDENTIFIER
    }

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(OpenVoxLanguage),
            OpenVoxCompletionProvider,
        )
    }
}

private object OpenVoxCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val position = parameters.position
        result.addAllElements(lookupsFor(OpenVoxCompletionContext.placeAt(position), position).asIterable())
    }

    private fun lookupsFor(
        place: OpenVoxCompletionPlace,
        position: PsiElement,
    ): Sequence<LookupElement> {
        val project = position.project
        return when (place) {
            is OpenVoxCompletionPlace.AttributeName -> {
                attributeNames(place.resourceType)
            }

            is OpenVoxCompletionPlace.AttributeValue -> {
                attributeValues(place) + variables(position)
            }

            OpenVoxCompletionPlace.DataType -> {
                dataTypes() + declarations(project, TYPE_REFERENCES, ::capitalise)
            }

            OpenVoxCompletionPlace.ResourceType -> {
                resourceTypes() + declarations(project, listOf(OpenVoxDeclarationKind.DEFINE))
            }

            is OpenVoxCompletionPlace.FactName -> {
                factKeys(place)
            }

            OpenVoxCompletionPlace.Statement -> {
                functions() + declarations(project, STATEMENT_DECLARATIONS) + variables(position)
            }
        }
    }

    private fun attributeNames(resourceType: String): Sequence<LookupElement> =
        OpenVoxKnowledge.attributesFor(resourceType).asSequence().map { attribute ->
            LookupElementBuilder
                .create(attribute.name)
                .withTypeText(attribute.kind)
                .withIcon(OpenVoxIcons.FILE)
                .withInsertHandler(insertArrow)
        }

    private fun attributeValues(place: OpenVoxCompletionPlace.AttributeValue): Sequence<LookupElement> =
        OpenVoxKnowledge
            .attribute(place.resourceType, place.attributeName)
            ?.values
            .orEmpty()
            .asSequence()
            .map { LookupElementBuilder.create(it).withTypeText("value") }

    private fun dataTypes(): Sequence<LookupElement> =
        OpenVoxKnowledge.dataTypes.values.asSequence().map { type ->
            LookupElementBuilder.create(type.name).withTypeText("data type").withIcon(OpenVoxIcons.FILE)
        }

    private fun resourceTypes(): Sequence<LookupElement> =
        OpenVoxKnowledge.resourceTypes.values.asSequence().map { type ->
            LookupElementBuilder.create(type.name).withTypeText("resource type").withIcon(OpenVoxIcons.FILE)
        }

    private fun factKeys(place: OpenVoxCompletionPlace.FactName): Sequence<LookupElement> {
        val paths = if (place.trusted) TRUSTED_FACTS else OpenVoxKnowledge.facts.map { it.path }
        return paths
            .asSequence()
            .filter { it.startsWith(place.prefix) }
            .map { it.removePrefix(place.prefix).substringBefore('.') }
            .distinct()
            .map { LookupElementBuilder.create(it).withTypeText("fact") }
    }

    private fun functions(): Sequence<LookupElement> =
        OpenVoxKnowledge.functions.values.asSequence().map { function ->
            LookupElementBuilder
                .create(function.name)
                .withTailText(function.signatures.firstOrNull()?.let { "($it)" } ?: "()", true)
                .withTypeText(function.returnType.ifEmpty { "function" })
                .withIcon(OpenVoxIcons.FILE)
        }

    private fun variables(position: PsiElement): Sequence<LookupElement> =
        OpenVoxScope
            .declarationsVisibleFrom(position)
            .asSequence()
            .mapNotNull { it.name?.let { name -> "$$name" } }
            .distinct()
            .map { LookupElementBuilder.create(it).withTypeText("variable") }

    /** Project declarations of [kinds], labelled with each kind's presentable name. */
    private fun declarations(
        project: Project,
        kinds: List<OpenVoxDeclarationKind>,
        present: (String) -> String = { it },
    ): Sequence<LookupElement> =
        kinds.asSequence().flatMap { kind ->
            OpenVoxResolver.allNames(project, kind).asSequence().map { name ->
                LookupElementBuilder.create(present(name)).withTypeText(kind.presentableName)
            }
        }

    private fun capitalise(name: String): String =
        name.split("::").joinToString("::") { it.replaceFirstChar(Char::uppercaseChar) }

    private val TYPE_REFERENCES = listOf(OpenVoxDeclarationKind.TYPE_ALIAS, OpenVoxDeclarationKind.DEFINE)
    private val STATEMENT_DECLARATIONS =
        listOf(OpenVoxDeclarationKind.FUNCTION, OpenVoxDeclarationKind.CLASS, OpenVoxDeclarationKind.DEFINE)
    private val TRUSTED_FACTS = listOf("authenticated", "certname", "domain", "extensions", "external", "hostname")
}

/**
 * Lower-case on purpose: the platform default starts with a capital, and the caret classifier
 * reads a capitalised word as a data type.
 */
private const val DUMMY_IDENTIFIER = "openvoxdummy"

private const val ARROW = " => "

private val insertArrow =
    InsertHandler<LookupElement> { context, _ ->
        val document = context.document
        // tailOffset is a range marker that moves past text inserted at it, so pin the offset first.
        val nameEnd = context.tailOffset
        val tail = document.charsSequence.subSequence(nameEnd, document.textLength).take(ARROW.length)
        if (!tail.trimStart().startsWith("=>")) {
            document.insertString(nameEnd, ARROW)
            context.editor.caretModel.moveToOffset(nameEnd + ARROW.length)
        }
    }
