<?php

/**
 * This class is responsible for checking a file and determining which type of image it is
 */
class DoomImage {

	/**
	 * Holds image constants
	 */
	const IMAGE_NONE = 0x00;
	const IMAGE_BMP = 0x01;
	const IMAGE_LMP = 0x02;
	const IMAGE_PNG = 0x03;
	const IMAGE_JPG = 0x04;

	/**
	 * Constructor
	 */
	function __construct($bytes) {
		$this->bytes = $bytes;
		$this->type = check_image_type();
	}

	/**
	 * Run through our checks and determine what type of image the file is
	 * Supported types: PNG, JPEG, BMP and LMP
	 */
	function check_image_type() {
		if (check_bmp()) return IMAGE_BMP;
		if (check_png()) return IMAGE_PNG;
		if (check_jpg()) return IMAGE_JPG;
		if (check_lmp()) return IMAGE_LMP;
		return IMAGE_NONE;
	}

	/**
	 * Returns true if image matches a LMP
	 */
	function check_lmp() {

	}

	/**
	 * Returns true if image matches a BMP
	 */
	function check_bmp() {
		if ($this->bytes[0] == 0x42 || $this->bytes[1] ==0x4D) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns true if image matches a PNG
	 */
	function check_png() {
		if ($this->bytes[0] == 0x89 ||
			$this->bytes[1] == 0x50 ||
			$this->bytes[2] == 0x4E ||
			$this->bytes[3] == 0x47 ||
			$this->bytes[4] == 0x0D ||
			$this->bytes[0] == 0x0A ||
			$this->bytes[0] == 0x1A ||
			$this->bytes[0] == 0x0A
		) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Return true if an image matches a PNG
	 */
	function check_jpg() {
		if ($this->bytes[0] == 0xFF ||
			$this->bytes[0] == 0xD8 ||
			$this->bytes[count($this->bytes)-1] == 0xD9 ||
			$this->bytes[count($this->bytes)-2] == 0xFF
		) {
			return true;
		}
		else {
			return false;
		}
	}
}