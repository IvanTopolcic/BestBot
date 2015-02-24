@include ('includes.header')

<div class="main-content">
	<div class="heading">{{{ $wad->wadname }}}</div>
	<div class="block margin-top">
		<table class="table table-bordered">

		<tr>
			<td>Path</td>
			<td>Size</td>
			@if ($wad->type == 'pk3')
				<td>Last Modified</td>
			@endif
		</tr>
		@foreach($wad->files as $file)
		<tr>
			<td><a href="/wadinfo?name={{{ $wad->wadname }}}&file={{{ $file->filename }}}">{{{ $file->filename }}}</a></td>
			<td>{{{ $file->content_size }}}</td>
			@if ($wad->type == 'pk3')
				<td>{{{ date('Y-m-d H:i:s', $file->last_modified) }}}</td>
			@endif
		</tr>
		@endforeach
		</table>
	</div>
</div>

@include ('includes.footer')