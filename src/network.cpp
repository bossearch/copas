#include "network.hpp"
#include <cstdio>
#include <memory>
#include <string>

struct FILE_Deleter {
  void operator()(FILE *fp) const {
    if (fp)
      pclose(fp);
  }
};

static std::string exec(const char *cmd) {
  std::unique_ptr<FILE, FILE_Deleter> pipe(popen(cmd, "r"));
  if (!pipe)
    return "";

  char buffer[256];
  std::string result;
  while (fgets(buffer, sizeof(buffer), pipe.get()) != nullptr) {
    result += buffer;
  }

  if (!result.empty() && result.back() == '\n')
    result.pop_back();

  return result;
}

std::string getLocalIPv4() {
  std::string ip = exec("ip -4 route get 1.1.1.1 2>/dev/null | grep -oP 'src "
                        "\\K[\\d.]+' | head -1");

  if (ip.empty() || ip == "127.0.0.1") {
    ip = exec("hostname -I | awk '{print $1}'");
  }

  if (ip.empty()) {
    ip = "127.0.0.1";
  }

  return ip;
}
