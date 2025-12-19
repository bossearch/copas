{
  stdenv,
  lib,
  cmake,
  pkg-config,
  gtk3,
  libayatana-appindicator,
  wl-clipboard,
  nlohmann_json,
  httplib,
}:
stdenv.mkDerivation {
  pname = "copas";
  version = "0.1.0";

  src = lib.cleanSource ../.;

  nativeBuildInputs = [
    cmake
    pkg-config
  ];

  buildInputs = [
    gtk3
    libayatana-appindicator
    nlohmann_json
    httplib
  ];

  propagatedBuildInputs = [
    wl-clipboard
  ];

  cmakeFlags = [
    "-DCMAKE_BUILD_TYPE=Release"
  ];

  meta = with lib; {
    mainProgram = "copas";
    description = "Copas is Copy Paste Send between Linux (Wayland) and Android";
    homepage = "https://github.com/bossearch/copas";
    license = licenses.mit;
    platforms = platforms.linux;
  };
}
