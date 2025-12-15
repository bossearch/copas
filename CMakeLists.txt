cmake_minimum_required(VERSION 3.14)
project(copas VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED gtk+-3.0)
pkg_check_modules(AYATANA REQUIRED ayatana-appindicator3-0.1)
find_path(NLOHMANN_JSON_INCLUDE_DIR nlohmann/json.hpp REQUIRED)

file(GLOB SOURCES "src/*.cpp")

add_executable(copas ${SOURCES})

target_include_directories(copas PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${GTK3_INCLUDE_DIRS}
    ${AYATANA_INCLUDE_DIRS}
    ${NLOHMANN_JSON_INCLUDE_DIR}
)

target_link_libraries(copas
    ${GTK3_LDFLAGS}
    ${AYATANA_LDFLAGS}
)

target_compile_definitions(copas PRIVATE
    _POSIX_C_SOURCE=200809L
    _XOPEN_SOURCE=700
    _GNU_SOURCE
)
