# Bungalow

Bungalow is a **Java / Swing desktop media player** built as a small plugin
operating system for music. Instead of bundling discovery, catalogue lookup,
purchase, streaming, and the whole UI into one closed vertical the way Spotify
or Apple Music do, Bungalow breaks each of those into an independently
swappable contribution that a plugin can provide, replace, or extend.

> **Naming.** *Bungalow* is the product/brand name. *Spacify* is the internal
> code name — you'll still see it throughout the source (the `se.spacify.*`
> packages, the `spacify:` navigation URIs, the `~/.spacify/` config
> directory, the `SpacifyApp` entry point). Those are technical identifiers
> and are left as-is; only the brand wording is "Bungalow".

Full architecture documentation lives in [`docs/`](docs/index.md) (published
as a Jekyll site via GitHub Pages) — start with the
[introduction & background](docs/introduction.md) and the
[architecture overview](docs/architecture.md).

## Requirements

- JDK 25
- Maven

## Building

```
mvn clean package
```

This produces a runnable jar plus its dependency libraries under
`target/app` (`spacify.jar` and `target/app/lib`).

## Running

```
mvn exec:exec
```

This launches the app in a forked JVM (`se.spacify.app.SpacifyApp`) so the
Swing window stays open. `exec:exec` is used instead of `exec:java` because
the latter runs inside Maven's own JVM and exits as soon as `main()` returns.

Alternatively, run the packaged jar directly:

```
java -cp "target/app/spacify.jar:target/app/lib/*" se.spacify.app.SpacifyApp
```

## Testing

```
mvn test
```

## Releases

Tagged builds (`v*`) and pushes to `main` are built by
[`.github/workflows/build.yml`](.github/workflows/build.yml) into a runnable
jar bundle, a Linux `.AppImage`, and a portable Windows app image, published
as GitHub Releases (or a rolling "nightly" prerelease on `main`).

## License

GNU General Public License v2 — see [`COPYING`](COPYING) and
[`LICENSE`](LICENSE).
