/feature-dev:feature-dev Create an app 'se.spacify.app.create' with concept CreateConcept,and CreateFeature, and a spacify:create:app route, which leads to a page with an input where user can prompt a new app. Using a built in distributable javac, it should be able to vibe code an app on the fly, based on the se.spacify.* Java API, which can extend the app with new concept, skin, views, etc.

How it should work:

1. User enter a prompt in spacify:create view, which is POST backed to CreateView's CreateController.

2.CreateController's handle is capturing the posted 'prompt' text.

3. With prompt text, it calls CreateService startCreatr which tells the the server side agent service to create a new ClaudeAgentSession, which gets the prompt and give directions to read the docs about app creation docs/plugins.md and spawns a new instance of this app, it ewyer apropriate Java code compiles it as an jar and load it ans test it, then pings back the AgentService through web sock once the JAR is completed.

4. The jar app is loaded and activated.