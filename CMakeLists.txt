cmake_minimum_required(VERSION 3.14)
project(copas VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(COPAS_USE_SYSTEM_DEPS "Use system-provided dependencies" OFF)

find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED IMPORTED_TARGET gtk+-3.0)
pkg_check_modules(AYATANA REQUIRED IMPORTED_TARGET ayatana-appindicator3-0.1)

if (COPAS_USE_SYSTEM_DEPS)
  find_path(NLOHMANN_JSON_INCLUDE_DIR
    NAMES nlohmann/json.hpp
    REQUIRED
  )

  find_path(HTTPLIB_INCLUDE_DIR
    NAMES httplib.h
    REQUIRED
  )

  set(COPAS_INCLUDE_DIRS
    ${NLOHMANN_JSON_INCLUDE_DIR}
    ${HTTPLIB_INCLUDE_DIR}
  )

  set(COPAS_LIBS "")
else()
  include(FetchContent)

  FetchContent_Declare(
    nlohmann_json
    GIT_REPOSITORY https://github.com/nlohmann/json.git
    GIT_TAG v3.12.0
  )

  set(HTTPLIB_USE_ZSTD_IF_AVAILABLE OFF CACHE BOOL "" FORCE)
  set(HTTPLIB_USE_ZLIB_IF_AVAILABLE OFF CACHE BOOL "" FORCE)
  set(HTTPLIB_USE_BROTLI_IF_AVAILABLE OFF CACHE BOOL "" FORCE)

  FetchContent_Declare(
    cpp_httplib
    GIT_REPOSITORY https://github.com/yhirose/cpp-httplib.git
    GIT_TAG v0.28.0
  )

  FetchContent_MakeAvailable(nlohmann_json cpp_httplib)

  set(COPAS_INCLUDE_DIRS "")
  set(COPAS_LIBS
    nlohmann_json::nlohmann_json
    httplib::httplib
  )
endif()

file(GLOB SOURCES CONFIGURE_DEPENDS "src/*.cpp")

add_executable(copas ${SOURCES})

target_include_directories(copas PRIVATE
  src
  ${COPAS_INCLUDE_DIRS}
)

target_link_libraries(copas PRIVATE
  PkgConfig::GTK3
  PkgConfig::AYATANA
  ${COPAS_LIBS}
)

target_compile_definitions(copas PRIVATE
  _POSIX_C_SOURCE=200809L
  _XOPEN_SOURCE=700
  _GNU_SOURCE
)

install(TARGETS copas
  RUNTIME DESTINATION bin
)
