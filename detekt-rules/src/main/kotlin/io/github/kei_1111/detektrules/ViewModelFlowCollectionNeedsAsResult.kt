package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
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
        val receiverText = expression.getQualifiedExpressionForSelector()?.receiverExpression?.text
        val unguardedUseCaseCollection = (calleeName == "collect" || calleeName == "launchIn") &&
            receiverText != null &&
            USE_CASE_RECEIVER_REGEX.containsMatchIn(receiverText) &&
            !receiverText.contains("asResult(")
        if (unguardedUseCaseCollection) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }
}
