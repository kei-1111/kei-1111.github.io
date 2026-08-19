package io.github.kei_1111.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class KeiRuleSetProvider : RuleSetProvider {
    override val ruleSetId = "kei-rules"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            NoRunCatchingInRepositoryFlow(config),
            ViewModelFlowCollectionNeedsAsResult(config),
        ),
    )
}
