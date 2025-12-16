#include "client.hpp"
#include "clipboard.hpp"
#include "httplib.h"
#include <atomic>
#include <iostream>
#include <memory>
#include <thread>

namespace {
std::unique_ptr<std::thread> g_http_thread;
std::atomic<bool> g_running{false};
}

void startClient(int port, const std::string &authToken) {
  if (g_running)
    return;

  g_running = true;
  g_http_thread = std::make_unique<std::thread>([port, authToken]() {
    httplib::Server svr;

    svr.Get("/pull", [authToken](const httplib::Request &req, httplib::Response &res) {
      if (req.get_param_value("token") != authToken) {
        res.status = 401;
        res.set_content("Unauthorized", "text/plain");
        return;
      }
      res.set_content(getClipboard(), "text/plain");
    });

    svr.Post("/push", [authToken](const httplib::Request &req, httplib::Response &res) {
      if (req.get_param_value("token") != authToken) {
        res.status = 401;
        res.set_content("Unauthorized", "text/plain");
        return;
      }
      if (req.body.size() >= 5 && req.body.substr(0, 5) == "text=") {
        setClipboard(req.body.substr(5));
        res.set_content("OK", "text/plain");
      } else {
        res.status = 400;
        res.set_content("Bad Request", "text/plain");
      }
    });

    std::cout << "🌐 copas listening on port " << port << "\n";
    svr.listen("0.0.0.0", port);
  });
}

void stopClient() {}
