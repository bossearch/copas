cmake_minimum_required(VERSION 3.14)
project(copas VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED IMPORTED_TARGET gtk+-3.0)
pkg_check_modules(AYATANA REQUIRED IMPORTED_TARGET ayatana-appindicator3-0.1)

find_path(NLOHMANN_JSON_INCLUDE_DIR nlohmann/json.hpp REQUIRED)
find_path(HTTPLIB_INCLUDE_DIR httplib.h REQUIRED)

set(SOURCES
  src/main.cpp
  src/client.cpp
  src/config.cpp
  src/clipboard.cpp
  src/network.cpp
  src/tray.cpp
)

add_executable(copas ${SOURCES})

target_include_directories(copas PRIVATE
  src
  ${NLOHMANN_JSON_INCLUDE_DIR}
  ${HTTPLIB_INCLUDE_DIR}
)

target_link_libraries(copas PRIVATE
  PkgConfig::GTK3
  PkgConfig::AYATANA
)

target_compile_definitions(copas PRIVATE
  _POSIX_C_SOURCE=200809L
  _XOPEN_SOURCE=700
  _GNU_SOURCE
)
