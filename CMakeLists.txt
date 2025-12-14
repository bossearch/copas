cmake_minimum_required(VERSION 3.14)
project(ClipboardSync VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED gtk+-3.0)
pkg_check_modules(AYATANA REQUIRED ayatana-appindicator3-0.1)

add_executable(copas src/main.cpp)

target_include_directories(copas PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party
    ${GTK3_INCLUDE_DIRS}
    ${AYATANA_INCLUDE_DIRS}
)

target_link_libraries(copas
    ${GTK3_LDFLAGS}
    ${AYATANA_LDFLAGS}
)

target_compile_definitions(copas PRIVATE _GNU_SOURCE)
