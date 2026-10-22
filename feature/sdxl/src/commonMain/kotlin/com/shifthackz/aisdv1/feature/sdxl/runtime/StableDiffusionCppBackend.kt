package com.shifthackz.aisdv1.feature.sdxl.runtime

enum class StableDiffusionCppBackend(val nativeId: Int) {
    AUTO(0),
    CPU(1),
    OPEN_CL(2),
    VULKAN(3),
    VULKAN_ONLY(4),
}
