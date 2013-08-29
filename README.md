twitterWall
===========
Twitter Wall, or Twitter Fall depending on who you ask, is a Processing based twitter client which displays user selected tweets in a constant trickle. These falling words emulate water droplets splashing down off a water fall. The user is able to interact via pusing tweet words out of the way using a webcam and the Flob Blob detection library.

Installation
============
It is compiled but you cant install it in its current stage, this has something to do with the gstreamer lib available on OSX, but its not clear what the exact issue is __ Fix this, its lame__

Use
===
Flob and Twitter4j should already be included in the repo.

* Download the repo

* Add the repo to Eclipse

* Download (Processing)[processing.org] and install it to /Applications/Processing.app

* Create a keys.txt file in the repo, add it to the sublime project

* Paste keys from twitter into the keys.txt file using the following format



    Twitter OAuthConsumerKey on line1
    Twitter OAuthConsumerSecret on line2
    Twitter OAuthAccessToken on line3
    Twitter OAuthAccessTokenSecret on line4
    Kuler Key on line5 (this key is optional as the feature has been disabled, but something MUST be on this line __ Fix this, its lame__)

