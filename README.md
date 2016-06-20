### WARNING
THIS IS DEPRECATED, NO UPDATES (except maybe security patches) WILL BE PUSHED TO THIS REPOSITORY. You are, however, welcome to send pull requests.

### WARNING
DO NOT USE THIS ON IMPORTANT MACHINES. BestBot itself has no known vulnerabilities, but with a combination of Zandronum could potentially post a risk. We do not take responsibility for any problems caused by this software.

#BestBot

BestBot is an IRC bot written in Java. It provides an easy interface for hosting Zandronum (DOOM) servers
straight from IRC, as well as allowing users to manage their hosted servers.

Originally developed for http://www.best-ever.org/ to help players host dedicated Zandronum servers on our machines,
we've expanded our sights in order to allow other server hosts to have an easier time
with managing and setting up servers.

##Features

* Full registration/account system based on the IRC network's hostmasks
* Very easy to start/stop your own servers
* Players are able to save configured servers and load them at any time
* Administrator as well as moderator types, allowing you to give trusted users the ability to moderate servers
* Can grant hosters RCON (admin) of their own server automatically, if set in the options
* Impose a custom server limit on each user (Default: 4)
* Players can upload their own WAD/PK3 files to use for hosting
* Players can upload their own configuration files, allowing the hoster to keep an easy persistent server configuration
* Ability to auto-restart servers on crash

##Building BestBot

###With Ant

**You need JDK 7 to compile BestBot**

1. cd to the working directory
2. type `ant build`
3. the bot should now be compiled!

##Configuration & Running

**You need JRE 1.7 to run BestBot**

1. Edit bestbot.ini and fill in your desired settings 
2. Run the bot with `java -jar BestBot.jar bestbot.ini`

##Libraries
	
* pircbot: http://www.jibble.org/pircbot.php
* MySQL: http://dev.mysql.com/downloads/connector/j/
* ini4j: http://ini4j.sourceforge.net/index.html
* jBCrypt: http://www.mindrot.org/projects/jBCrypt/

##Copyright

Copyright 2013 Best. Ever.

Licensed under the GNU General Public License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.gnu.org/licenses/gpl-2.0.html

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. See the License for the specific language governing permissions and limitations under the License.
