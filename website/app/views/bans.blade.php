@include ('includes.header')

<div class="main-content">
	<div class="heading">Bans</div>
	<div class="block">
		This is the Best Ever banlist. Users are added here when they break Zandronum Master Server rules (cheating, excessive trolling, negative attitude, etc).
		If you were wrongfully added, please visit irc.zandronum.com:6667 #bestever and speak to an administrator.
	</div>
	<div class="block">
		Please note that VPN bans are hidden by default. If you'd like to see them click <a href="?vpn=1">here</a>.
	</div>
	<div class="block">
		<table style="background: #EEE" class="table table-striped table-bordered table-condensed">
			<tr>
				<th>IP Address</th>
				<th>Reason</th>
			</tr>
			@foreach ($bans as $ban)
				<tr>
					<td>{{{ $ban->ip }}}</td>
					<td>{{{ $ban->reason }}}</td>
				</tr>
			@endforeach
		</table>
	</div>
</div>

@include ('includes.footer')