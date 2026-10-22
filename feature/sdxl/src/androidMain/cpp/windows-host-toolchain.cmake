# The GGML Vulkan shader generator is created in a deeply nested .cxx tree on Windows.
# Its MSVC test object path is otherwise too close to the filesystem limit, so increase it.
set(CMAKE_OBJECT_PATH_MAX 500 CACHE STRING "" FORCE)

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

if(NOT _sdai_vc_tools_dir)
    message(FATAL_ERROR "Visual Studio MSVC toolchain was not found. Install Visual Studio Build Tools or ensure VCToolsInstallDir is available.")
endif()

set(_sdai_msvc_compiler "${_sdai_vc_tools_dir}/bin/Hostx64/x64/cl.exe")
if(NOT EXISTS "${_sdai_msvc_compiler}")
    message(FATAL_ERROR "Visual Studio host compiler not found: ${_sdai_msvc_compiler}")
endif()

set(CMAKE_C_COMPILER "${_sdai_msvc_compiler}" CACHE FILEPATH "Host C compiler" FORCE)
set(CMAKE_CXX_COMPILER "${_sdai_msvc_compiler}" CACHE FILEPATH "Host C++ compiler" FORCE)

set(_sdai_windows_sdk_rc "")
file(GLOB _sdai_windows_sdk_rc_candidates
    "C:/Program Files (x86)/Windows Kits/10/bin/*/x64/rc.exe"
    "C:/Program Files/Windows Kits/10/bin/*/x64/rc.exe"
)
foreach(_sdai_windows_sdk_rc_candidate IN LISTS _sdai_windows_sdk_rc_candidates)
    if(EXISTS "${_sdai_windows_sdk_rc_candidate}")
        set(_sdai_windows_sdk_rc "${_sdai_windows_sdk_rc_candidate}")
        break()
    endif()
endforeach()
if(_sdai_windows_sdk_rc)
    get_filename_component(_sdai_windows_sdk_bin_dir "${_sdai_windows_sdk_rc}" DIRECTORY)
    set(CMAKE_RC_COMPILER "${_sdai_windows_sdk_rc}" CACHE FILEPATH "Host resource compiler" FORCE)
    set(CMAKE_MT "${_sdai_windows_sdk_bin_dir}/mt.exe" CACHE FILEPATH "Manifest tool" FORCE)
    set(ENV{PATH} "${_sdai_windows_sdk_bin_dir};$ENV{PATH}")
endif()

set(CMAKE_C_COMPILER_ID "MSVC")
set(CMAKE_CXX_COMPILER_ID "MSVC")
set(CMAKE_C_COMPILER_FRONTEND_VARIANT "MSVC")
set(CMAKE_CXX_COMPILER_FRONTEND_VARIANT "MSVC")
set(CMAKE_C_FLAGS_INIT "/O2")
set(CMAKE_CXX_FLAGS_INIT "/O2")
set(CMAKE_C_STANDARD_LIBRARIES_INIT "")
set(CMAKE_CXX_STANDARD_LIBRARIES_INIT "")
