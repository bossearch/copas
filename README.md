<a id="readme-top"></a>

<div>
    <h1 align="center">Copas</h1>
    <p align="center">
    A clipboard sharing utility via local network.
    </p>
</div>

<details>
    <summary>Table of Contents</summary>

<!-- toc -->

- [About](#about)
- [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Installation](#installation)
- [Usage](#usage)
    * [How it works](#how-it-works)
    * [Configuration](#configuration)
- [Roadmap](#roadmap)
- [License](#license)
- [Acknowledgments](#acknowledgments)

<!-- tocstop -->

</details>

## About

Copas is a lightweight utility for sharing clipboard text data between Linux (Wayland) and Android over a local network.
It focuses on doing a few things well, offering a minimal alternative for workflows where simplicity and control matter.

The Purpose of this project is to evolve into a minimal peripheral bridge, allowing an Android device to serve as a seamless clipboard peer, touchpad, and keyboard.

## Getting Started

What you need and how to set up Copas on your system.

### Prerequisites

Copas is written in C++ and built using CMake.
If you are using Nix, all dependencies are provided automatically.
For manual installation, the following runtime libraries are required:

- GTK 3
- Libayatana AppIndicator
- wl-clipboard

<details>
      <summary>Installing Dependencies</summary>

<h4>Ubuntu and derivatives</h4>

```sh
sudo apt install -y libgtk-3-0t64 libayatana-appindicator3-1 wl-clipboard
```

<h4>Arch and derivatives</h4>

```sh
sudo pacman -S --needed gtk3 libayatana-appindicator wl-clipboard
```

<h4>Fedora and derivatives</h4>

```sh
sudo dnf install -y gtk3 libayatana-appindicator-gtk3 wl-clipboard
```

</details>

### Installation

To install the Copas, choose and follow the steps below:

<details>
    <summary>Nix</summary>

<h4>Try it out</h4>

```bash
nix run github:bossearch/copas

# With params
nix run github:bossearch/copas -- --help

```

<h4>Flake</h4>

1. Clone and cd to the git repository.

```sh
git clone https://github.com/bossearch/copas.git
cd copas
```

2. Enter nix dev shell

```sh
nix develop
nix build
```

3. Find the binary inside `./result/bin/copas`

<h4>Home-manager module</h4>

Add input to your `flake.nix`:

```nix
inputs = {
    copas.url = "github:bossearch/copas";
}
```

Enable copas:

```nix
{
  inputs,
  ...
}: {
  # Add the Home Manager module
  imports = [inputs.copas.homeManagerModules.default];

  programs.copas = {
    enable = true;
    # systemd = false;
    # settings = {
    #   port = 6669;
    #   auth_token = "copas-auto-generated-secret";
    # };
    };
  };
}

```

</details>

<details>
    <summary>Manual Installation</summary>

1. First make sure to install the necessary [dependencies](https://github.com/bossearch/copas#prerequisites).

2. Download and extract the prebuilt Linux binary from the [Release](https://github.com/bossearch/copas/releases) section.

```sh
chmod +x copas
./copas --help
```

3. You may place the binary to your `$PATH`.

```sh
sudo cp path/to/copas /usr/local/bin/
```

</details>

<details>
    <summary>Build from source</summary>

1. Clone and cd to the git repository.

```sh
git clone https://github.com/bossearch/copas.git
cd copas
```

2. Configure and build using CMake.

```sh
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build
```

3. Install the binary.

```sh
sudo cmake --install build
```

The `copas` binary will be installed to `/usr/local/bin` by default.

</details>

<details>
    <summary>Android</summary>
The Android client is distributed as a standalone APK.

You can install it using [Obtainium](https://github.com/ImranR98/Obtainium) or download it manually from the [Release](https://github.com/bossearch/copas/releases) section.

> **Note:** The Android APK is currently signed with a debug key.

</details>

## Usage

By default, Copas works as a **Linux clipboard server** and an **Android client**.

### How it works

1. When Copas runs for the first time on Linux, it automatically creates a default configuration file at `$XDG_CONFIG_HOME/copas/config.json`

2. This configuration contains:
   - A default TCP port
   - An auto-generated authentication token

3. Copas exposes a **local HTTP API** on the configured port.

4. The Android app connects to the Copas server running on Linux using:
   - Linux local IP address
   - TCP port
   - Authentication token

5. Once connected, you can pull or push clipboard from the android app.

### Configuration

Example `config.json`:

```json
{
  "port": 6669,
  "auth_token": "copas-auto-generated-secret"
}
```

Make sure you have TCP port `6669` (or the one selected) opened up in your firewall.

## Roadmap

Planned features and future improvements.

- [x] Clipboard sync between Linux and Android
- [ ] Cursor control (Android as touchpad)
- [ ] Keyboard input from Android
- [ ] End to end encryption (TLS/HTTPS)

## License

Distributed under the GPL-3.0-or-later. See [LICENSE](https://github.com/bossearch/copas/blob/main/LICENSE) for more information.

## Acknowledgments

- [KDE Connect](https://invent.kde.org/network/kdeconnect-kde)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
