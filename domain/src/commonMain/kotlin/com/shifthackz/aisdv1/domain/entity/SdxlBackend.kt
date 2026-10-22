package com.shifthackz.aisdv1.domain.entity

enum class SdxlBackend(
    val key: String,
    val displayName: String,
) {
    AUTO("auto", "AUTO"),
    CPU("cpu", "CPU"),
    OPEN_CL("opencl", "OpenCL"),
    VULKAN("vulkan", "Vulkan (Hybrid)"),
    VULKAN_ONLY("vulkan_only", "Vulkan Only (Experimental)"),
    ;

    val nativeId: Int
        get() = when (this) {
            AUTO -> 0
            CPU -> 1
            OPEN_CL -> 2
            VULKAN -> 3
            VULKAN_ONLY -> 4
        }

    companion object {
        fun parse(value: String?): SdxlBackend {
            return entries.firstOrNull { backend ->
                backend.key.equals(value, ignoreCase = true) ||
                    backend.name.equals(value, ignoreCase = true)
            } ?: AUTO
        }
    }
}
