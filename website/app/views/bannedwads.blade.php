@include ('includes.header')

<div class="main-content">
	<div class="heading">Banned Wads</div>
	<div class="block">
		<table class="table table-bordered table-striped" style="background: #F5F5F5">
			<div style="margin: 5px">
				Wads on this page can still be downloaded, but cannot be hosted primarily due to causing strain on our system via unintended (or intentional) ACS bugs. Wads with
				copyrighted material will be deleted instead of appearing on this page.
			</div>
			<tr>
				<th>Name</th>
				<th>MD5 Hash</th>
			</tr>
				@foreach ($bannedwads as $wad)
				<tr>
					<td><a href="http://www.best-ever.org/download?file={{{ urlencode($wad->name) }}}">{{{ $wad->name }}}</a></td>
					<td>{{{ $wad->md5 }}}</td>
				</tr>
				@endforeach
		</table>
</div>

@include ('includes.footer')