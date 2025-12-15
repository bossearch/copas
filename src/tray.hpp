#pragma once
#include <string>

using QuitCallback = void (*)();

void setupTray(const std::string &statusLabel, QuitCallback onQuit);
