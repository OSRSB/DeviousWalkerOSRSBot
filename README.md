#DeviousWalkerOSRSBot

Devious client movement api port for RSB. 
This is currently early in development and likely not functional in many ways. 

To try it out, clone the repo and add these to your build files in script-template:

settings.gradle
	
	 include ":DeviousWalkerRSB"
	 project(":DeviousWalkerRSB").projectDir = file("path/To/Cloned/Repo")

build.gradle under dependencies
	 
	    implementation project(":DeviousWalkerRSB")

Next, look at DeviousWalker.java and access the api in this way
	
	 DeviousWalker.walkTo(x, y, z); // non-blocking
	 DeviousWalker.walkToCompletion(x, y, z) // blocking