@include ('includes.header')

<div class="main-content">
	<div class="block">
		<div class="heading">Starting a server</div>
		<div class="content">
			Assuming that you've correctly made an account with Zandronum IRC as well as Best Ever, the below information will help you fine tune your server.
			All of the options listen below are in a option="value" format. These can be added to the end of your server hosting command to include additional options.
			As a default, we'll be using <span class="code">.host iwad="doom2.wad" hostname="My server" gamemode="duel"</span> as a reference, as those three options
			(iwad, hostname, and gamemode) are required to start a server. When you're done making the host command, simply send it as a message to #bestever and it
			should start!
		</div>
		<div class="heading">Host Options</div>
		<div class="content">
			<table class="table table-bordered table-striped" style="background: #F5F5F5">
				<tr>
					<th>Option</th>
					<th>Description</th>
					<th>Example</th>
				</tr>
				<tr>
					<td><strong>Iwad</strong></td>
					<td>Adds an IWAD to your server. Each server can only have one IWAD.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop"</span></td>
				</tr>
				<tr>
					<td><strong>Hostname</strong></td>
					<td>Sets the hostname for the server. This is what will appear on the master server list.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop"</span></td>
				</tr>
				<tr>
					<td><strong>Gamemode</strong></td>
					<td>Sets the gamemode for the server.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop"</span></td>
				</tr>
				<tr>
					<td><strong>Wad</strong></td>
					<td>Adds a single, or multiple wads/pk3s to your server.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test" gamemode="coop" wad="firstwad.wad, secondwad.pk3"</span></td>
				</tr>
				<tr>
					<td><strong>Skill</strong></td>
					<td>Adjusts the skill of the server. 4 is nightmare.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" skill=4</span></td>
				</tr>
				<tr>
					<td><strong>Data</strong></td>
					<td>Enables skulltag data (skulltag_actors and skulltag_data)</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" data=true</span></td>
				</tr>
				<tr>
					<td><strong>Config</strong></td>
					<td>Loads an uploaded configuration file. You can upload/view configuration files <a href="/configs">here</a>.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" config="example.cfg"</span></td>
				</tr>
				<tr>
					<td><strong>Autorestart</strong></td>
					<td>If the server crashes, automatically restarts it.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" autorestart=true</span></td>
				</tr>
				<tr>
					<td><strong>Dmflags</strong></td>
					<td>Sets the dmflags.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" dmflags=1024</span></td>
				</tr>
				<tr>
					<td><strong>Dmflags2</strong></td>
					<td>Sets the dmflags2.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" dmflags2=1024</span></td>
				</tr>				<tr>
					<td><strong>Dmflags3</strong></td>
					<td>Sets the dmflags3.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" dmflags3=1024</span></td>
				</tr>
				<tr>
					<td><strong>Compatflags</strong></td>
					<td>Sets the compatflags.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" compatflags=1024</span></td>
				</tr>
				<tr>
					<td><strong>Compatflags2</strong></td>
					<td>Sets the compatflags2.</td>
					<td><span class="code">.host iwad="doom2.wad" hostname="Test name" gamemode="coop" compatflags2=1024</span></td>
				</tr>

			</table>
		</div>
		<div class="heading">Managing your server</div>
		<div class="content">
			After you've started the server, BestBot should send you a private message containing the RCON password of your server. If you'd like to utilize RCON
			to change more advanced settings around, follow the <a href="/rcon/">RCON Guide</a>. If you'd like to retreive your RCON password, simply send this message to IRC:
			<span class="code">/msg BestBot .rcon &lt;port&gt;</span>. Alternatively, check the website for the RCON/Logfile.
		</div>
	</div>
</div>

@include ('includes.footer')