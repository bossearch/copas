self: {
  config,
  lib,
  pkgs,
  ...
}:
with lib; let
  cfg = config.programs.copas;
  defaultPackage =
    self.packages.${pkgs.stdenv.hostPlatform.system}.default;
  jsonFormat = pkgs.formats.json {};
in {
  options.programs.copas = {
    enable = mkEnableOption "Whether to enable copas.";

    package = mkOption {
      type = types.package;
      default = defaultPackage;
      defaultText =
        literalExpression
        "inputs.copas.packages.${pkgs.stdenv.hostPlatform.system}.default";
      description = "The copas package to use.";
    };

    systemd = mkOption {
      type = types.bool;
      default = true;
      description = "Whether to run copas as a systemd user service.";
    };

    settings = {
      port = mkOption {
        type = types.port;
        default = 6669;
        description = "Port copas listens on.";
      };

      auth_token = mkOption {
        type = types.str;
        default = "copas-auto-generated-secret";
        description = "Authentication token.";
      };
    };
  };

  config = mkIf cfg.enable {
    home.packages = [cfg.package];

    xdg.configFile."copas/config.json".source =
      jsonFormat.generate "config.json" cfg.settings;

    systemd.user.services.copas = mkIf cfg.systemd {
      Unit = {
        Description = "copas daemon";
        PartOf = ["graphical-session.target"];
        After = ["graphical-session.target"];
      };

      Service = {
        ExecStart = "${cfg.package}/bin/copas";
        Restart = "always";
        RestartSec = 5;
      };

      Install.WantedBy = ["graphical-session.target"];
    };
  };
}
