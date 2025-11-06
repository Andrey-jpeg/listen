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

## Using listen (Homebrew)

After installation, the `listen` executable is available on your `PATH`. Invoke it with the source song / album URL:

```bash
listen https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT
```

- Pass `-p/--platform` to jump straight to a specific catalogue, e.g. `listen -p apple-music …`.
- When no platform is provided, `listen` lists the matches returned by song.link; use the arrow keys and Enter to copy the desired URL to your clipboard.
- Run `listen --help` anytime to inspect the available flags and supported platforms.

## Building Native Binaries

The project targets both `macosArm64` and `macosX64`. Build the executables for each architecture:

```bash
./gradlew build
```

Gradle produces binaries at:

- `build/bin/macosArm64/debugExecutable/listen.kexe`
- `build/bin/macosX64/debugExecutable/listen.kexe`
- `build/bin/macosArm64/releaseExecutable/listen.kexe`
- `build/bin/macosX64/releaseExectuable/listen.kexe`

## Troubleshooting

- **Native build fails with `xcrun` errors** – ensure Xcode and its command line tools are installed and configured.
- **No link returned for a platform** – song.link does not guarantee coverage for every track or region. The CLI reports the failure and exits non-zero.

## Project Structure

- `src/commonMain` – shared CLI logic, link resolution, terminal helpers, and clipboard abstraction.
- `src/nativeMain` – macOS-specific expect/actual implementations and entry point.

Gradle config lives in `build.gradle.kts` and defines multiplatform targets for macOS only.
