#include "client.hpp"
#include "config.hpp"
#include "network.hpp"
#include "tray.hpp"
#include <atomic>
#include <csignal>
#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <gtk/gtk.h>
#include <iostream>
#include <string>

void printHelp(const char* prog) {
  std::cout << "Usage: \n"
            << "  " << prog << " [options]\n\n"
            << "Options:\n"
            << "  -v  --version         Print the current version\n"
            << "  -h  --help            Print this help message\n"
            << "  -c  --config          Print the config file path\n"
            << "  -cd --config-dir      Print the config directory\n";
}

void printVersion() {
  std::cout << "copas 0.1.0\n";
}

void handleCliArgs(int argc, char* argv[]) {
  const std::string prog = argv[0];
  for (int i = 1; i < argc; ++i) {
    std::string arg = argv[i];
    if (arg == "-h" || arg == "--help") {
      printHelp(prog.c_str());
      std::exit(0);
    } else if (arg == "-v" || arg == "--version") {
      printVersion();
      std::exit(0);
    } else if (arg == "-cd" || arg == "--config-dir") {
      const char* home = std::getenv("HOME");
      if (!home) {
        std::cerr << "$HOME not set\n";
        std::exit(1);
      }
      std::cout << std::string(home) + "/.config/copas\n";
      std::exit(0);
    } else if (arg == "-c" || arg == "--config") {
      const char* home = std::getenv("HOME");
      if (!home) {
        std::cerr << "$HOME not set\n";
        std::exit(1);
      }
      std::string configPath = std::string(home) + "/.config/copas/config.json";

      std::ifstream file(configPath);
      if (!file) {
        std::cerr << "Config file not found: " << configPath << "\n";
        std::exit(1);
      }

      std::string line;
      while (std::getline(file, line)) {
        std::cout << line << '\n';
      }
      std::exit(0);
    } else {
      std::cerr << "Unknown option: " << arg << "\n";
      std::exit(1);
    }
  }
}

std::atomic<bool> g_should_quit{false};

void requestQuit() {
  if (!g_should_quit.exchange(true)) {
    stopClient();
    gtk_main_quit();
  }
}

int main(int argc, char *argv[]) {
  handleCliArgs(argc, argv);
  gtk_init(&argc, &argv);

  auto config = loadConfig();
  startClient(6669, config.auth_token);

  std::string localIP = getLocalIPv4();
  std::string label = "copas – " + localIP + ":6669";
  setupTray(label, requestQuit);

  std::cout << "📋 copas ready. Use tray menu to quit.\n";
  gtk_main();

  return 0;
}
