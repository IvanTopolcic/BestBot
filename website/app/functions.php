<?php

/**
 * This class holds useful functions for our project
 */

/**
 * Checks if a string starts with another string
 */
function startsWith($haystack, $needle) {
	return $needle === "" || strpos($haystack, $needle) === 0;
}

/**
 * Checks if a string ends with another string
 */
function endsWith($haystack, $needle) {
	return $needle === "" || substr($haystack, -strlen($needle)) === $needle;
}

/**
 * Checks to see if we can view any of the files
 */
function view_file($filename) {
	$ext = pathinfo(strtolower($filename), PATHINFO_EXTENSION);
	switch ($ext) {
		case 'c':
			return 'c';
		case 'txt':
		case 'dec':
		case 'h':
		case 'acs':
			return 'text';
		case 'png':
			return 'png';
		default:
			return 'download';
	}
}