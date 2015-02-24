<?php

/**
 * Checks the extension of the uploaded file
 * Returns 1 if a wad file, 2 if a config, or 0 if none
 */
function check_upload_filetype($filename) {
	$parts = pathinfo($filename);
	switch (strtolower($parts['extension'])) {
		case "wad":
		case "pk3":
		case "pk7":
			return 1;
		case "cfg":
			return 2;
		default:
			return 0;
	}
}

/**
 * Verifies an uploaded wad
 */
function verify_wad($upload) {
	$settings = include('settings.php');
	// Make sure they actually provided a file
	if (empty($upload)) {
		return array('status' => 'error', 'message' => 'You did not select a file.');
	}
	// Check if they're using a copyrighted iwad
	foreach ($settings['bad_iwads'] as $iwad) {
		if (strtolower($iwad) === $upload['file']['name']) {
			return array('status' => 'error', 'message' => 'Can\'t upload that :(');
		}
	}
	// Check if the wad already exists (soft DB check)
	if (DB::table('wads')->where('wadname', $upload['file']['name'])->get() != null) {
		return array('status' => 'error', 'message' => 'File already exists.');
	}
	// Make sure our filesize limit is enforced
	if (filesize($upload['file']['tmp_name']) > $settings['max_upload_size']) {
		return array('status' => 'error', 'message' => 'File exceeds ' . $settings['max_upload_size'] . ' bytes');
	}
	// Check to see if the filename is too long
	if (strlen($upload['file']['name']) > $settings['max_filename_length']) {
		return array('status' => 'error', 'message' => 'Filename must be below ' . $settings['max_filename_length'] . ' characters.');
	}
	// Everything looks good, attempt to move the file
	if (!move_uploaded_file($upload['file']['tmp_name'], $settings['wad_directory'] . strtolower($upload['file']['name']))) {
		return array('status' => 'error', 'message' => 'Looks like something went wrong! Please report this error.');
	}
	// Good to go!
	else {
		DB::table('wads')->insert(array(
			'wadname' => $upload['file']['name'],
			'username' => Auth::user()->username,
			'ip' => $_SERVER['REMOTE_ADDR'],
			'date' => new DateTime('NOW'),
			'size' => filesize($settings['wad_directory'] . $upload['file']['name']),
			'md5' => md5_file($settings['wad_directory'] . $upload['file']['name'])));
		return array('status' => 'success', 'message' => 'File uploaded.');
	}
}

/**
 * Verifies an uploaded config
 */
function verify_cfg($upload) {
	$settings = include('settings.php');
	// Make sure they actually provided a file
	if (empty($upload)) {
		return array('status' => 'error', 'message' => 'You did not select a file.');
	}
	// Check if the wad already exists (soft DB check)
	if (DB::table('cfg')->where('cfgname', $upload['file']['name'])->get() != null) {
		return array('status' => 'error', 'message' => 'File already exists.');
	}
	// Make sure our filesize limit is enforced
	if (filesize($upload['file']['tmp_name']) > $settings['max_upload_size']) {
		return array('status' => 'error', 'message' => 'File exceeds ' . $settings['max_upload_size'] . ' bytes');
	}
	// Check to see if the filename is too long
	if (strlen($upload['file']['name']) > $settings['max_filename_length']) {
		return array('status' => 'error', 'message' => 'Filename must be below ' . $settings['max_filename_length'] . ' characters.');
	}
	// Check the file for bad variables
	$cfg_status = check_bad_commands($upload['file']['tmp_name']);
	if ($cfg_status['status'] == 'error') {
		return $cfg_status;
	}
	// Everything looks good, attempt to move the file
	if (!move_uploaded_file($upload['file']['tmp_name'], $settings['cfg_directory'] . strtolower($upload['file']['name']))) {
		return array('status' => 'error', 'message' => $upload['file']['error']);
	}
	// Good to go!
	else {
		DB::table('cfg')->insert(array(
			'cfgname' => $upload['file']['name'],
			'username' => Auth::user()->username,
			'ip' => $_SERVER['REMOTE_ADDR'],
			'date' => new DateTime('NOW')
		));
		return array('status' => 'success', 'message' => 'File uploaded.');
	}
}

/**
 * Checks the file, if it passes all checks, moves it to the upload
 * directory and adds it to the database
 */
function process_uploaded_file($upload) {
	$upload['file']['name'] = strtolower($upload['file']['name']);
	switch(check_upload_filetype($upload['file']['name'])) {
		case 0:
			return array('status' => 'error', 'message' => 'That filetype isn\'t supported.');
		case 1:
			return verify_wad($upload);
		case 2:
			return verify_cfg($upload);
		default:
			return array('status' => 'error', 'message' => 'Encountered unknown error. Please report this.');
	}
}

/**
 * Deletes a wad/pk3/pk7 or cfg file
 */
function delete_uploaded_file($file, $type) {
	$settings = include('settings.php');
	if (Auth::user()->level > 14 || Auth::user()->username == $file->username) {
		switch($type) {
			case 'wad':
				if (file_exists($settings['wad_directory'] . $file->wadname)) {
					DB::table('wads')->where('wadname', $file->wadname)->delete();
					unlink($settings['wad_directory'] . $file->wadname);
					return array('status' => 'success', 'message' => 'File was successfully deleted.');
				}
				return array('status' => 'error', 'message' => 'There was an error deleting the file. Please report this.');
			case 'cfg':
				if (file_exists($settings['cfg_directory'] . $file->cfgname)) {
					DB::table('cfg')->where('cfgname', $file->cfgname)->delete();
					unlink($settings['cfg_directory'] . $file->cfgname);
					return array('status' => 'success', 'message' => 'File was successfully deleted.');
				}
				return array('status' => 'error', 'message' => 'There was an error deleting the file. Please report this.');
		}
	}
	else {
		return array('status' => 'error', 'message' => 'You do not have permission to delete this file.');
	}
}

/**
 * Checks a config file for invalid commands
 */
function check_bad_commands($filepath) {
	$settings = include('settings.php');
	$file = fopen($filepath, 'r');
	if (!$file) {
		return array('status' => 'error', 'message' => 'Error validating config. Please report this.');
	}
	while(!feof($file)) {
		$line = fgets($file);
		foreach ($settings['bad_commands'] as $bad_command) {
			$file_array[] = $line;
			if (strpos($line, $bad_command) !== false) {
				return array ('status' => 'error', 'message' => 'Bad variable in ' . $line);
			}
		}
	}
	return array('status' => 'success');
}