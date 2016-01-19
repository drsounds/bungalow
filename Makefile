all:
	rm -rf cache
	rm -rf settings
	rm -rf ../Bungalow.app/Contents/Resources/app.nw
	cp -R . ../Bungalow.app/Contents/Resources/app.nw
	