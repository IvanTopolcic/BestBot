@include ('includes.header')

<div class="main-content">
	<div class="block">
		<div class="heading">{{{ Auth::user()->username }}} ({{{ Auth::user()->id }}})</div>
		<h1>Slots</h1>
		<div class="block">
			<table style="background: #EEE" class="table table-striped table-bordered table-condensed">
				<tr>
					<th>Slot</th>
					<th>Server String</th>
				</tr>
				@foreach ($slots as $slot)
				<tr>
					<td>{{{ $slot->slot }}}</td>
					<td>{{{ $slot->serverstring }}}</td>
				</tr>
				@endforeach
			</table>
		</div>
		<h1>Servers</h1>
		<div class="block">
			<table style="background: #EEE" class="table table-striped table-bordered table-condensed">
				<tr>
					<th>Unique ID</th>
					<th>Server Name</th>
					<th>Date</th>
				</tr>
				@foreach ($servers as $server)
				<tr>
					<td><a href="http://static.best-ever.org/logs/{{{ $server->unique_id }}}.txt">{{{ $server->unique_id }}}</a></td>
					<td>{{{ $server->servername }}}</td>
					<td>{{{ $server->date }}}</td>
				</tr>
				@endforeach
			</table>
		</div>
		<h1>Configs</h1>
		<div class="block">
			<table style="background: #EEE" class="table table-striped table-bordered table-condensed">
				<tr>
					<th>Name</th>
					<th>Date</th>
				</tr>
				@foreach ($configs as $config)
				<tr>
					<td><a style="margin-right:10px;cursor:pointer;" class="deletable"><span id="{{{ $config->cfgname }}}" class="glyphicon glyphicon-remove"></span></a>
						<a href='/viewconfig?name={{{ urlencode($config->cfgname) }}}'>{{{ $config->cfgname }}}</a>
					</td>
					<td>{{{ $config->date }}}</td>
				</tr>
				@endforeach
			</table>
		</div>
		<h1>Wads</h1>
		<div class="block">
			<table style="background: #EEE" class="table table-striped table-bordered table-condensed">
				<tr>
					<th>Name</th>
					<th>Size</th>
					<th>Date</th>
				</tr>
				@foreach ($wads as $wad)
				<tr>
					<td><a style="margin-right:10px;cursor:pointer;" class="deletable"><span id='{{{ $wad->wadname }}}' class="glyphicon glyphicon-remove"></span></a>
						<a href="/download?file={{{ urlencode($wad->wadname) }}}">{{{ $wad->wadname }}}</a>
						<span style='margin-left:3px;font-size: 8px'><a href='/wadinfo?name={{{ urlencode($wad->wadname) }}}'>[View]</a></span></td>
					<td>{{{ $wad->size }}}</td>
					<td>{{{ $wad->date }}}</td>
				</tr>
				@endforeach
			</table>
		</div>
	</div>
</div>

@include ('includes.footer')