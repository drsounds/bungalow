# Spacify Architecture Documentation

Spacify is a **Java / Swing desktop media player** built as a small **plugin
operating system for music**. Where Spotify or Apple Music bundle *discovery,
catalogue lookup, purchase, streaming, listening history and the entire UI* into
one closed vertical, Spacify breaks each of those into an independently
swappable contribution that a plugin can provide, replace, or extend.

The goal is an **open, Frutiger-Aero–inspired music ecosystem**: the app is the
"OS", and the catalogues, stores, streaming back-ends, playlist providers and
even the whole look-and-feel are "apps" that plug in.

```
        Spotify / Apple Music                         Spacify
   ┌──────────────────────────────┐      ┌──────────────────────────────────┐
   │  discovery                   │      │  discovery   → MusicCatalogueService
   │  lookup / resolution         │      │  resolution  → MusicService + PlaybackCoordinator
   │  purchase / store            │      │  purchase    → store views + AuthAspect
   │  streaming                   │      │  streaming   → MediaService
   │  listening / queue           │      │  listening   → PlayQueue
   │  the entire UI               │      │  UI          → View / Chrome / Skin / Design
   │  ── all welded together ──   │      │  ── each a separate plugin/aspect ──
   └──────────────────────────────┘      └──────────────────────────────────┘
          one vendor, locked                  many vendors, composable
```

## Read in this order

0. [Introduction & background](introduction.md) — *why* this exists: the monolith
   problem, streaming economics, gatekeeping, vendor lock-in, the UX regression,
   and the Longhorn / Windows Media Player road not taken.
1. [Architecture overview](architecture.md) — the layers, the boot sequence, and
   how a play request flows end to end.
2. [The plugin system](plugin-system.md) — how plugins are discovered, activated,
   and cleanly removed; how to write one.
3. [Aspects & Services](aspects-and-services.md) — the capability model that
   replaces the monolith: `MediaService`, `MusicService`,
   `MusicCatalogueService`, `AuthAspect`, …
4. [Navigation & Views](navigation-and-views.md) — the `spacify:` URI space, the
   `ViewStack`, and the sidebar.
5. [Look & feel: Design = Chrome + Skin](look-and-feel.md) — themeable, skinnable,
   re-chromeable UI selected at runtime.
6. [Playback & resolution](playback-and-resolution.md) — the play queue and the
   "Play with…" cross-service resolver.
7. [Status & roadmap](status-and-roadmap.md) — what is wired today, the known
   gaps in the in-progress refactor, and where the ecosystem is headed.

## Vocabulary at a glance

| Term | What it is | Key type |
|------|------------|----------|
| **Plugin** | A unit of distribution that contributes everything below. | `se.spacify.plugin.Plugin` |
| **Aspect** | A capability interface (identity + `onRegister`). Everything pluggable is an Aspect. | `se.spacify.aspect.Aspect` |
| **Service** | A back-end with a lifecycle; takes on capability aspects (streaming, discovery, auth). | `se.spacify.service.Service` |
| **Concept** | A coarse feature domain a plugin activates (library, search, playlists). | `se.spacify.concept.Concept` |
| **Feature** | A bundle of views + sidebar nodes registered as a unit. | `se.spacify.feature.Feature` |
| **View** | A screen bound to a `spacify:` URI. | `se.spacify.navigation.View` |
| **Design** | A premix of a **Chrome** (window shell/layout) and a **Skin** (control painting). | `se.spacify.design.Design` |
| **Theme / Taste** | Colour/tint model; `Taste` is the per-window state, `ThemeManager` the live palette. | `se.spacify.ui.theme.*` |

> These docs describe the code as it currently stands. The codebase is mid-way
> through a refactor toward the Concept/Aspect model; see
> [Status & roadmap](status-and-roadmap.md) for what is and isn't wired yet.
