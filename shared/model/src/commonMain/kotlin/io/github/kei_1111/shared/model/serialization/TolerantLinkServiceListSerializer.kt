package io.github.kei_1111.shared.model.serialization

import io.github.kei_1111.shared.model.LinkService
import io.github.kei_1111.shared.model.LinkServiceType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

/** Keeps known links when a newer server sends an unknown service type. */
internal object TolerantLinkServiceListSerializer :
    JsonTransformingSerializer<ImmutableList<LinkService>>(
        ImmutableListSerializer(LinkService.serializer()),
    ) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonArray) return element

        return JsonArray(
            element.filterNot { it.hasUnknownEnumString("type", knownLinkServiceTypes) },
        )
    }
}

private val knownLinkServiceTypes = LinkServiceType.serializer().descriptor.wireNames()

private fun SerialDescriptor.wireNames(): Set<String> =
    (0 until elementsCount).mapTo(mutableSetOf()) { getElementName(it) }

private fun JsonElement.hasUnknownEnumString(key: String, known: Set<String>): Boolean {
    val value = (this as? JsonObject)?.get(key)
    return value is JsonPrimitive && value.isString && value.content !in known
}
