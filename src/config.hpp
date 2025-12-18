#pragma once
#include <string>

struct AppConfig {
  int port = 6669;
  std::string auth_token;
};

AppConfig loadConfig();
