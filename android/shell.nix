{
  pkgs ?
    import <nixpkgs> {
      config = {
        allowUnfree = true;
        android_sdk.accept_license = true;
      };
    },
}: let
  androidSdk = pkgs.androidenv.composeAndroidPackages {
    cmdLineToolsVersion = "12.0";
    platformToolsVersion = "35.0.1";
    buildToolsVersions = ["34.0.0"];
    platformVersions = ["34"];
    includeEmulator = false;
    includeSystemImages = false;
  };
in
  pkgs.mkShell {
    packages = with pkgs; [
      jdk17
      gradle
      androidSdk.androidsdk
      # android-studio
      android-tools
    ];

    shellHook = ''
      export ANDROID_HOME=${androidSdk.androidsdk}/libexec/android-sdk
      export ANDROID_SDK_ROOT=$ANDROID_HOME
      export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0:$PATH
    '';
  }
