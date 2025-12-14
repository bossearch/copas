{pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  buildInputs = with pkgs; [
    gcc
    cmake
    pkg-config
    gtk3
    libayatana-appindicator
    wl-clipboard
    curl
  ];

  shellHook = ''
    export LD_LIBRARY_PATH="${pkgs.libayatana-appindicator}/lib:$LD_LIBRARY_PATH"
    echo "C++ dev shell ready (cpp-httplib + Ayatana)"
  '';
}
