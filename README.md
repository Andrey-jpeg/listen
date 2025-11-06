# listen

`listen` is a Kotlin Multiplatform command-line tool that rewrites song links between streaming services. Give it a URL from Apple Music, Spotify, YouTube, etc., and it will ask the public [song.link](https://song.link/) API for the matching link on your target platform.

## Prerequisites

- Java 21 (the Gradle wrapper will download the toolchain automatically for JVM builds)
- macOS with Xcode command-line tools for native compilation (`xcode-select --install`)

## Usage

Run the tool on the JVM while developing:

```bash
./gradlew runJvm -Pargs='-p apple-music https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT'
```

- `-p/--platform` chooses the destination catalogue (defaults to Spotify). Valid values match the list from `listen --help`.
- The final argument is the source song URL.

If the song.link API cannot provide a link for the requested platform, the command exits with code `1` and prints the error message.

## Building Native Binaries

The project targets both `macosArm64` and `macosX64`. Build the release executables for each architecture:

```bash
./gradlew linkReleaseExecutableMacosArm64 linkReleaseExecutableMacosX64
```

Gradle produces binaries at:

- `build/bin/macosArm64/releaseExecutable/listen.kexe`
- `build/bin/macosX64/releaseExecutable/listen.kexe`

To ship a universal macOS binary (required for Homebrew), create a fat binary with `lipo`:

```bash
mkdir -p build/distributions
lipo -create \
  build/bin/macosArm64/releaseExecutable/listen.kexe \
  build/bin/macosX64/releaseExecutable/listen.kexe \
  -output build/distributions/listen
chmod +x build/distributions/listen
```

Sign and notarise the binary if you plan to distribute it widely.

## Homebrew Publishing Workflow

1. Build the fat binary as shown above and archive it (e.g. `tar czf listen-<version>-macos.tar.gz -C build/distributions listen`).
2. Create a GitHub release that hosts the archive and note the SHA256 checksum.
3. Author a tap formula or submit to `homebrew-core`. A minimal tap formula might look like:

   ```ruby
   class Listen < Formula
     desc "Convert streaming links between music platforms"
     homepage "https://github.com/andreyjpeg/listen"
     url "https://github.com/andreyjpeg/listen/releases/download/v0.1.0/listen-0.1.0-macos.tar.gz"
     sha256 "<SHA256>"
     version "0.1.0"

     def install
       bin.install "listen"
     end

     test do
       output = shell_output("#{bin}/listen --help")
       assert_match "Target platform", output
     end
   end
   ```

4. `brew tap` your repository (or submit the formula upstream) so users can install with `brew install <tap>/listen`.

## Troubleshooting

- **Native build fails with `xcrun` errors** – ensure Xcode and its command line tools are installed and configured.
- **No link returned for a platform** – song.link does not guarantee coverage for every track or region. The CLI reports the failure and exits non-zero.
- **Passing arguments from Gradle** – use `-Pargs='...'` (or `-PappArgs='...'`) to forward CLI arguments when running via the Gradle task.

## Project Structure

- `src/commonMain` – shared CLI logic and link-resolution code.
- `src/jvmMain` – JVM entry point, CIO engine wiring, and stderr/exit implementations.
- `src/nativeMain` – Native entry point and CIO configuration for macOS targets.

Gradle config lives in `build.gradle.kts` and defines multiplatform targets plus the helper `runJvm` task for quick feedback during development.
