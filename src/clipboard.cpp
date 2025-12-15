#include "clipboard.hpp"
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
