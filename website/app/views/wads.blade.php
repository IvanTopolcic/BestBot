@include ('includes.header')

<div class="main-content">
	<div class="heading">Wads</div>
	<div id="wad-table" style="width: 90%;margin:20px auto 0 auto"></div>
	<script>
		$(document).ready(function() {
			var wads = [
			@foreach ($wads as $wad)
					@if (Auth::check() && Auth::user()->level > 14)
					["<a style='margin-right:10px;cursor:pointer;' class='deletable'><span class='glyphicon glyphicon-remove' id='{{{ $wad->wadname }}}'></span></a>" +
						"<a href='/download?file={{{ urlencode($wad->wadname) }}}'>{{{ $wad->wadname }}}</a><span style='margin-left:3px;font-size: 8px'>" +
						"<a href='/wadinfo?name={{{ urlencode($wad->wadname) }}}'>[View]</a></span>",
					@else
						["<a href='/download?file={{{ urlencode($wad->wadname) }}}'>{{{ $wad->wadname }}}</a><span style='margin-left:3px;font-size: 8px'>" +
							"<a href='/wadinfo?name={{{ urlencode($wad->wadname) }}}'>[View]</a></span>",
					@endif
						"{{{$wad->size}}}",
						"{{{$wad->username}}}",
						"{{{$wad->date}}}",
						"{{{$wad->md5}}}"
					],
			@endforeach
			]
			$('#wad-table').html( '<table class="table table-striped table-bordered" style="background:#EEE" id="wads"></table>' );
			$('#wads').dataTable({
				"oLanguage": {
					"oPaginate": {
						"sPrevious": "<span class='page-button'>Back</span>&nbsp;",
						"sNext": "<span class='page-button'>Next</span>"
					}
				},
				"bLengthChange": false,
				"iDisplayLength": 50,
				"aaData": wads,
				"aoColumns": [
					{ "sTitle": "Name" },
					{ "sTitle": "Size" },
					{ "sTitle": "Uploader" },
					{ "sTitle": "Date" },
					{ "sTitle": "MD5" }
				]
			}).fnSort([[3, 'desc']]);
		});
	</script>
</div>


@include ('includes.footer')