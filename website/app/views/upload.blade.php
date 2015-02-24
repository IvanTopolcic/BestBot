@include ('includes.header')

<script type="text/javascript">
	function getFile(){
		document.getElementById("upfile").click();
	}
	function sub(obj){
		var file = obj.value;
		var fileName = file.split("\\");
		if (fileName[fileName.length-1].length > 26) {
			fileName[fileName.length-1] = fileName[fileName.length-1].substring(0, 26) + "...";
		}
		document.getElementById("upload-button").innerHTML = fileName[fileName.length-1];
		$('#submit').removeClass('disabled');
	}
</script>

<div class="main-content">
	<div class="block">
		<div class="heading">Upload</div>
		<div class="content">
			Here you can upload your favorite DOOM wads, as well as configuration files. Accepted filetypes are WAD, PK3, PK7, as well as CFG for configuration files.
			The maximum upload size is 400MB.
		</div>
	</div>

	<div class="block">
		<form id="upload" action="/file-upload" method="post" enctype="multipart/form-data">
			<div id="upload-button" onclick="getFile()">Click to upload</div>
			<div style='height: 0px;width: 0px; overflow:hidden;'><input id="upfile" name="file" type="file" value="upload" onchange="sub(this)"/></div>
			<div class="text-center" style="margin-top: 10px"><button type="submit" id="submit" class="btn btn-primary disabled">Submit</button></div>
		</form>
	</div>

	<div class="percent"></div>

	<div id="status"></div>

	<div class="heading">Latest Uploads</div>
	<div class="content">
		<table class="table table-striped table-bordered" style="background: #EEE">
			<tr>
				<th>Name</th>
				<th>Size</th>
				<th>Uploader</th>
				<th>Date</th>
				<th>MD5</th>
			</tr>
				@foreach ($wads as $wad)
					<tr>
						<td>@if (Auth::check() && Auth::user()->level > 14)
							<a style="margin-right:10px;cursor:pointer;" class="deletable"><span class="glyphicon glyphicon-remove" id='{{{ $wad->wadname }}}'></span></a>@endif
							<a href="/download?file={{{ urlencode($wad->wadname) }}}">{{{ $wad->wadname }}}</a>
							<span style='margin-left:3px;font-size: 8px'><a href='/wadinfo?name={{{ urlencode($wad->wadname) }}}'>[View]</a></span></td>
						<td>{{{ $wad->size }}}</td>
						<td>{{{ $wad->username }}}</td>
						<td>{{{ $wad->date }}}</td>
						<td>{{{ $wad->md5 }}}</td>
					</tr>
				@endforeach
		</table>
	</div>

</div>

@include ('includes.footer')