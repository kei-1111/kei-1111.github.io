package org.example.project.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.model.Tool
import org.example.project.model.ToolSet
import org.example.project.ui.theme.dimensions.IconSizes
import org.example.project.ui.theme.dimensions.Paddings
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToolsSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        SectionTitle(
            title = "ツール",
        )
        SectionContent(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Paddings.Large),
                ) {
                    ToolSet.tools.forEach { tool ->
                        ToolIcon(
                            tool = tool,
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun ToolIcon(
    tool: Tool,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(tool.image),
        contentDescription = null,
        modifier = modifier.size(IconSizes.Small),
    )
}
