package com.shifthackz.aisdv1.presentation.screen

import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.entity.StableDiffusionCppSampler

internal fun PreferenceManager.getSelectedSdxlSampler(): StableDiffusionCppSampler =
    StableDiffusionCppSampler.parse(
        getLocalSdxlSampler(
            modelId = localSdxlModelId,
            customModelPath = localSdxlCustomModelPath,
        ),
    )

internal fun PreferenceManager.saveSelectedSdxlSampler(sampler: StableDiffusionCppSampler): Boolean =
    setLocalSdxlSampler(
        modelId = localSdxlModelId,
        customModelPath = localSdxlCustomModelPath,
        sampler = sampler.key,
    )
