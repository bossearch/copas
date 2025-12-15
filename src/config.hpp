#pragma once
#include <string>

struct AppConfig {
  std::string auth_token;
};

AppConfig loadConfig();
