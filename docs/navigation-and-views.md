# Navigation & Views

Every screen in Bungalow is a **View** bound to a **`spacify:` URI**. Navigation
is a single-stack router: ask to go to a URI, and the first registered view that
accepts it is shown. This keeps screens decoupled — a plugin contributes a view
and a URI, and anything (sidebar, header, another view, a deep link) can navigate
to it without a direct reference.

Types: [`View`](../src/main/java/se/spacify/navigation/View.java),
[`ViewStack`](../src/main/java/se/spacify/navigation/ViewStack.java),
[`SidebarNode`](../src/main/java/se/spacify/navigation/SidebarNode.java),
`NavigationListener`, `PlayerView`.

## The `View` contract

```java
public abstract class View {
    public View(ViewStack viewStack) { … }
    public abstract boolean acceptsUri(String uri);   // "is this URI mine?"
    public abstract void    navigate(String uri);     // "render this URI"
    public abstract JComponent getComponent();        // the Swing component shown
    public String getTitle() { return ""; }
    public void onShow() {}  public void onHide() {}

    // Views with their own history (e.g. the embedded browser) opt in:
    public boolean handlesHistory() { return false; }
    public boolean canGoBack() / canGoForward() / goBack() / goForward();
}
```

`acceptsUri` is typically a regex or prefix test, so one view can serve a family
of URIs (e.g. `ReleaseDetailView` accepts `spacify:library:release:<id>`).

## The `ViewStack` router

[`ViewStack`](../src/main/java/se/spacify/navigation/ViewStack.java) is a `Panel`
that holds the registered views and the current one, plus back/forward history.

`navigate(uri)`:

1. Find the first registered view with `acceptsUri(uri) == true` (no match → no-op).
2. If it's the **same** view (different URI) → retarget it in place via
   `view.navigate(uri)` (important for heavyweight views like the JCEF browser —
   no component swap).
3. Otherwise hide the old view, swap in `view.getComponent()`, call
   `navigate` + `onShow`, push history, and notify `NavigationListener`s.

Back/forward first defer to a view that `handlesHistory()` (the browser consumes
its own back), then fall back to the URI history stacks. `NavigationListener`s
(the header's address field, the back/forward buttons, the immersive-mode
toggle) are notified on every change.

> A view is only reachable once it is **registered** with the stack — via
> `ctx.registerView(v)` from a plugin, or `viewStack.registerView(v)` for views
> the window owns (e.g. the now-playing view). An unregistered view means
> `navigate` silently matches nothing.

## The `spacify:` URI scheme

URIs are opaque strings matched by views; there is no central registry, which is
what keeps the scheme open — a plugin coins its own namespace. Conventions in use:

| URI pattern | View | Provided by |
|-------------|------|-------------|
| `spacify:now-playing`, `spacify:home` | Now Playing | window / media plugin |
| `spacify:library`, `:tracks` `:recordings` `:releases` `:artists` `:local` | library lists | `library` |
| `spacify:library:release:<id>`, `spacify:library:artist:<id>` | detail pages | `library` |
| `spacify:recording:isrc:<isrc>` | recording resolver | `library` / music |
| `spacify:playlist:<id>` | playlist | `playlist` |
| `spacify:search`, `spacify:search?q=…` | search | `search` |
| `spacify:catalog:<serviceId>:artists\|releases\|recordings` | catalogue browse | `catalog` |
| `spacify:site:<host>`, `spacify:store:<host>` | embedded browser / store | `web` |
| `spacify:youtube:<id>` | YouTube playback | `youtube` |
| `spacify:plugins` | plugin manager | UI |

Query strings (`?q=…`, `?artist=…`) carry view parameters; the accepting view
parses them in `navigate`.

## The sidebar

[`SidebarNode`](../src/main/java/se/spacify/navigation/SidebarNode.java) is a
plain tree of `{ label, uri, children, icon }`. A leaf with a URI navigates on
click; a branch groups children. Plugins contribute subtrees via
`ctx.addSidebarNode(node)`, which returns a `SidebarHandle` for **post-activation
edits** — adding/removing children as state changes, and `expand()`.

The sidebar itself (`LeftLibraryMenu`) is **owned by `MainWindow`**, created
before plugins activate, and then embedded by the active Chrome. That ownership
split is deliberate: the Chrome can change at runtime (switch Design) without
losing the plugin-contributed nodes. See [Look & feel](look-and-feel.md).
