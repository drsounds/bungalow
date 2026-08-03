
/feature-dev:feature-dev Create an app 'se.spacify.app.create' with concept CreateConcept,and Create according to SPEC.md in src/java/se/spacify/app/create/SPEC.md

spacify:create:app route, which leads to a page with an input where user can prompt a new app. It is a done with a AgentService using Lang4ChainJ library, it should be vibe coding an app based on se.spacify.* APIs on the fly, in the background with async stream to Swing,  which can extend the app with new concept, skin, views, etc.

package: se.spacify.app.create

spacify:conversation:<conversation id> for the build

spacify:create - general Lovable like vibe coding start page

# How it should work:

1. User enter a prompt in spacify:create view, which is POST backed to CreateView's CreateController.
2. CreateController's handle is capturing the posted 'prompt' text.
3. It starts the app bulding (Se How prompt building works section)

# How prompt building works

1. With prompt text, it calls CreateService startCreate which tells the the server side agent service to create a new Lang4JAgentSession, which gets the prompt and give directions to read the docs about app creation docs/plugins.md and according to the technical guidelines write and build the app according to prompt.
2. This session can be revisited as user can return to the message thread is saved so user can revisit and make further modifications.
3. spawns a new instance of this app, apropriate Java code compiles it as an jar and load it on the fly in a debug mode.