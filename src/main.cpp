#include "httplib.h"
#include <gtk/gtk.h>
#include <libayatana-appindicator/app-indicator.h>

#include <cstdio>
#include <iostream>
#include <memory>
#include <string>
#include <thread>

std::string exec(const char *cmd) {
  std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd, "r"), pclose);
  if (!pipe)
    return "";
  char buffer[256];
  std::string result;
  while (fgets(buffer, sizeof buffer, pipe.get()) != nullptr) {
    result += buffer;
  }
  if (!result.empty() && result.back() == '\n')
    result.pop_back();
  return result;
}

std::string getClipboard() { return exec("wl-paste --no-newline 2>/dev/null"); }

void setClipboard(const std::string &text) {
  FILE *pipe = popen("wl-copy", "w");
  if (pipe) {
    std::fwrite(text.data(), 1, text.size(), pipe);
    pclose(pipe);
  }
}

int main(int argc, char *argv[]) {
  httplib::Server svr;

  svr.Get("/pull", [](const httplib::Request &, httplib::Response &res) {
    std::string content = getClipboard();
    res.set_content(content, "text/plain");
  });

  svr.Post("/push", [](const httplib::Request &req, httplib::Response &res) {
    if (req.body.substr(0, 5) == "text=") {
      std::string text = req.body.substr(5);
      setClipboard(text);
      res.set_content("OK", "text/plain");
    } else {
      res.status = 400;
      res.set_content("Bad Request: expected 'text=...'", "text/plain");
    }
  });

  std::thread server_thread([&svr]() {
    std::cout << "🌐 HTTP server starting on 0.0.0.0:6669\n";
    svr.listen("0.0.0.0", 6669);
  });

  gtk_init(&argc, &argv);

  auto *indicator = app_indicator_new(
      "copas", "edit-paste", APP_INDICATOR_CATEGORY_APPLICATION_STATUS);
  app_indicator_set_status(indicator, APP_INDICATOR_STATUS_ACTIVE);

  auto *menu = gtk_menu_new();
  auto *quit_item = gtk_menu_item_new_with_label("Quit");
  g_signal_connect_swapped(quit_item, "activate", G_CALLBACK(gtk_main_quit),
                           nullptr);
  gtk_menu_shell_append(GTK_MENU_SHELL(menu), quit_item);
  gtk_widget_show_all(menu);
  app_indicator_set_menu(indicator, GTK_MENU(menu));

  std::cout << "📋 Tray icon active. Use 'Quit' to exit.\n";

  gtk_main();

  svr.stop();
  if (server_thread.joinable()) {
    server_thread.join();
  }

  return 0;
}
