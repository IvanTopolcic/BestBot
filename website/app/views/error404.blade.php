<!doctype html>
<html>
<head>
	{{ HTML::style('/static/css/bootstrap.min.css'); }}
	{{ HTML::style('/static/css/jquery.dataTables.css'); }}
	{{ HTML::style('/static/css/main.css'); }}
	{{ HTML::script('/static/js/jquery-1.11.0.min.js'); }}
	{{ HTML::script('/static/js/bootstrap.min.js'); }}
	{{ HTML::script('/static/js/jquery.dataTables.min.js'); }}
	{{ HTML::script('/static/js/jquery.form.min.js'); }}
	{{ HTML::script('/static/js/bootbox.min.js'); }}
	{{ HTML::script('/static/js/main.js'); }}
	<link href='http://fonts.googleapis.com/css?family=Noto+Sans' rel='stylesheet' type='text/css'>
	<link href='http://fonts.googleapis.com/css?family=Source+Code+Pro' rel='stylesheet' type='text/css'>
	<title>404 - Best Ever</title>
</head>
<body>
<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/">BE</a>
		</div>
		<div class="collapse navbar-collapse" id="navbar-collapse">
			<ul class="nav navbar-nav">
				<li><a href="/wads">Wads</a></li>
				<li><a href="/configs">Configs</a></li>
				<li><a href="/upload">Upload</a></li>
				<li><a href="/host">Hosting</a></li>
				<li><a href="/wadstats">Stats</a></li>
				<li><a href="/rcon">RCON</a></li>
				<li><a href="/bans">Bans</a></li>
				<li><a href="/bannedwads">Banned Wads</a></li>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				@if (isset(Auth::user()->username))
				<li><a href="/account">{{{ Auth::user()->username }}}</a></li>
				<li><a href="/logout">Log out</a></li>
				@else
				<li><a href="/login"><span style="margin-right: 5px" class="glyphicon glyphicon-lock"></span>Log in</a>
					@endif
			</ul>
		</div>
	</div>
</nav>
<div class="main">
	<div class="percent" style="margin-top: 40px;font-size:128px;">
		404
	</div>
	<div class="text-center" style="font-size:36px;margin-bottom:90px;">The content you are looking for was not found.</div>
	<footer id="footer">
		<div class="wrap">
			<p class="footer-text">© 2014 Best Ever<br /><a target="_BLANK" href="https://www.oblivionro.net">Powered by OblivionRO</a></p>
		</div>
	</footer>
</div>
</body>
</html>