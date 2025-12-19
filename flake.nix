{
  description = "Nix Flake for copas";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }: let
    copasHmModule = import ./nix/hm-module.nix self;
  in
    {
      homeManagerModules.default = copasHmModule;
    }
    // flake-utils.lib.eachDefaultSystem (system: let
      pkgs = nixpkgs.legacyPackages.${system};
      copasPkg = pkgs.callPackage ./nix {};
    in {
      packages = {
        copas = copasPkg;
        default = copasPkg;
      };

      devShells.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          cmake
          pkg-config
          gtk3
          libayatana-appindicator
          wl-clipboard
          nlohmann_json
          httplib
        ];
      };
    });
}
