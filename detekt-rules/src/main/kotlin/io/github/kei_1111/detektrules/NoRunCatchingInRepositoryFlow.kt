package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

class NoRunCatchingInRepositoryFlow(config: Config) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Violates .claude/rules/error-handling.md — Prohibited Patterns; return a plain Flow " +
            "and let .asResult() handle failures at the ViewModel boundary (copy from " +
            "ProfileRepository.kt)",
        Debt.TWENTY_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "runCatching") return
        report(CodeSmell(issue, Entity.from(expression), issue.description))
    }
}
