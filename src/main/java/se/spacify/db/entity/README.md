Refactor to this pattern:

We want to work with abstraction at high level so views can share characteristics.

1. Rename the nomenklure title to 'name' in all entities, because it is the new standard.
2. Add a 'version' field to Recording.
3. We want a root abstract entity called Node which have getName() and getVersion().

Then, rename the Artist nomenclature to Creator. 

Then we have an abstract Content class which Recording and Release are subclassing from it, which adds getCreatorCredits() seCreatorCredits() which is a list of CreatorCredit like in Release and Recording that is abstracted to CreatorCredit<T extends Content>, where ReleaseCreatorCredits and RecordingCreatorCredits are subclassing (ReleaseCreatorCredits extends CreatorCredits<Release>, RecordingCreatorCredits<Relase> etc.), and the getCreatorCredits and setCreatorCredits in Content subclasses is then abstracted to the correct subclass like above.
This lays the foundation for expanding to other verticals and content types later (Show/Season/Episode, Video, Game etc.) which also can share the same infrastructure like the Buy/Purchase button because new verticals and music is inheriting from Media and Content.

Move today's implementation of Release subclass of Release, MusicRelease and make Release a generic class Release<T extends Content>, to support future release types (VideoRelease, GameRelease etc.).  

4. Detach the file path from the Recording node, into a on own table, and remove the file path field from the Recording table altogether. which references the Recording node, in order to make all playback, streaming and purchase types treated equally, so local files is as much a 'streaming service' as YouTube.

