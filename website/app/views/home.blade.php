@include ('includes.header')

<div class="header">
	<div class="logo">
		Best Ever
		<div class="logo-footer">
			A free, open source server host for Zandronum
		</div>
	</div>
</div>

<div class="main-content">
	<div class="block">
		<div class="heading">Welcome</div>
		<div class="content">
			Best Ever is a free, automated server host for Zandronum.
			Started in 2012, our goal was to provide an easy to use stable host for wad authors, but expanded our reach to include any user who wishes to host a server.
			With BE, users can simply upload their files and host a Zandronum server in a few seconds! We have hosted
			tens of thousands of servers over the years, with excellent uptime and availability. This is all provided free of charge for you!
		</div>
		<div class="heading">Why use BE?</div>
		<div class="content">
			<ul>
				<li>Starting a server takes a few seconds</li>
				<li>Upload your own WAD/PK3 files and play the game your way</li>
				<li>Even if you turn off your computer/client, your server will stay up</li>
				<li>RCON access as well as log access to your server, granting you complete control</li>
				<li>Gigabit network to ensure your servers don't lag</li>
			</ul>
			By using BE, players who are unable to host (slow internet connection, can't forward ports) are still able to set up a server to their liking.
		</div>
		<div class="heading">Getting started</div>
		<div class="content">
			At the moment, BE servers are started and managed through IRC. We plan to add a web interface in the future, but for now you'll need to use the old IRC commands.
			The full guide with all options can be viewed <a href="/host" target="_blank">here</a>, a quick guide is provided below.
			<ol style="margin-top: 10px">
				<li>Get an IRC client, connect to Zandronum's IRC server: irc.zandronum.com:6667, and join the #bestever channel. If you're unsure on how to connect, or which
				client to use, you can take a look at Zandronum's IRC guide <a href="http://wiki.zandronum.com/IRC#Software" target="_blank">here</a>.</li>
				<li>Type <span class="code">/msg nickserv register &lt;password&gt; &lt;email&gt;</span></li>
				<li>Check your email, and follow the instructions to finish registering with Zandronum IRC</li>
				<li>After you have validated your account, register with BE by typing <span class="code">/msg BestBot register &lt;password&gt;</span>
				<li>Type <span class="code">.host iwad="doom2.wad" gamemode="duel" wad="dwango5.wad" hostname="My first server"</span>
					in the #bestever channel to start your server</li>
				<li>This will start a duel server named "My first server" with the wad dwango5.wad. Please see
					<a href="/host" target="_blank">this</a> guide for all hosting options.</li>
			</ol>
			<strong>*</strong> Please note that you'll need to re-identify each time you connect to IRC. You need to type
				<span class="code">/msg nickserv identify &lt;username&gt; &lt;password&gt;</span> (using your Zandronum IRC account information) each time you connect.
		</div>
	</div>
</div>

@include ('includes.footer')