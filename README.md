# listen

`listen` is a Kotlin/Native command-line tool that rewrites song links between streaming services. Give it a URL from Apple Music, Spotify, YouTube, etc., and it will ask the public [song.link](https://song.link/) API for the matching link on your target platform.

## Prerequisites

- macOS with Xcode command-line tools for native compilation (`xcode-select --install`)

## Installation

Install via Homebrew from the custom tap:

```bash
brew install andrey-jpeg/tap/listen
```

You can also add the tap explicitly:

```bash
brew tap andrey-jpeg/tap
brew install listen
```

When managing dependencies with a `Brewfile`, include:

```ruby
tap "andrey-jpeg/tap"
brew "listen"
```

## Usage

Build the CLI binary:

```bash
./gradlew build
```

You can find the debug executables at:

- `build/bin/macosArm64/debugExecutable/listen.kexe`
- `build/bin/macosX64/debugExecutable/listen.kexe`

Run the binary directly (ensure it has execute permission):

```bash
./build/bin/macosArm64/debugExecutable/listen.kexe https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT
```

- `-p/--platform` chooses the destination catalogue. When omitted, the tool lists all available matches and lets you pick one with the arrow keys.
- The final argument is the source song URL.

If the song.link API cannot provide a link for the requested platform, the command exits with code `1` and prints the error message.

## Building Native Binaries

The project targets both `macosArm64` and `macosX64`. Build the executables for each architecture:

```bash
./gradlew linkDebugExecutableMacosArm64 linkDebugExecutableMacosX64
```

Gradle produces binaries at:

- `build/bin/macosArm64/debugExecutable/listen.kexe`
- `build/bin/macosX64/debugExecutable/listen.kexe`

## Troubleshooting

- **Native build fails with `xcrun` errors** – ensure Xcode and its command line tools are installed and configured.
- **No link returned for a platform** – song.link does not guarantee coverage for every track or region. The CLI reports the failure and exits non-zero.

## Project Structure

- `src/commonMain` – shared CLI logic, link resolution, terminal helpers, and clipboard abstraction.
- `src/nativeMain` – macOS-specific expect/actual implementations and entry point.

Gradle config lives in `build.gradle.kts` and defines multiplatform targets for macOS only.
