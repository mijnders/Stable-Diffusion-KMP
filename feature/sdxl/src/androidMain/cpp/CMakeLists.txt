cmake_minimum_required(VERSION 3.22.1)

project(sdai_sdcpp_runtime LANGUAGES C CXX)

option(SDAI_SDCPP_ENABLE_OPENCL "Build stable-diffusion.cpp with OpenCL backend" ON)
option(SDAI_SDCPP_ENABLE_VULKAN "Build stable-diffusion.cpp with Vulkan backend" ON)

set(SDAI_SDCPP_VENDOR_DIR ${CMAKE_CURRENT_LIST_DIR}/vendor)
set(SDAI_SDCPP_STABLE_DIFFUSION_CPP_VENDOR_DIR
    ${SDAI_SDCPP_VENDOR_DIR}/stable-diffusion.cpp
)
set(SDAI_SDCPP_VULKAN_HEADERS_DIR ${SDAI_SDCPP_VENDOR_DIR}/Vulkan-Headers)
set(SDAI_SDCPP_SPIRV_HEADERS_DIR ${SDAI_SDCPP_VENDOR_DIR}/SPIRV-Headers)
set(SDAI_SDCPP_OPENCL_HEADERS_DIR ${SDAI_SDCPP_VENDOR_DIR}/OpenCL-Headers)
set(SDAI_SDCPP_OPENCL_ICD_LOADER_DIR ${SDAI_SDCPP_VENDOR_DIR}/OpenCL-ICD-Loader)

if(NOT EXISTS ${SDAI_SDCPP_STABLE_DIFFUSION_CPP_VENDOR_DIR}/CMakeLists.txt)
    message(
        FATAL_ERROR
        "Missing stable-diffusion.cpp submodule. Run: git submodule update --init --recursive -- feature/sdxl/src/androidMain/cpp/vendor/stable-diffusion.cpp"
    )
endif()

if(NOT EXISTS ${SDAI_SDCPP_STABLE_DIFFUSION_CPP_VENDOR_DIR}/ggml/CMakeLists.txt)
    message(
        FATAL_ERROR
        "Missing stable-diffusion.cpp nested submodules. Run: git submodule update --init --recursive -- feature/sdxl/src/androidMain/cpp/vendor/stable-diffusion.cpp"
    )
endif()

if(SDAI_SDCPP_ENABLE_OPENCL)
    if(NOT EXISTS ${SDAI_SDCPP_OPENCL_HEADERS_DIR}/CL/cl.h)
        message(
            FATAL_ERROR
            "Missing OpenCL-Headers submodule. Run: git submodule update --init -- feature/sdxl/src/androidMain/cpp/vendor/OpenCL-Headers"
        )
    endif()

    if(NOT EXISTS ${SDAI_SDCPP_OPENCL_ICD_LOADER_DIR}/CMakeLists.txt)
        message(
            FATAL_ERROR
            "Missing OpenCL-ICD-Loader submodule. Run: git submodule update --init -- feature/sdxl/src/androidMain/cpp/vendor/OpenCL-ICD-Loader"
        )
    endif()
endif()

