<?php

include ('files.php');
include ('wads.php');

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the Closure to execute when that URI is requested.
|
*/

/**
 * Homepage
 */
Route::get('/', function()
{
	return View::make('home')->with('title', 'Home');
});

/**
 * 404 page
 */
App::missing(function($exception)
{
	return Response::view('error404', array(), 404);
});

/**
 * Banlist
 */
Route::get('/bans', function() {
	if (!empty($_GET['vpn']) && $_GET['vpn'] == 1)
		return View::make('bans')->with('title', 'Banlist')->with('bans', DB::table('banlist')->orderBy('ip', 'asc')->get());
	else
		return View::make('bans')->with('title', 'Banlist')->with('bans', DB::table('banlist')->where('reason', '!=', 'VPN')->orderBy('ip', 'asc')->get());
});

/**
 * Account page
 */
Route::get('/account', array('before' => 'auth', function() {
	return View::make('account')->with('title', 'Account')
		->with('slots', DB::table('save')->where('username', Auth::user()->username)->orderBy('id', 'asc')->get())
		->with('servers', DB::table('serverlog')->where('username', Auth::user()->username)->orderBy('id', 'desc')->limit(10)->get())
		->with('wads', DB::table('wads')->where('username', Auth::user()->username)->orderBy('id', 'desc')->get())
		->with('configs', DB::table('cfg')->where('username', Auth::user()->username)->orderBy('id', 'desc')->get());
}));

/**
 * Hosting Guide
 */
Route::get('/host', function() {
	return View::make('host')->with('title', 'Hosting Guide');
});

/**
 * RCON Guide
 */
Route::get('/rcon', function() {
	return View::make('rcon')->with('title', 'RCON Guide');
});

/**
 * Wad page
 */
Route::get('/wads', function() {
	$wads = DB::table('wads')->orderBy('id', 'desc')->get();
	foreach ($wads as $wad) {
		$wad->size = format_size($wad->size);
	}
	return View::make('wads')->with('title', 'Wads')->with('wads', $wads);
});

/**
 * Config page
 */
Route::get('/configs', function() {
	return View::make('configs')->with('title', 'Configs')->with('configs', DB::table('cfg')->orderBy('id', 'desc')->get());
});

/**
 * Log out page
 */
Route::get('/logout', function() {
	if (Auth::check()) {
		Auth::logout();
	}
	return Redirect::to('/');
});

/**
 * Log in page
 */
Route::get('/login', function() {
	return View::make('login')->with('title', 'Login');
});

/**
 * File upload page
 */
Route::get('/upload', function() {
	// Not logged in
	if (!Auth::check()) {
		return Redirect::to('/login')->withErrors(['You must log in to upload files.']);
	}
	$wads = DB::table('wads')->orderBy('id', 'desc')->limit(10)->get();
	foreach ($wads as $wad) {
		$wad->size = format_size($wad->size);
	}
	return View::make('upload')->with('title', 'Upload')->with('wads', $wads);
});

/**
 * File upload process script
 */
Route::post('/file-upload', array('before' => 'auth', function() {
	if (!empty($_FILES))
		return Response::json(process_uploaded_file($_FILES), 200);
	else
		return Response::json(array('status' => 'error', 'message' => 'You did not select a file.'));
}));

/**
 * Login process script
 */
Route::post('/login', function() {
	if (Auth::check() || Auth::attempt(array('username' => $_POST['username'], 'password' => $_POST['password']), true)) {
		return Redirect::to('/');
	}
	return Redirect::back()->withErrors(['Incorrect username/password']);
});

/**
 * Wad information page
 */
Route::get('/wadinfo', function() {
	if (Auth::check()) {
		if (empty($_GET['name'])) {
			return Redirect::to('/');
		}
		$db_wad = DB::table('wads')->where('wadname', $_GET['name'])->first();
		if ($db_wad == null) {
			return Redirect::to('/');
		}
		$wad = new WadInfo($db_wad->wadname);
		if (!empty($_GET['file'])) {
			foreach($wad->files as $file) {
				if (strtolower($file->filename) == strtolower($_GET['file'])) {
					return serve($file);
				}
			}
		}
		return View::make('wadinfo')->with('title', $wad->wadname)->with('db_wad', $db_wad)->with('wad', $wad);
	}
	else {
		return Redirect::to('/login')->withErrors(['You must be logged in to view wad information.']);
	}
});

/**
 * View configuration file
 */
Route::get('/viewconfig', function() {
	$settings = include('settings.php');
	if (Auth::check()) {
		if (empty($_GET['name'])) {
			return Redirect::to('/');
		}
		$db_config = DB::table('cfg')->where('cfgname', $_GET['name'])->first();
		if ($db_config == null) {
			return Redirect::to('/');
		}
		return View::make('readfile')
			->with('title', $db_config->cfgname)
			->with('text', file_get_contents($settings['cfg_directory'] . $db_config->cfgname));
	}
	else {
		return Redirect::to('/login')->withErrors(['You must be logged in to view wad information.']);
	}
});

/**
 * Serves a WAD download
 */
Route::get('/download', function() {
	$settings = include('settings.php');
	if (empty($_GET['file'])) {
		return Response::view('error404', array(), 404);
	}
	$wad = DB::table('wads')->where('wadname', $_GET['file'])->first();
	if ($wad == null) {
		return Response::view('error404', array(), 404);
	}
	$ip_address = Request::getClientIp();
	// Add the download to the database, if it's unique
	if (DB::table('wad_downloads')->where('ip', $ip_address)->where('name', $wad->wadname)->first() == null) {
		DB::table('wad_downloads')->insert(array(
			'wad_id' => $wad->id,
			'name' => $wad->wadname,
			'ip' => $_SERVER['REMOTE_ADDR'],
			'date' => new DateTime('NOW')
		));
	}
	return Response::download($settings['wad_directory'] . $wad->wadname);
});

