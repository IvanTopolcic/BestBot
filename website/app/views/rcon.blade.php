@include ('includes.header')

<div class="main-content">
	<div class="block">
		<div class="heading">Usage</div>
		<div class="content">
			Upon starting a server, BestBot will private message (PM) you the RCON password for that server. In order to make use of this RCON password,
			you will have to join that server, open the console (default is the ` key), and type: <span class="code">send_password your_pass_here</span>.
			If the password was accepted, an 'Access Granted' message should pop up, otherwise, you may have mistyped the password. It should be noted that
			you can also copy-paste the password.
		</div>
		<div class="content">
			Once you have access, any server setting can be changed. You just need to type command in the console, prefixed by 'rcon'. For example, to change
			the fraglimit to 50, you'd type <span class="code">rcon fraglimit 50</span>. To see the current value of the setting, simply type
			<span class="code">rcon fraglimit</span>.
		</div>
		<div class="content">
			When a command's value has spaces, you have to include quotes ("") around it, for example:
			<span class="code">rcon sv_rconpassword "new password"</span>
		</div>
		<div class="content">
			Here are a few simple RCON tips to follow:
			<ul style="margin-top: 10px;">
				<li>Not all settings will tell you they were changed. They will still be changed.</li>
				<li>Some changes (such as skill) require a map change to take effect. To do so, type <span class="code">rcon map map01</span> to change the map. You
				will also need to re-send the RCON password on a hard map reset.</li>
				<li>Commands can be autocompleted with the tab key, so you can see a list of commands.</li>
			</ul>
		</div>
		<div class="heading">RCON Commands</div>
		<div class="content">
			<a href="http://wiki.zandronum.com/Category:Console_Configuration" target="_blank">http://wiki.zandronum.com/Category:Console_Configuration</a>
			<br />
			<a href="http://wiki.zandronum.com/Server_Variables" target="_blank">http://wiki.zandronum.com/Server_Variables</a>
		</div>
		<div class="heading">Rules</div>
		<div class="content">
			Please note that although the server started is still yours, you need to abide by a set of rules in order to keep using BE. These are as follow:
			<ol style="margin-top:10px;">
				<li>You may not kick/ban random players for no reason. Each punishment must have a valid reason associated with it.</li>
				<li>Please keep sv_cheats off.</li>
			</ol>
		</div>
		<div class="heading">Recovering your password</div>
		<div class="content">
			If you forget your RCON password, you can type <span class="code">/msg BestBot .rcon &lt;port&gt;</span>. If you don't remember your server's port number,
			you can type <span class="code">/msg #bestever .servers &lt;your_username&gt;</span> to see a list of your running servers.
		</div>
	</div>
</div>

@include ('includes.footer')