if(SDAI_SDCPP_ENABLE_VULKAN)
    if(NOT EXISTS ${SDAI_SDCPP_VULKAN_HEADERS_DIR}/include/vulkan/vulkan.h)
        message(
            FATAL_ERROR
            "Missing Vulkan-Headers submodule. Run: git submodule update --init -- feature/sdxl/src/androidMain/cpp/vendor/Vulkan-Headers"
        )
    endif()

    if(NOT EXISTS ${SDAI_SDCPP_SPIRV_HEADERS_DIR}/include/spirv/unified1/spirv.hpp)
        message(
            FATAL_ERROR
            "Missing SPIRV-Headers submodule. Run: git submodule update --init -- feature/sdxl/src/androidMain/cpp/vendor/SPIRV-Headers"
        )
    endif()
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_compile_options(
    "-ffile-prefix-map=${CMAKE_SOURCE_DIR}=."
    "-ffile-prefix-map=${CMAKE_BINARY_DIR}=."
)

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(SD_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(SD_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(SD_BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(SD_BUILD_SHARED_GGML_LIB OFF CACHE BOOL "" FORCE)
set(SD_WEBP OFF CACHE BOOL "" FORCE)
set(SD_WEBM OFF CACHE BOOL "" FORCE)
set(SD_OPENCL ${SDAI_SDCPP_ENABLE_OPENCL} CACHE BOOL "" FORCE)
set(SD_VULKAN ${SDAI_SDCPP_ENABLE_VULKAN} CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)

if(SDAI_SDCPP_ENABLE_OPENCL)
    set(OPENCL_ICD_LOADER_BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
    set(OPENCL_ICD_LOADER_BUILD_TESTING OFF CACHE BOOL "" FORCE)
    set(OPENCL_ICD_LOADER_HEADERS_DIR ${SDAI_SDCPP_OPENCL_HEADERS_DIR} CACHE PATH "" FORCE)
    set(ENABLE_OPENCL_LAYERS OFF CACHE BOOL "" FORCE)
    set(ENABLE_OPENCL_LAYERINFO OFF CACHE BOOL "" FORCE)
    add_subdirectory(
        ${SDAI_SDCPP_OPENCL_ICD_LOADER_DIR}
        ${CMAKE_CURRENT_BINARY_DIR}/OpenCL-ICD-Loader-build
    )
endif()

if(SDAI_SDCPP_ENABLE_VULKAN)
    if(ANDROID_NDK)
        file(GLOB SDAI_SDCPP_NDK_GLSLC_CANDIDATES
            "${ANDROID_NDK}/shader-tools/*/glslc"
            "${ANDROID_NDK}/shader-tools/*/glslc.exe"
        )
        list(LENGTH SDAI_SDCPP_NDK_GLSLC_CANDIDATES SDAI_SDCPP_NDK_GLSLC_COUNT)
        if(SDAI_SDCPP_NDK_GLSLC_COUNT GREATER 0)
            list(GET SDAI_SDCPP_NDK_GLSLC_CANDIDATES 0 SDAI_SDCPP_NDK_GLSLC)
            set(Vulkan_GLSLC_EXECUTABLE ${SDAI_SDCPP_NDK_GLSLC} CACHE FILEPATH "" FORCE)
        endif()
    endif()

    include_directories(SYSTEM ${SDAI_SDCPP_VULKAN_HEADERS_DIR}/include)
    include_directories(SYSTEM ${SDAI_SDCPP_SPIRV_HEADERS_DIR}/include)
    set(Vulkan_INCLUDE_DIR ${SDAI_SDCPP_VULKAN_HEADERS_DIR}/include CACHE PATH "" FORCE)

    if(CMAKE_HOST_WIN32)
        set(_sdai_vc_tools_dir "")
        if(DEFINED ENV{VCToolsInstallDir})
            file(TO_CMAKE_PATH "$ENV{VCToolsInstallDir}" _sdai_vc_tools_dir)
        endif()

        if(NOT _sdai_vc_tools_dir)
            file(GLOB _sdai_vc_tool_roots
                "C:/Program Files/Microsoft Visual Studio/*/VC/Tools/MSVC/*"
                "C:/Program Files (x86)/Microsoft Visual Studio/*/VC/Tools/MSVC/*"
                "C:/Program Files/Microsoft Visual Studio/*/Professional/VC/Tools/MSVC/*"
                "C:/Program Files/Microsoft Visual Studio/*/Enterprise/VC/Tools/MSVC/*"
                "C:/Program Files/Microsoft Visual Studio/*/Community/VC/Tools/MSVC/*"
                "C:/Program Files (x86)/Microsoft Visual Studio/*/Professional/VC/Tools/MSVC/*"
                "C:/Program Files (x86)/Microsoft Visual Studio/*/Enterprise/VC/Tools/MSVC/*"
                "C:/Program Files (x86)/Microsoft Visual Studio/*/Community/VC/Tools/MSVC/*"
            )
            foreach(_sdai_vc_tool_root IN LISTS _sdai_vc_tool_roots)
                if(EXISTS "${_sdai_vc_tool_root}/bin/Hostx64/x64/cl.exe")
                    set(_sdai_vc_tools_dir "${_sdai_vc_tool_root}")
                    break()
                endif()
            endforeach()
        endif()

        if(_sdai_vc_tools_dir)
            set(GGML_VULKAN_SHADERS_GEN_TOOLCHAIN
                "${CMAKE_CURRENT_LIST_DIR}/windows-host-toolchain.cmake"
            )
        endif()
    endif()

    if(NOT Vulkan_GLSLC_EXECUTABLE OR Vulkan_GLSLC_EXECUTABLE MATCHES "NOTFOUND$")
        message(FATAL_ERROR "SDAI stable-diffusion.cpp Vulkan backend requires glslc. Install Vulkan SDK or use an Android NDK with shader-tools/glslc.")
    endif()
endif()

set(stable_diffusion_cpp_SOURCE_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/stable-diffusion.cpp-src
)
set(stable_diffusion_cpp_BINARY_DIR
    ${CMAKE_CURRENT_BINARY_DIR}/stable-diffusion.cpp-build
)

file(REMOVE_RECURSE ${stable_diffusion_cpp_SOURCE_DIR})
file(
    COPY ${SDAI_SDCPP_STABLE_DIFFUSION_CPP_VENDOR_DIR}/
    DESTINATION ${stable_diffusion_cpp_SOURCE_DIR}
    PATTERN ".git" EXCLUDE
)

# Work around GGML Vulkan descriptor-set count overflow.
# Patch the build-local copy; leave the pinned submodules unchanged.
if(SDAI_SDCPP_ENABLE_VULKAN)
    set(SDAI_GGML_VULKAN_SOURCE
        "${stable_diffusion_cpp_SOURCE_DIR}/ggml/src/ggml-vulkan/ggml-vulkan.cpp"
    )
    if(NOT EXISTS "${SDAI_GGML_VULKAN_SOURCE}")
        message(FATAL_ERROR
            "GGML Vulkan CEIL_DIV fix could not locate '${SDAI_GGML_VULKAN_SOURCE}'. "
            "Check the pinned stable-diffusion.cpp vendor source layout."
        )
    endif()

    file(READ "${SDAI_GGML_VULKAN_SOURCE}" SDAI_VULKAN_CONTENT)

    set(SDAI_CEIL_DIV_OLD
        "#define CEIL_DIV(M, N) (((M) + (N)-1) / (N))"
    )
    set(SDAI_CEIL_DIV_NEW
        "#define CEIL_DIV(M, N) (((M) / (N)) + (((M) % (N)) != 0))"
    )

    string(REGEX MATCHALL "#define CEIL_DIV\\(M, N\\)" SDAI_CEIL_DIV_DEFINITIONS
        "${SDAI_VULKAN_CONTENT}"
    )
    list(LENGTH SDAI_CEIL_DIV_DEFINITIONS SDAI_CEIL_DIV_DEFINITION_COUNT)
    if(NOT SDAI_CEIL_DIV_DEFINITION_COUNT EQUAL 1)
        message(FATAL_ERROR
            "Expected exactly one GGML Vulkan CEIL_DIV definition in "
            "'${SDAI_GGML_VULKAN_SOURCE}', found ${SDAI_CEIL_DIV_DEFINITION_COUNT}."
        )
    endif()

    string(FIND "${SDAI_VULKAN_CONTENT}" "${SDAI_CEIL_DIV_OLD}" SDAI_CEIL_DIV_OLD_POSITION)
    string(FIND "${SDAI_VULKAN_CONTENT}" "${SDAI_CEIL_DIV_NEW}" SDAI_CEIL_DIV_NEW_POSITION)
    if(SDAI_CEIL_DIV_OLD_POSITION GREATER_EQUAL 0)
        string(REPLACE
            "${SDAI_CEIL_DIV_OLD}"
            "${SDAI_CEIL_DIV_NEW}"
            SDAI_VULKAN_PATCHED
            "${SDAI_VULKAN_CONTENT}"
        )
        string(FIND "${SDAI_VULKAN_PATCHED}" "${SDAI_CEIL_DIV_OLD}" SDAI_CEIL_DIV_OLD_REMAINS)
        string(FIND "${SDAI_VULKAN_PATCHED}" "${SDAI_CEIL_DIV_NEW}" SDAI_CEIL_DIV_NEW_AFTER_PATCH)
        if(SDAI_CEIL_DIV_OLD_REMAINS GREATER_EQUAL 0 OR SDAI_CEIL_DIV_NEW_AFTER_PATCH LESS 0)
            message(FATAL_ERROR
                "GGML Vulkan CEIL_DIV overflow fix failed to apply to "
                "'${SDAI_GGML_VULKAN_SOURCE}'."
            )
        endif()
        file(WRITE "${SDAI_GGML_VULKAN_SOURCE}" "${SDAI_VULKAN_PATCHED}")
        message(STATUS "Applied GGML Vulkan CEIL_DIV overflow fix")
    elseif(SDAI_CEIL_DIV_NEW_POSITION GREATER_EQUAL 0)
        message(STATUS "GGML Vulkan CEIL_DIV overflow fix is already present")
    else()
        message(FATAL_ERROR
            "GGML Vulkan CEIL_DIV overflow fix is incompatible with the vendored source. "
            "Expected either '${SDAI_CEIL_DIV_OLD}' or '${SDAI_CEIL_DIV_NEW}' in "
            "'${SDAI_GGML_VULKAN_SOURCE}'."
        )
    endif()
endif()

if(SDAI_SDCPP_ENABLE_OPENCL)
    set(SDAI_SDCPP_GGML_OPENCL_CMAKE
        ${stable_diffusion_cpp_SOURCE_DIR}/ggml/src/ggml-opencl/CMakeLists.txt
    )
    if(EXISTS ${SDAI_SDCPP_GGML_OPENCL_CMAKE})
        # Patch the build-local copy only; the pinned submodule stays clean.
        # Upstream probes the host sysroot for OpenCL, while SDAI provides
        # vendored Khronos headers and the ICD loader target.
        file(READ ${SDAI_SDCPP_GGML_OPENCL_CMAKE} SDAI_SDCPP_GGML_OPENCL_CMAKE_CONTENT)
        string(
            REPLACE
            "find_package(OpenCL REQUIRED)"
            "if(TARGET OpenCL)\n    set(OpenCL_LIBRARIES OpenCL)\n    set(OpenCL_INCLUDE_DIRS ${SDAI_SDCPP_OPENCL_HEADERS_DIR})\nelse()\n    find_package(OpenCL REQUIRED)\nendif()"
            SDAI_SDCPP_GGML_OPENCL_CMAKE_PATCHED
            "${SDAI_SDCPP_GGML_OPENCL_CMAKE_CONTENT}"
        )
        if(NOT SDAI_SDCPP_GGML_OPENCL_CMAKE_PATCHED STREQUAL SDAI_SDCPP_GGML_OPENCL_CMAKE_CONTENT)
            file(WRITE ${SDAI_SDCPP_GGML_OPENCL_CMAKE} "${SDAI_SDCPP_GGML_OPENCL_CMAKE_PATCHED}")
        endif()
    endif()
endif()

if(SDAI_SDCPP_ENABLE_VULKAN)
    set(SDAI_SDCPP_GGML_VULKAN_CMAKE
        ${stable_diffusion_cpp_SOURCE_DIR}/ggml/src/ggml-vulkan/CMakeLists.txt
    )
    if(EXISTS ${SDAI_SDCPP_GGML_VULKAN_CMAKE})
        # Patch the build-local copy only; the pinned submodule stays clean.
        file(READ ${SDAI_SDCPP_GGML_VULKAN_CMAKE} SDAI_SDCPP_GGML_VULKAN_CMAKE_CONTENT)
        string(
            REPLACE
            "if (\${glslc_error} MATCHES"
            "if (\"\${glslc_error}\" MATCHES"
            SDAI_SDCPP_GGML_VULKAN_CMAKE_PATCHED
            "${SDAI_SDCPP_GGML_VULKAN_CMAKE_CONTENT}"
        )
        if(NOT SDAI_SDCPP_GGML_VULKAN_CMAKE_PATCHED STREQUAL SDAI_SDCPP_GGML_VULKAN_CMAKE_CONTENT)
            file(WRITE ${SDAI_SDCPP_GGML_VULKAN_CMAKE} "${SDAI_SDCPP_GGML_VULKAN_CMAKE_PATCHED}")
        endif()
    endif()

    set(SDAI_SDCPP_GGML_VULKAN_CPP
        ${stable_diffusion_cpp_SOURCE_DIR}/ggml/src/ggml-vulkan/ggml-vulkan.cpp
    )
    if(EXISTS ${SDAI_SDCPP_GGML_VULKAN_CPP})
        file(READ ${SDAI_SDCPP_GGML_VULKAN_CPP} SDAI_SDCPP_GGML_VULKAN_CPP_CONTENT)
        string(
            REPLACE
            "vkGetPhysicalDeviceFeatures2("
            "VULKAN_HPP_DEFAULT_DISPATCHER.vkGetPhysicalDeviceFeatures2("
            SDAI_SDCPP_GGML_VULKAN_CPP_PATCHED
            "${SDAI_SDCPP_GGML_VULKAN_CPP_CONTENT}"
        )
        if(NOT SDAI_SDCPP_GGML_VULKAN_CPP_PATCHED STREQUAL SDAI_SDCPP_GGML_VULKAN_CPP_CONTENT)
            file(WRITE ${SDAI_SDCPP_GGML_VULKAN_CPP} "${SDAI_SDCPP_GGML_VULKAN_CPP_PATCHED}")
        endif()
    endif()
endif()

add_subdirectory(${stable_diffusion_cpp_SOURCE_DIR} ${stable_diffusion_cpp_BINARY_DIR})

if(SDAI_SDCPP_ENABLE_VULKAN)
    set(SDAI_SDCPP_VULKAN_SHADER_DIR
        ${stable_diffusion_cpp_BINARY_DIR}/ggml/src/ggml-vulkan/vulkan-shaders.spv
    )
    add_custom_target(
        sdai_sdcpp_prepare_vulkan_shaders
        COMMAND ${CMAKE_COMMAND} -E make_directory ${SDAI_SDCPP_VULKAN_SHADER_DIR}
    )
    if(TARGET ggml-vulkan)
        add_dependencies(ggml-vulkan sdai_sdcpp_prepare_vulkan_shaders)
    endif()
endif()

add_library(sdai_sdcpp SHARED stable_diffusion_jni.cpp)

target_link_libraries(
    sdai_sdcpp
    PRIVATE
    stable-diffusion
    android
    log
    c++_shared
)

target_compile_definitions(
    sdai_sdcpp
    PRIVATE
    SDAI_SDCPP_HAS_OPENCL=$<BOOL:${SDAI_SDCPP_ENABLE_OPENCL}>
    SDAI_SDCPP_HAS_VULKAN=$<BOOL:${SDAI_SDCPP_ENABLE_VULKAN}>
)

target_link_options(
    sdai_sdcpp
    PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
)

target_compile_options(
    sdai_sdcpp
    PRIVATE
    -Wall
    -Wextra
    -Wno-unused-parameter
)
