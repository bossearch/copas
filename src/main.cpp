// src/main.cpp
#include <atomic>
#include <csignal>
#include <gtk/gtk.h>
#include <iostream>

#include "client.hpp"
#include "config.hpp"
#include "network.hpp"
#include "tray.hpp"

// Global shutdown flag
std::atomic<bool> g_should_quit{false};

// Function to trigger full shutdown
void requestQuit() {
  if (!g_should_quit.exchange(true)) {
    stopClient();    // stop HTTP server
    gtk_main_quit(); // exit GTK loop
  }
}

int main(int argc, char *argv[]) {
  gtk_init(&argc, &argv);

  auto config = loadConfig();
  startClient(6669, config.auth_token);

  std::string localIP = getLocalIPv4();
  std::string label = "copas – " + localIP + ":6669";
  setupTray(label, requestQuit); // pass quit callback

  std::cout << "📋 copas ready. Use tray menu to quit.\n";
  gtk_main();

  return 0;
}
