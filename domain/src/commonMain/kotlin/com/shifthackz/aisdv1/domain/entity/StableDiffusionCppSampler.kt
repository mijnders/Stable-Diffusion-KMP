package com.shifthackz.aisdv1.domain.entity

enum class StableDiffusionCppSampler(
    val key: String,
    val displayName: String,
    val nativeId: Int,
) {
    EULER_A("euler_a", "Euler A", 0),
    EULER("euler", "Euler", 1),
    HEUN("heun", "Heun", 2),
    DPM2("dpm2", "DPM2", 3),
    DPMPP2S_A("dpmpp2s_a", "DPM++ 2S A", 4),
    DPMPP2M("dpmpp2m", "DPM++ 2M", 5),
    IPNDM("ipndm", "IPNDM", 6),
    LCM("lcm", "LCM", 7),
    DDIM_TRAILING("ddim_trailing", "DDIM Trailing", 8),
    TCD("tcd", "TCD", 9);

    companion object {
        fun parse(value: String): StableDiffusionCppSampler =
            entries.firstOrNull { sampler ->
                sampler.key == value.trim().lowercase().replace(" ", "_")
            } ?: EULER
    }
}
