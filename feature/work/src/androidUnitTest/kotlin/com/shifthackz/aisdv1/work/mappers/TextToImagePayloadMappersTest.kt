package com.shifthackz.aisdv1.work.mappers

import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import org.junit.Assert
import org.junit.Test

class TextToImagePayloadMappersTest {

    @Test
    fun `given SDXL sampler and backend, background payload round trips both selections`() {
        val payload = createPayload().copy(
            sdxlBackend = SdxlBackend.VULKAN_ONLY,
            sdxlSampler = "lcm",
        )

        val restored = requireNotNull(payload.toByteArray().toTextToImagePayload())

        Assert.assertEquals(SdxlBackend.VULKAN_ONLY, restored.sdxlBackend)
        Assert.assertEquals("lcm", restored.sdxlSampler)
    }

    @Test
    fun `given queued payload without sampler field, defaults to Euler`() {
        val serialized = createPayload()
            .copy(sdxlSampler = "lcm")
            .toByteArray()
            .decodeToString()
        val legacySerialized = serialized.replace("\"sdxlSampler\":\"lcm\",", "")
        Assert.assertNotEquals(serialized, legacySerialized)

        val restored = legacySerialized.encodeToByteArray().toTextToImagePayload()

        Assert.assertEquals("euler", restored?.sdxlSampler)
    }

    private fun createPayload() = TextToImagePayload(
        prompt = "test",
        negativePrompt = "",
        samplingSteps = 6,
        cfgScale = 1.5f,
        width = 512,
        height = 512,
        restoreFaces = false,
        seed = "42",
        subSeed = "",
        subSeedStrength = 0f,
        sampler = "",
        nsfw = false,
        batchCount = 1,
        style = null,
        quality = null,
        openAiModel = null,
        stabilityAiClipGuidance = null,
        stabilityAiStylePreset = null,
    )
}