/**
 * Wad Stats page
 */
Route::get('wadstats', function() {
	$total_size = DB::table('wads')->sum('size');
	$count = DB::table('wads')->count();
	$downloads = DB::table('wad_downloads')->count();
	return View::make('wadstats')->with('title', 'Wad stats')->with('size', format_size($total_size))->with('count', $count)->with('downloads', number_format($downloads));
});

/**
 * Banned wads page
 */
Route::get('bannedwads', function() {
	$bannedwads = DB::table('blacklist')->orderBy('name', 'asc')->get();
	return View::make('bannedwads')->with('title', 'Banned Wads')->with('bannedwads', $bannedwads);
});

/**
 * Serves a WAD download page given the wads in the URL
 */
Route::get('wadpage', function() {
	if (empty($_GET['key'])) {
		return Redirect::to('/');
	}
	$key = $_GET['key'];
	if (($wad_page = DB::table('wad_pages')->where('key', $key)->first()) == null) {
		return Response::view('error404', array(), 404);
	}
	return View::make('wadpage')->with('wads', explode(',', $wad_page->wad_string));
});

/**
 * Serves a file based on the filetype
 */
function serve($file) {
	switch(view_file($file->filename)) {
		case 'c':
			return View::make('readfile')->with('title', $file->filename)->with('filename', $file->filename)->with('text', $file->content)->with('language', 'c');
		case 'text':
			return View::make('readfile')->with('title', $file->filename)->with('filename', $file->filename)->with('text', $file->content);
		case 'png':
			return Response::make($file->content, '200', array('Content-Type' => "image/png;", 'Content-Disposition' => "inline; name=$file->filename"));
		case 'download':
			return View::make('readfile')->with('title', $file->filename)->with('filename', $file->filename)->with('text', $file->content);
		case 'cfg':
			return View::make('readconfig')->with('title', $file->filename)->with('filename', $file->filename)->with('text', $file->content);
		default:
			return Response::make($file->content, '200', array(
				'Content-Type' => 'application/octet-stream',
				'Content-Disposition' => "attachment; filename=$file->filename",
				'Content-Length' => "$file->content_size"
			));
	}
}

/**
 * File deletion script
 */
Route::get('/delete', array('before' => 'auth', function() {
	switch(check_upload_filetype(basename($_GET['name']))) {
		case 1:
			$file = DB::table('wads')->where('wadname', basename($_GET['name']))->first();
			$type = 'wad';
			break;
		case 2:
			$file = DB::table('cfg')->where('cfgname', basename($_GET['name']))->first();
			$type = 'cfg';
			break;
		case 3:
			$file = null;
			break;
	}
	if ($file == null) {
		return Response::json(array('status' => 'error', 'message' => 'That file does not exist.'));
	}
	else {
		return Response::json(delete_uploaded_file($file, $type));
	}

}));

//-------------------------------------//
//                API                  //
//-------------------------------------//

/**
 * Wad downloads
 */
Route::get('/api/wadstats', function() {
	switch ($_GET['date']) {
		case "all":
			return Response::json(DB::select(DB::raw("SELECT name, COUNT(name) as downloads FROM wad_downloads GROUP BY name ORDER BY downloads DESC LIMIT 50")), 200);
		case "year":
			return Response::json(DB::select(DB::raw("SELECT name, COUNT(name) as downloads FROM wad_downloads
				WHERE date > NOW() - INTERVAL 1 YEAR GROUP BY name ORDER BY downloads DESC LIMIT 50")), 200);
		case "month":
			return Response::json(DB::select(DB::raw("SELECT name, COUNT(name) as downloads FROM wad_downloads
				WHERE date > NOW() - INTERVAL 1 MONTH GROUP BY name ORDER BY downloads DESC LIMIT 50")), 200);
		case "week":
			return Response::json(DB::select(DB::raw("SELECT name, COUNT(name) as downloads FROM wad_downloads
				WHERE date > NOW() - INTERVAL 1 WEEK GROUP BY name ORDER BY downloads DESC LIMIT 50")), 200);
		case "day":
			return Response::json(DB::select(DB::raw("SELECT name, COUNT(name) as downloads FROM wad_downloads
				WHERE date > NOW() - INTERVAL 1 DAY GROUP BY name ORDER BY downloads DESC LIMIT 50")), 200);
	}
});

/**
 * Wad information
 */
Route::get('/api/wads', function() {
	return Response::json(DB::table('wads')->orderBy('id', 'desc')->get(), 200);
});

/**
 * Config information
 */
Route::get('/api/configs', function() {
	return Response::json(DB::table('cfg')->orderBy('id', 'desc')->get(), 200);
});

/**
 * Balist
 */
Route::get('/banlist', function() {
	$banlist = DB::table('banlist')->where('reason', 'vpn')->get();
	foreach ($banlist as $ban) {
		$bans[] = $ban->ip;
	}
	return Response::make(implode("\n", $bans), 200)->header('Content-type', 'text/plain');
});

function format_size($size)
{
	$size = max(0, (int)$size);
	$units = array( 'B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB');
	$power = $size > 0 ? floor(log($size, 1024)) : 0;
	return number_format($size / pow(1024, $power), 2, '.', ',') . $units[$power];
}