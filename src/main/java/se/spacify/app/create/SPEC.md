# Module Spec: Spacify Create

## 1. Context & Purpose

**What:** Allow users to create plugins directly inside the Bungalow app without coding
**Why:** To boost community of this app

## 2. Boundaries & Interfaces

**Inputs:** User prompts what the app should do and follow up, file attachments (.pdf, .txt)
**Outputs:** Source code and jar file of the generated plugin in the ~/Bungalow folder, a Lang4ChainJ session
**Dependencies:** Lang4ChainJ Java library, ECJ (Eclipse Compiler for Java)

## 3. Core Logic & Flow

1. Create a Lang4ChainJ agent session, save in the database. 
2. If not already there, create a new folder, ~/Bungalow/<plugin name taken from prompt>, Provide Lang4ChainJ the prompt, the directory to the docs/plugin-system.md folder, and instruct it to create a java project which build a plugin in Java according to the spec and API in this folder, which links to this app's .jar. 
3. Invoke ECJ to build a .jar from the project.
4. This can be repeated by answers from users to improve the vibe coded app, through repeating the steps without creating new but instructing Lang4ChainJ agent session to improve the existing code according to the user's session

## 4. Error handling

**Empty prompt:** Show a message
**Agent unavailable:** Show a message

## 5. Acceptance

- [ ] User can open a view with the input field at spacify:create
- [ ] Once user has opened the view, it can enter the app in the text area
- [ ] When clicking start, the button transform into a spinner, and the system is begin to work
- [ ] Then redirect to an URI spacify:agent-session:<id of session> which is a direct link to the session
- [ ] Here it shows the thread which the user can reply and follow up for improvements
- [ ] After each generation, install the new version of the generated plugin .jar in the main app