<?php

include 'functions.php';
include 'doom/images/imagetype.php';

/**
 * Holds the wad information
 */
class WadInfo {

	/**
	 * Holds an array of all of the files in the wad
	 */
	public $files = array();

	/**
	 * Constructor
	 */
	function __construct($wadname) {
		$this->settings = include('settings.php');
		$this->wadname = strtolower($wadname);
		$this->type = $this->get_wad_type($this->wadname);
		switch ($this->type) {
			case 'wad':
				// Inefficient at the moment
				// $this->get_wad_info();
				break;
			case 'pk3':
				$this->get_pk3_info();
				break;
			case 'pk7':
				break;
			default:
				break;
		}
	}

	/**
	 * Gets the type of wad
	 */
	function get_wad_type() {
		if (ends_with($this->wadname, '.wad')) {
			return 'wad';
		}
		else if (ends_with($this->wadname, '.pk3')) {
			return 'pk3';
		}
		else if (ends_with($this->wadname, '.pk7')) {
			return 'pk7';
		}
		else {
			return 'unknown';
		}
	}

	/**
	 * Populate our pk3
	 */
	function get_pk3_info() {
		$this->wad = new stdClass();
		$this->pk3file = new ZipArchive();
		$this->pk3file->open($this->settings['wad_directory'] . $this->wadname);
		for($i = 0; $i < $this->pk3file->numFiles; $i++) {
			$file = $this->pk3file->statIndex($i);
			if (!ends_with($file['name'], '/')) {
				$this->files[] = new DoomFile($file['name'], $this->pk3file->getFromIndex($file['index']), $file['size'], $file['mtime']);
			}
		}
	}

	/**
	 * Populate our wad
	 */
	function get_wad_info() {
		$this->wad_bytes = file_get_contents($this->settings['wad_directory'] . $this->wadname);
		$this->lumps = array();
		$this->offset = 0;
		// Get our header information
		$this->parse_header();
		// Get each lump
		for ($i = 0; $i < $this->lump_size; $i++) {
			$this->parse_directory();
		}
	}

	/**
	 * Gets all the data from the header and advances the index
	 */
	function parse_header() {
		// Read in IWAD/PWAD
		$this->wad_type = $this->get_string(4);
		// Read in the number of lumps
		$this->lump_size = $this->get_int();
		// Advance our pointer to the next directory
		$this->offset = $this->get_int();
	}

	/**
	 * Gets all the data from the directory and advances the index to the next directory
	 */
	function parse_directory() {
		// Pointer to the lump's data
		$pointer = $this->get_int();
		// Size of the lump
		$size = $this->get_int();
		// Name of the lump
		$name = $this->get_string(8);
		// Holds the data of the lump
		$data = array();
		for ($i = 0; $i < $size; $i++) {
			$data[] = ord($this->wad_bytes[$pointer + $i]);
		}
		$string = implode(array_map("chr", $data));
		$this->files[] = new DoomFile(trim($name), $string, $size, null);
	}

	/**
	 * Extracts a string with a specified length
	 */
	function get_string($length) {
		$return_string = "";
		for ($i = 0; $i < $length; $i++) {
			$return_string .= chr($this->get_byte());
		}
		return $return_string;
	}

	/**
	 * Extracts a byte
	 */
	function get_byte() {
		$byte = $this->wad_bytes[$this->offset];
		$this->offset++;
		return ord($byte);
	}

	/**
	 * Extracts a little-endian 32 bit integer
	 */
	function get_int() {
		return unpack("V", pack("C*", $this->get_byte(), $this->get_byte(), $this->get_byte(), $this->get_byte()))[1];
	}

}

class DoomFile {

	/**
	 * Holds the filename
	 */
	public $filename;

	/**
	 * Holds a byte array of the content
	 */
	public $content;

	/**
	 * Holds the content size
	 */
	public $content_size;

	/**
	 * Holds the last modified date
	 * NOTE: since .wad files do not store this information,
	 * $last_modified will be null
	 */
	public $last_modified;

	/**
	 * Constructors
	 */
	function __construct($filename, $content, $content_size, $last_modified) {
		$this->filename = $filename;
		$this->content = $content;
		$this->content_size = $content_size;
		$this->last_modified = $last_modified;
	}

}