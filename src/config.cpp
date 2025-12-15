#include "config.hpp"
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <nlohmann/json.hpp>
#include <sys/stat.h>
#include <unistd.h>

using json = nlohmann::json;

AppConfig loadConfig() {
  const char *home = std::getenv("HOME");
  if (!home) {
    std::cerr << "$HOME not set, using default token\n";
    return {"default-token"};
  }

  std::string configDir = std::string(home) + "/.config/copas";
  std::string configPath = configDir + "/config.json";

  struct stat st;
  if (stat(configDir.c_str(), &st) != 0) {
    if (mkdir(configDir.c_str(), 0700) != 0) {
      std::cerr << "Failed to create config dir\n";
    }
  }

  std::ifstream infile(configPath);
  if (!infile) {
    json cfg;
    cfg["auth_token"] = "copas-auto-generated-secret";
    std::ofstream outfile(configPath);
    outfile << std::setw(2) << cfg << std::endl;
    std::cout << "Created config: " << configPath << "\n";
    return {cfg["auth_token"]};
  }

  try {
    json data = json::parse(infile);
    if (data.contains("auth_token") && data["auth_token"].is_string()) {
      return {data["auth_token"]};
    } else {
      std::cerr << "Invalid config, using default token\n";
      return {"default-token"};
    }
  } catch (const std::exception &e) {
    std::cerr << "Parse error: " << e.what() << ", using default\n";
    return {"default-token"};
  }
}
