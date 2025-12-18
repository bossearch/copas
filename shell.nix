{pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  buildInputs = with pkgs; [
    cmake
    pkg-config
    gtk3
    libayatana-appindicator
    wl-clipboard
    nlohmann_json
    httplib
  ];

  shellHook = ''
    export PKG_CONFIG_PATH="${pkgs.sysprof}/lib/pkgconfig:$PKG_CONFIG_PATH"
    export LD_LIBRARY_PATH="${pkgs.libayatana-appindicator}/lib:$LD_LIBRARY_PATH"
  '';
}
