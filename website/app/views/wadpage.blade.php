<a href="/download?file=newtextcolours1_170.pk3">newtextcolours1_170.pk3</a>
<a href="/download?file=skulltag_actors_1-1-1.pk3">skulltag_actors_1-1-1.pk3</a>
<a href="/download?file=skulltag_data_126.pk3">skulltag_data_126.pk3</a>
@foreach ($wads as $wad)
	<a href="/download?file={{{ urlencode($wad) }}}">{{{ $wad }}}</a>
@endforeach