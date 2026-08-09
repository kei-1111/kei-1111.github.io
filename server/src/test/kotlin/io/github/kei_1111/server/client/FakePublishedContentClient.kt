package io.github.kei_1111.server.client

import io.github.kei_1111.shared.model.Readme
import io.github.kei_1111.shared.model.TerminalTextCommands
import io.github.kei_1111.shared.model.Works

internal class FakePublishedContentClient(
    private val works: PublishedResult<Works>? = PublishedResult.Missing,
    private val profile: PublishedResult<PublishedProfile>? = PublishedResult.Missing,
    private val readme: PublishedResult<Readme>? = PublishedResult.Missing,
    private val terminalCommands: PublishedResult<TerminalTextCommands>? = PublishedResult.Missing,
) : PublishedContentClient {
    override suspend fun fetchWorks(): PublishedResult<Works>? = works
    override suspend fun fetchProfile(): PublishedResult<PublishedProfile>? = profile
    override suspend fun fetchReadme(): PublishedResult<Readme>? = readme
    override suspend fun fetchTerminalCommands(): PublishedResult<TerminalTextCommands>? = terminalCommands
}
