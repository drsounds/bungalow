# RFC-0001 — Universal Playlist Sharing (UPS)

| | |
|---|---|
| **Status** | Draft |
| **Created** | 2026-06-30 |
| **Author(s)** | Alexander Forselius (@drsounds) |
| **Source sketch** | [UNIVERSAL_PLAYLIST_SHARING.md](UNIVERSAL_PLAYLIST_SHARING.md) |
| **Affects** | `se.spacify.app.playlist.*`, `se.spacify.net.Uri`, the `spacify:` URI resolver |
| **Supersedes** | — |

> Sections marked **(extends sketch)** add rigor the one-paragraph source sketch
> left implicit; they are proposals, not yet agreed. Genuinely undecided points
> are collected in [§12 Open Questions](#12-open-questions).

---

## 1. Abstract

This RFC defines **Universal Playlist Sharing (UPS)**: a decentralized,
self-contained link format that encodes an entire playlist — its name,
description, optional cover image, and an ordered list of content references —
into a single `spacify:` URI (with an equivalent `https://` mirror). A UPS link
carries the originating user's federated (Mastodon-style) handle for provenance
and resolves, via Spacify's existing URI resolver, into a previewable,
importable [`Playlist`](../../../db/entity/Playlist.java) without requiring a
central server.

## 2. Motivation

Playlists in Spacify are an ordered collection of *mixed* content referenced by
`spacify:` URI (see [`PlaylistRow`](../../../db/entity/PlaylistRow.java) and the
[`ContentCollection`](../../../db/entity/ContentCollection.java) model). Because
content is addressed by stable identifiers (ISRC, artist/release/track names,
recording version) rather than by a provider's row id, a playlist is inherently
portable: any client that can resolve those identifiers can reconstruct the same
listening experience on whatever services it has.

UPS makes that portability shareable. Goals:

- **Decentralized** — a link is self-describing; no central catalogue is required
  to read it.
- **Federated provenance** — each link is attributed to a Mastodon-style handle,
  so playlists travel through the fediverse like any other post.
- **Service-agnostic** — references resolve through the identifier/name lookup
  model, so the recipient plays each item on *their* available services.
- **One click** — produced and consumed from a button in the playlist UI.

## 3. Terminology

The key words **MUST**, **MUST NOT**, **SHOULD**, **SHOULD NOT**, **MAY** are to
be interpreted as in RFC 2119.

- **UPS link** — a complete shareable URI in either the `spacify:federated:` form
  (§6) or the `https:` mirror (§9).
- **Handle** — a Mastodon `acct` identity, `@localpart@domain` (RFC 7565 `acct:`).
- **Document** — the structured, delimited record describing the playlist (§7).
- **Payload** — the `Document`, Base62-encoded, that appears after `:playlist:`.
- **Content reference** — one inner URI naming a playable item (§8).
- **Resolver** — the Spacify component that expands a UPS link into a `Playlist`
  (§10). Today, URI dispatch is `ViewStack.navigate(uri)` → first
  [`View.acceptsUri`](../../../navigation/View.java).

## 4. Design overview

A UPS link has three nested layers:

```
 ┌ spacify:federated:user:<handle>:playlist:<payload> ──────────── transport (§6)
 │                                          │
 │          payload = BASE62( UTF8( Document ) ) ─────────────────── opacity  (§7)
 │                                          │
 │   Document = name : description [: image] : uris[ … ] ────────── structure (§7)
 │             where each leaf value is BASE62 / BASE64URL ──────── safety   (§5)
 └──────────────────────────────────────────────────────────────────────────────
```

The **leaf encoding** (Base62 of each text value) guarantees that no user text
can contain a structural delimiter (`:`, `,`, `[`, `]`). The **outer Base62 wrap**
makes the whole payload a single opaque, transport-safe token. (Whether the outer
wrap is necessary given encoded leaves is [Open Question 12.3](#12-open-questions).)

## 5. Encoding primitives

Spacify currently ships **no** Base62/Base64 codec; this RFC requires adding one
(e.g. `se.spacify.net.Base62`, `se.spacify.net.Base64Url`).

### 5.1 Base62

- Alphabet (index → glyph): `0-9` (0–9), `A-Z` (10–35), `a-z` (36–61).
- `BASE62(bytes)`: interpret `bytes` as a big-endian unsigned integer and emit its
  base-62 representation. Each leading `0x00` byte **MUST** be preserved as a
  leading `'0'` glyph (as Base58Check preserves leading zeros), so the transform
  is exactly reversible.
- `BASE62(text)` is shorthand for `BASE62(UTF8(text))`.

### 5.2 Base64url

- `BASE64URL(bytes)`: RFC 4648 §5 (URL- and filename-safe alphabet), **without**
  padding. Used only for the optional cover image (§7).

## 6. The `spacify:federated:` URI

```abnf
ups-uri   = "spacify:federated:user:" handle ":playlist:" payload
handle    = "@" localpart "@" domain        ; Mastodon acct, RFC 7565
localpart = 1*( ALPHA / DIGIT / "_" / "." / "-" )
domain    = 1*( ALPHA / DIGIT / "." / "-" )
payload   = 1*BASE62CHAR
BASE62CHAR = DIGIT / %x41-5A / %x61-7A       ; 0-9 A-Z a-z
```

The literal `@` and `:` inside `handle` are part of the grammar and are **not**
escaped; a resolver tokenizes by locating the fixed `:playlist:` separator, then
treats everything before it (after `spacify:federated:user:`) as the handle and
everything after as the opaque `payload`.

## 7. Document grammar (the decoded payload)

After `BASE62`-decoding `payload` to UTF-8 bytes, the result is a `Document`:

```abnf
document    = name ":description:" description
              [ ":image:" image ]
              ":uris:" uri-list

name        = BASE62TEXT          ; BASE62( UTF8( display name ) )
description = BASE62TEXT          ; BASE62( UTF8( description ) )  (may be empty)
image       = BASE64URLTEXT       ; BASE64URL( UTF8( image URL ) )  — see 12.4
uri-list    = "[" [ content-ref *( "," content-ref ) ] "]"
content-ref = BASE62TEXT          ; BASE62( UTF8( content URI, §8 ) )
```

Because every leaf is Base62/Base64url, the structural bytes `:` `,` `[` `]`
appear **only** as delimiters and never inside a value — this is the property the
parser relies on.

**(extends sketch)** A resolver **SHOULD** ignore unknown `:key:` segments
between `description` and `uris` so the format can grow (e.g. a future
`:format-version:`, see [12.6](#12-open-questions)) without breaking old readers.

## 8. Content references

Each `content-ref`, once Base62-decoded, is a URI in Spacify's identifier/name
lookup space. A resolver **MUST** support at least:

| Form | Meaning |
|---|---|
| `spacify:isrc:<isrc>` | A recording by ISRC — the canonical, portable key. Mirrors [`Recording.getPlayUri()`](../../../db/entity/Recording.java) (`spacify:recording:isrc:<isrc>`). |
| `spacify:artist:<artist>:release:<release>:track:<n>:recording:<recording>:version:[<version>]` | A recording addressed by human-readable names + position, with an **optional** recording version in `[ ]`. The fallback when no ISRC is known. |
| `https:` / `http:` | An external resource (e.g. a video or a store page). |

```abnf
content-uri = isrc-ref / name-ref / web-ref
isrc-ref    = "spacify:isrc:" 1*VCHAR
name-ref    = "spacify:artist:" seg ":release:" seg ":track:" 1*DIGIT
              ":recording:" seg ":version:" "[" [ version ] "]"
seg         = 1*VCHAR        ; a name; carried Base62-wrapped at the list level
web-ref     = ( "http" / "https" ) "://" 1*VCHAR
```

Names inside a `name-ref` need no further escaping because the entire
`content-ref` is Base62-encoded as one list element. Resolution of each form is
§10.3.

## 9. HTTPS mirror

Every UPS link has an equivalent web URL so it is clickable outside the app and
indexable by fediverse software:

```abnf
ups-url  = "https://" base-domain "/user/" handle "/playlist/" payload
```

- `base-domain` is the canonical UPS web host — **TBD**, see
  [12.1](#12-open-questions).
- `handle` here **MUST** be percent-encoded per RFC 3986 (the `@` and `.` are
  URL-safe but encoding keeps it unambiguous); `payload` is already URL-safe
  (Base62 ⊂ unreserved).
- The two forms are interchangeable: a resolver normalizes either into
  `(handle, payload)` before decoding.

**(extends sketch) Embedded vs Hosted.** The same `payload` MAY be served two
ways:
- **Embedded** — the link *is* the data (subject to the §11 size limit).
- **Hosted** — the web host stores the full playlist under `payload` (treated as
  an opaque id), letting the HTTPS form carry playlists too large to embed. A
  resolver SHOULD prefer embedded data when present and fall back to fetching the
  host only when the payload is a bare id. (Hosting protocol is out of scope.)

## 10. Resolution

### 10.1 Entry points

- **In-app:** register a resolver `View`/handler whose `acceptsUri` matches
  `spacify:federated:…` (and the existing `spacify:playlist:<uuid>` continues to
  open local playlists via [`PlaylistView`](../views/PlaylistView.java)).
- **Web/deep link:** the `https://<base-domain>/user/…/playlist/…` form enters
  through the web layer and is normalized to the `spacify:federated:` form.

### 10.2 Algorithm

1. Normalize the link to `(handle, payload)`.
2. `Document ← UTF8⁻¹( BASE62⁻¹( payload ) )`.
3. Split `Document` on the structural delimiters into `name`, `description`,
   optional `image`, and the `content-ref` list; Base62/Base64url-decode each
   leaf. Reject and surface an error if any leaf fails to decode (§13).
4. For each `content-ref` in order, resolve to a `Playable` (§10.3). Preserve list
   order; unresolved references are kept as placeholders, not dropped.
5. Build a preview titled `name` with `description`/`image` and the resolved
   items. Offer **Save to Your Playlists**, which calls
   [`PlaylistService.createPlaylist`](../service/PlaylistService.java) then
   `addToPlaylist` per item against the editable
   [`LocalPlaylistService`](../service/LocalPlaylistService.java).

### 10.3 Per-reference resolution

- `spacify:isrc:<isrc>` → look up a recording/track by ISRC across registered
  music services; the recipient's "Play with…" picker selects the actual service
  at play time.
- `spacify:artist:…:recording:…:version:[…]` → resolve by name/position against
  the catalogue services, preferring an exact `version` match when given.
- `http(s):` → handed to the web/media layer as an external item.

The `handle` is **provenance only** in embedded mode; a resolver MAY use WebFinger
(RFC 7033) on the handle's `domain` to attribute or to fetch a Hosted payload, but
**MUST NOT** require the origin instance to be reachable to read an embedded link.

## 11. Size constraints

URIs are widely capped at ~2048 characters. The **embedded** form therefore has a
finite item budget:

```
budget(items) ≈ len("spacify:federated:user:") + len(handle)
              + len(":playlist:")
              + ceil( (overhead + Σ encoded-leaf-bytes) × 62/62-expansion )
```

Rules:

- A producer **MUST** keep an embedded UPS link ≤ **2048** characters.
- When the playlist would exceed the budget, the producer **MUST** either (a) emit
  the **Hosted** HTTPS form (§9) instead, or (b) truncate to the first *N* items
  that fit and mark the result as truncated. (a) is **RECOMMENDED**.
- Base62 expands binary by ~34% (log₂62 ≈ 5.95 bits/char); the double-wrap (§4)
  compounds this. Minimizing per-item bytes — preferring short `isrc-ref`s over
  verbose `name-ref`s — materially increases the embeddable item count. This is
  the practical argument for [collapsing the double wrap (12.3)](#12-open-questions).

## 12. Open questions

1. **Canonical `base-domain`** for the HTTPS mirror (§9) — undecided in the sketch.
2. **Signing / authenticity.** v1 links are unauthenticated; a `handle` can be
   spoofed. Should the payload carry an instance signature (e.g. HTTP Signatures /
   the user's ActivityPub key) so recipients can verify origin? (§13)
3. **Double-encoding.** Leaves are already Base62 (delimiter-safe), so the outer
   `payload` wrap only adds opacity, not safety, while inflating size (§11). Keep,
   or define the payload as the raw delimited `Document` (URL-escaped)?
4. **`image` semantics.** The sketch says `base64_image_url`. This RFC reads it as
   `BASE64URL(UTF8(image URL))` (a link). Alternative: inline the image *bytes*
   (much larger; usually impossible within §11). Confirm "URL, not bytes."
5. **Versioned `name-ref` matching.** Exact behavior when `version:[]` is empty vs
   a value not found — pick newest? nearest? fail?
6. **Format version tag.** Add an explicit UPS format version (e.g. a leading
   `:v1:` segment) so future breaking changes are detectable. Recommended.
7. **Ordering & dedup.** Confirm list order is authoritative (it maps to
   `PlaylistRow.row_index`) and whether duplicate references are allowed.

## 13. Security & privacy considerations

- **Encoding ≠ encryption.** Base62/Base64url are reversible; a UPS link exposes
  the full playlist to anyone who holds it. Do not put private data in `name`/
  `description`.
- **Spoofable provenance.** Without §12.2 signing, the `handle` is a claim, not a
  proof. UI **SHOULD** present it as "shared by <handle>" rather than as verified
  authorship.
- **External references.** `http(s):` content refs and the `image` URL are fetched
  from third parties and can act as tracking pixels / leak the recipient's IP. A
  resolver **SHOULD** fetch them lazily and **MAY** gate them behind user consent.
- **Injection.** The parser trusts that structural delimiters never appear inside a
  value; this holds only because every leaf is Base62/Base64url. A resolver **MUST**
  reject a `Document` whose leaves do not cleanly decode rather than guessing.
- **Resource limits (DoS).** A resolver **MUST** bound the decoded size, the item
  count, and per-reference network work, independent of the §11 producer limit.

## 14. Worked example (illustrative)

A two-item playlist by `@drsounds@mastodon.social` named *Roadtrip*
("Summer bangers"), one ISRC item and one name-addressed item:

```
Document (before the outer wrap; B62(x) tokens shown symbolically):
  B62("Roadtrip"):description:B62("Summer bangers"):uris:[
    B62("spacify:isrc:GBAAA0000001"),
    B62("spacify:artist:Aphex Twin:release:SAW 85-92:track:1:recording:Xtal:version:[]")
  ]

UPS link:
  spacify:federated:user:@drsounds@mastodon.social:playlist:<BASE62(UTF8(Document))>

HTTPS mirror:
  https://<base-domain>/user/%40drsounds%40mastodon.social/playlist/<same payload>
```

(The `B62(...)`/`<payload>` tokens are placeholders; a conforming encoder emits
concrete `0-9A-Za-z` strings.)

## 15. UX

- **Share** — a button in [`PlaylistView`](../views/PlaylistView.java) builds the
  UPS link for the current playlist, choosing embedded vs hosted per §11, and
  copies it to the clipboard / opens the system share sheet.
- **Open** — pasting a `spacify:federated:` link, or visiting the HTTPS mirror,
  routes through the resolver (§10) to a preview with **Save to Your Playlists**.

## 16. References

- RFC 2119 — Key words for requirement levels.
- RFC 3986 — URI generic syntax.
- RFC 4648 — Base16/32/64 (Base64url, §5).
- RFC 7033 — WebFinger.
- RFC 7565 — the `acct:` URI scheme (Mastodon handles).
- Internal: [`Playlist`](../../../db/entity/Playlist.java),
  [`PlaylistRow`](../../../db/entity/PlaylistRow.java),
  [`PlaylistService`](../service/PlaylistService.java),
  [`View.acceptsUri`](../../../navigation/View.java),
  [`net.Uri`](../../../net/Uri.java).
