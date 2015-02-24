@include ('includes.header')

<div class="main-content">
	<div class="heading">Configs</div>
	<div id="cfg-table" style="width: 90%;margin:20px auto 0 auto"></div>
	<script>
		$(document).ready(function() {
			var cfgs = [
			@foreach ($configs as $cfg)
				@if (Auth::check() && Auth::user()->level > 14)
				["<a style='margin-right:10px;cursor:pointer;' class='deletable'><span class='glyphicon glyphicon-remove'></span></a>" +
					"<a href='/viewconfig?name={{{ urlencode($cfg->cfgname) }}}'>{{{ $cfg->cfgname }}}</a>",
						@else
				["<a href='/viewconfig?name={{{ urlencode($cfg->cfgname) }}}'>{{{ $cfg->cfgname }}}</a>",
				@endif
				"{{{$cfg->username}}}",
				"{{{$cfg->date}}}"
			],
			@endforeach
			]
			$('#cfg-table').html( '<table class="table table-striped table-bordered" style="background:#EEE" id="cfgs"></table>' );
			$('#cfgs').dataTable({
				"oLanguage": {
					"oPaginate": {
						"sPrevious": "<span class='page-button'>Back</span>&nbsp;",
						"sNext": "<span class='page-button'>Next</span>"
					}
				},
				"bLengthChange": false,
				"iDisplayLength": 50,
				"aaData": cfgs,
				"aoColumns": [
					{ "sTitle": "Name" },
					{ "sTitle": "Uploader" },
					{ "sTitle": "Date" },
				]
			}).fnSort([[2, 'desc']]);
		});
	</script>
</div>


@include ('includes.footer')