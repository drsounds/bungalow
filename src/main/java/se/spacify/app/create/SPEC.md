/feature-dev:feature-dev Create an app 'se.spacify.app.create' with concept CreateConcept,and CreateFeature, and a spacify:create:app route, which leads to a page with an input where user can prompt a new app. It is a done with a AgentService using Lang4ChainJ library, it should be vibe coding an app based on se.spacify.* APIs on the fly, in the background with async stream to Swing,  which can extend the app with new concept, skin, views, etc.

How it should work:

1. User enter a prompt in spacify:create view, which is POST backed to CreateView's CreateController.

2. CreateController's handle is capturing the posted 'prompt' text.

3. With prompt text, it calls CreateService startCreatd which tells the the server side agent service to create a new Lang4JAgentSession, which gets the prompt and give directions to read the docs about app creation docs/plugins.md and according to thid write the app according to prompt, and spawns a new instance of this app, it ewyer apropriate Java code compiles it as an jar and load it on the fly in a debug mode, but the message thread is saved so user can revisit and make further modifications.

4. The jar app is loaded and activated on the client instance.