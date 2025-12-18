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
    std::cerr << "$HOME not set, using defaults\n";
    return {6669, "copas-auto-generated-secret"};
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
    cfg["port"] = 6669;
    cfg["auth_token"] = "copas-auto-generated-secret";
    std::ofstream outfile(configPath);
    outfile << std::setw(2) << cfg << std::endl;
    std::cout << "Created config: " << configPath << "\n";
    return {6669, "copas-auto-generated-secret"};
  }

  try {
    json data = json::parse(infile);
    AppConfig cfg;

    if (data.contains("port") && data["port"].is_number_integer()) {
      cfg.port = data["port"];
    } else {
      cfg.port = 6669;
    }

    if (data.contains("auth_token") && data["auth_token"].is_string()) {
      cfg.auth_token = data["auth_token"];
    } else {
      cfg.auth_token = "copas-auto-generated-secret";
    }

    return cfg;
  } catch (const std::exception &e) {
    std::cerr << "Parse error: " << e.what() << ", using defaults\n";
    return {6669, "copas-auto-generated-secret"};
  }
}
