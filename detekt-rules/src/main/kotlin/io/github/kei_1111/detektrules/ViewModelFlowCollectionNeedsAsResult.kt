package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

private val USE_CASE_RECEIVER_REGEX = Regex("\\b[a-z]\\w*UseCase\\(")

class ViewModelFlowCollectionNeedsAsResult(config: Config) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Violates .claude/rules/error-handling.md — every ViewModel collector guards with " +
            ".asResult(); use collectAsResult()/prefetchAsResult() (copy from " +
            "ProfileViewModel.kt)",
        Debt.TWENTY_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val calleeName = expression.calleeExpression?.text
        if (calleeName != "collect" && calleeName != "launchIn") return

        val receiver = expression.getQualifiedExpressionForSelector()?.receiverExpression ?: return
        val receiverText = receiver.text
        val unguardedUseCaseCollection = isUseCaseRooted(expression, receiverText) &&
            receiver.rightmostCalleeName() != "asResult"
        if (unguardedUseCaseCollection) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }

    private fun isUseCaseRooted(expression: KtCallExpression, receiverText: String): Boolean =
        USE_CASE_RECEIVER_REGEX.containsMatchIn(receiverText) ||
            constructorUseCaseParameterNames(expression).any { name ->
                Regex("\\b${Regex.escape(name)}\\s*\\(").containsMatchIn(receiverText)
            }

    private fun constructorUseCaseParameterNames(expression: KtCallExpression): Set<String> {
        val containingClass = PsiTreeUtil.getParentOfType(expression, KtClass::class.java)
        val classes = if (containingClass != null) {
            listOf(containingClass)
        } else {
            PsiTreeUtil.findChildrenOfType(expression.containingKtFile, KtClass::class.java)
        }
        return classes
            .asSequence()
            .flatMap { it.primaryConstructor?.valueParameters.orEmpty().asSequence() }
            .filter { parameter ->
                parameter.typeReference?.text?.substringBefore('<')?.trim()?.endsWith("UseCase") == true
            }
            .mapNotNull { it.name }
            .toSet()
    }

    private fun KtExpression.rightmostCalleeName(): String? = when (this) {
        is KtDotQualifiedExpression ->
            (selectorExpression as? KtCallExpression)?.calleeExpression?.text

        is KtCallExpression -> calleeExpression?.text
        else -> null
    }
}
