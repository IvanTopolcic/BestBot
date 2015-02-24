<?php
/* 7z.inc
 * PHP Class to interface with p7zip Version 4.44+
 *    (file content listing + plain-text extraction only)
 *
 * p7z Public functions:
 *    p7z()             - constructor
 *    del_dir()         - returns TRUE/FALSE; Empty + (default) remove supplied dir (+ all sub-directories)
 *    getComment()      - returns $_comment                (archive-wide comment)
 *    getList()         - returns $this->lists array/FALSE (not a supported archive)
 *    getHostOS()       - returns $_host_os                ('' == Host OS vary, or absent (?))
 *    getNumDirs()      - returns $num_dirs
 *    getNumFiles()     - returns $num_files               (files--not directories--in listing)
 *    getSize()         - returns $size                    (total un-compressed size of files)
 *    getSizePacked()   - returns $size_packed             (total compressed size of files)
 *    is7zError()       - returns $_7z_error/FALSE
 *    isAllEncrypted()  - returns TRUE/FALSE               (all files are encrypted)
 *    isArchive()       - returns TRUE/FALSE
 *    isAttributed()    - returns TRUE/FALSE               (any file is attributed)
 *    isCharsetAdded()  - returns TRUE/FALSE               (any file has a charset)
 *    isCommented()     - returns TRUE/FALSE               (at least one file is commented)
 *    isEncrypted()     - returns TRUE/FALSE               (at least one file is encrypted)
 *    isError()         - returns TRUE/FALSE
 *    isExecError()     - returns $_exec_error/FALSE
 *    isMimeAdded()     - returns TRUE/FALSE               (`Content-Type' values added to $lists)
 *    isRecursion()     - returns TRUE/FALSE               ((at least one) `archive' value added to $lists)
 *    isTextAdded()     - returns TRUE/FALSE               (`plain/text', etc `Content-Type' values added to $lists)
 *
 * p7z Private functions:
 *    _list_7z()        - returns TRUE/FALSE; Use p7zip to list files + directories within $archive
 *    _list_from_dir()  - returns TRUE/FALSE; Use unshield/unstuff to list files + dirs within InstallShield/Mac $archive
 *    _list_rpm()       - returns TRUE/FALSE; Use RPM to list files + directories within .rpm $archive
 *    _ret_bytes()      - returns (int) bytes equivalent for supplied string/int parameter
 *
 *    See end of Class for help + advice, changelog, etc.
 *
 *    TAB=3
 *
 * @category File
 * @version $Id: 7z.include, v0.13.2 2011-10-30
 * @author Alex KEMP http://www.modem-help.co.uk/
 */
 	class p7z {
/*   If $archive is:
 *      1 a valid file accessible through Apache (no test), and
 *      2 a 7z-supported archive
 *
 *   then the Constructor will produce a list of file + directories within $this->lists
 *+  with full `technical' information for each file/dir.
 *
 *   see also comments at bottom (`Key names for $this->lists').
 */
		var $archive			= '';			// array; full server path + filename to archive
		var $lists				= array();	// array; archive listing with tech info on each file/dir
		var $num_dirs			= 0;			// int;   number of directories
		var $num_files			= 0;			// int;   number of files (not including directories)
		var $size				= 0;			// int;   (un-compressed) size of all files
		var $size_packed		= 0;			// int;   (compressed) size of all files
		// private variables
		var $_7z_error			= '';			// string; return error from 7z
		var $_attributed		= FALSE;		// bool;   at least one file has attributes
		var $_charset_added	= FALSE;		// bool;   `Charset' value added to $lists
		var $_comment			= '';			// string; single comment (empty means they differ)
		var $_commented		= FALSE;		// bool;   at least one file has a comment
		var $_encrypt_all		= FALSE;		// bool;   all files are encrypted
		var $_encrypted		= FALSE;		// bool;   at least one file is encrypted
		var $_exec_error		= '';			// string; error when exec() attempted
		var $_host_os			= '';			// string; single Host OS (empty means they differ)
		var $_mime_added		= FALSE;		// bool;   `Content-Type' value added to $lists
		var $_recurse			= FALSE;		// bool;   an archive added to $lists
		var $_temp_dir			= '/home/htdocs/hosted/modem-help.com/temp';
													// string; full server path for /temp (no end `/')
		var $_text_added		= FALSE;		// bool;   `text' value added to $lists
/*
 * Constructor
 *
 * @param string $archive         (required) server path to (possibly) archive file
 * @param bool   $add_mime        (def FALSE) flag to add `Content-Type' values to $lists
 * @param bool   $add_text        (def FALSE) flag to add `text' content + `charset' value to $lists
 * @param string $default_charset (def '') default value for `Charset' when `Content-Type' == `text/...'
 * @param bool   $recurse         (def FALSE) flag to add archives-within-$archive to this Class
 * @param bool   $isIS            (def FALSE) flag to signal is/not an InstallShield collection
 * @param bool   $tryUnstuff      (def FALSE) flag to try/not try `unstuff' if 7z fails
 * @access public
 */
		function p7z(
			$archive,						// full server path + filename
			$add_mime			= FALSE,	// add MIME Content-Types + Charset (plain text only) to $lists
												// (will increase exec time a lot)
			$add_text			= FALSE,	// add extracted plain/text files to the Class
			$default_charset	= '',		// default Charset when unknown (eg 'us-ascii', 'iso-8859-1')
												// FALSE => put '' when unknown
			$recurse				= FALSE,	// add extracted archive files to the Class
			$isIS					= FALSE,	// archive is an InstallShield collection
												// note: `$archive' needs to be `data1.hdr',
												// but all other IS cabs also need to be within same directory
			$tryUnstuff			= FALSE	// test for Apple Mac archive (needs `unstuff' on server)
		) {
/*			!!!IMPORTANT!!! You need to be very certain before running this Class that
 *+      `$archive' contains a path+filename to a server-file **and nothing else**.
 *
 *	      Beware of user-input to this param, else your machine can be owned.
 *
 *       2009-09-24 unstuff routine added for Apple Mac files (7-Zip cannot open any such files).
 *+                 Can open: sit, cpt, zip, arc, arj, lha, rar, gz, compress, uu, hqx, btoa, mime,
 *+                           tar, bin, sitseg, sitsegN, pf, sit5, bz2, apple
 *+                 Requires `unstuff' on server.
 *       2007-10-10 Separate InstallShield-unpack routine introduced; requires `unshield' on server
 *+                 Listing command in `unshield' is buggy; therefore obtain all info from files extracted
 *       2007-04-23 Separate RPM routine introduced
 *       7z can extract RPMs, but the list command does not contain a `Path'
 *+      Update: v4.44: RPM >> cpio >> files (2-stage, thus no file listing from RPM).
 */
			$output_dir	= '';

			// attempt to get a file listing from an archive as first act
			// 7-Zip has quirks/glitches/problems with certain file collections:
			//   1 open/list RPMs is a 2-stage process; we bottle out & use RPM itself
			//   2 cannot list nor open InstallShield file collections
			//   3 will always fail with Apple Mac archives
 			$is_rpm		= ( strtolower( substr( $archive, -4 )) == '.rpm');
			if( !$isIS ) {
 				if( $is_rpm )	{ $this->_list_rpm( $archive );	$tryUnstuff	= FALSE; }
	 			else				if( $this->_list_7z( $archive ))	$tryUnstuff	= FALSE;

				if( $this->isError() and ( !$tryUnstuff )) return;
			}
/*			2009-09-24 $isIS + $tryUnstuff each require file extraction to obtain file listing. In
 *+                 classic lazy programming style, $add_mime turned on to guarantee that.
 *			2007-04-28 bugfix: no point in `Content-Type', `plain/text' nor `Charset' if files encrypted;
 *                  routine also changed slightly so that $add_text or $default_charset or $recurse
 *+                 == true sets earlier parameters to TRUE (depend on each other).
 *                  Routine below designed to save exec time.
 */
 			if( $add_mime or $add_text or $default_charset or $recurse or $isIS or $tryUnstuff ) {
				if( !empty( $this->lists['Encrypted'])) {
					if( array_search( FALSE, $this->lists['Encrypted']) === FALSE ) {
						// all entries are encrypted
						$add_mime	= $add_text = $default_charset = $recurse = $isIS = $tryUnstuff = FALSE;
					}
				}
				if( $default_charset or $add_text or $recurse or $isIS or $tryUnstuff )	$add_mime	= TRUE;
 			}

			// not every archive will give rise to every key, so we may need to create them
			// we assume just `Path'
			// if $isIS==TRUE, or $tryUnstuff==TRUE, no listing yet
			if(
				empty( $this->lists['Path']) and
				( !( $tryUnstuff or $isIS ))
			) {
				// duck out
				$this->archive		= '';
				$this->lists		= array();
				$this->_7z_error	.= '<br />p7z::p7z error: No Path';
				return;
			} else {
				// 2007-09-14 bugfix for 'Out of memory' fatal errors on vast archives-within-Archive
				$originalMem	= // intentionally left empty!
				$currentMem		= $this->_ret_bytes( ini_get('memory_limit'));
				if( $add_mime ) {
					// obtain `Content-Type's for each file in $archive - will extend Class exec time a lot
					// 2009-09-24 bugfix: vvv occasional clashes, so rand() added
					$tmp						= uniqid( rand());				// unique dir to prevent threads interfering
					$stuff_archive			= "_$tmp";							// for use with unstuff
					$stuff_archive_dir	= "_$stuff_archive";				// for use with unstuff
					$output_dir				= "$this->_temp_dir/$stuff_archive_dir";
					$oldSetting				= ignore_user_abort( TRUE );	// don't want files hanging around

/*					2009-09-24 InstallShield & Apple Mac archives each require file extraction to obtain file listing.
 *					2007-04-24 bugfix: Separate RPM routine introduced - requires `rpm2cpio' on server
 *					7z will only output a single file from the RPM.
 *					Note that the RPM command will fail under safe_mode due to presence of a pipe (`|')
 */
		 			if( $is_rpm or $isIS or $tryUnstuff ) {
						$oldMask	= umask( 0 );	// prevent umask value interfering
						if( !mkdir( $output_dir )) {
							$this->archive		= '';
							$this->lists		= array();
							$this->_7z_error	.= '<br />p7z::p7z error: mkdir() failed';
							ignore_user_abort( $oldSetting );		// don't mind user-abort now
							return;
						}
						umask( $oldMask );
		 				chdir( $output_dir );
			 			if( $is_rpm ) {
							$command	= '/usr/bin/rpm2cpio < '. escapeshellarg( $archive ) .' | cpio -i --make-directories';
						} elseif( $isIS ) {
							$command	= '/usr/bin/unshield -d '. escapeshellarg( $output_dir ) .' x '. escapeshellarg( $archive );
						} else {
							// unstuff path-handling is exceptionally poor - all relevant files & dirs must be
							// at the same level or below $CWD. Plus `m' option does not work. This is all Linux only.
							chdir( $this->_temp_dir );
							symlink( $archive, $stuff_archive );
							$command	= '/usr/bin/unstuff -q -d='. escapeshellarg( $stuff_archive_dir ) .' '. escapeshellarg( $stuff_archive );
						}
		 			} else {
						$command	= '/usr/local/bin/7z x -bd -y -o'. escapeshellarg( $output_dir ) .' '. escapeshellarg( $archive );
		 			}

					$_error					= @exec( $command, $tmp, $_exec_error );
					if( $_exec_error ) {
						$this->_exec_error	.= "<br />p7z::p7z error on `$command': Exec errorcode: `$_exec_error'; return value: `$_error'.<br />\n";
						// 2009-09-28 bugfixes: need to clean up after the error
						chdir( $this->_temp_dir );
						if( file_exists( $stuff_archive )) {
							if( unlink( $stuff_archive ) === FALSE ) {
								$this->_7z_error	.= "<br />p7z::p7z error: cannot delete symbolic link `$this->_temp_dir/$stuff_archive_dir'.<br />\n";
							}
						}
						if( file_exists( $output_dir )) {
							if( !$this->del_dir( $output_dir )) {	// remove the tmp files
								$this->_7z_error	.= "<br />p7z::p7z error: unable to remove tmp dir `$output_dir'.<br />";
							}
						}
						if( !empty( $this->lists )) {
							// 7-Zip v4.65 can list .dmg files, but then fail on extraction (Mac only?)
							$this->lists				= array();
							$this->num_dirs			= // intentionally left empty!
							$this->num_files			= // intentionally left empty!
							$this->size					= // intentionally left empty!
							$this->size_packed		= 0;
							$this->_attributed		= // intentionally left empty!
							$this->_charset_added	= // intentionally left empty!
							$this->_commented			= // intentionally left empty!
							$this->_encrypt_all		= // intentionally left empty!
							$this->_encrypted			= // intentionally left empty!
							$this->_mime_added		= // intentionally left empty!
							$this->_recurse			= // intentionally left empty!
							$this->_text_added		= FALSE;
							$this->_comment			= // intentionally left empty!
							$this->_host_os			= '';
						}
						ignore_user_abort( $oldSetting );			// don't mind user-abort now
						return FALSE;
					}	// if( $_exec_error )

		 			if( $is_rpm ) {
		 				chdir( $this->_temp_dir );						// avoid poss later problem with del_dir()?
		 			} else if( $isIS or $tryUnstuff ) {				// unshield list-files command is too buggy
		 				chdir( $output_dir );							// unstuff list-files command does not exist
		 				if(( !$this->_list_from_dir()) or empty( $this->lists['Path'])) {
		 					// _list_from_dir() returned FALSE (error)
							// duck out
							chdir( $this->_temp_dir );
							$this->archive		= '';
							$this->lists		= array();
							$this->_7z_error	.= '<br />p7z::p7z error whilst listing InstallShield or Stuffit files.<br />';
							if( !$this->del_dir( $output_dir )) {	// remove the tmp files
								$this->_7z_error	.= "<br />p7z::p7z error: unable to remove tmp dir `$output_dir'.<br />";
							}
							if( $tryUnstuff )	unlink( $stuff_archive );
							ignore_user_abort( $oldSetting );		// don't mind user-abort now
							return;
		 				} else {
							$this->archive		= $archive;
							chdir( $this->_temp_dir );
							if( $tryUnstuff )	unlink( $stuff_archive );
		 				}
		 			}	// if( $is_rpm ) else if( $isIS or $tryUnstuff )
				}	// if( $add_mime )

				// apply default values for missing $lists keys
				$noAttributes	= empty( $this->lists['Attributes']);
				$noComment		= empty( $this->lists['Comment']);
				$noCRC			= empty( $this->lists['CRC']);
				$noEncrypted	= empty( $this->lists['Encrypted']);
				$noFolder		= empty( $this->lists['Folder']);
				$noHost_OS		= empty( $this->lists['Host OS']);
				$noMethod		= empty( $this->lists['Method']);
				$noModified		= empty( $this->lists['Modified']);
				$noPackedSize	= empty( $this->lists['Packed Size']);
				$noSize			= empty( $this->lists['Size']);
				foreach( $this->lists['Path'] as $i => $path ) {
/*					2011-10-31 bugfix: take account of possible undefined offset(s)
 *					2009-09-21 change test for $noSize to $noSize+$noPackedSize
 *					         + (some .exe archives have empty Size for files)
 *					2008-03-07 take account of empty `$path' (reduce PHP Notice in error log).
 *					2007-05-04 bugfix: Not every archive will contain `Folder' (7z as the obvious example!),
 *+								but it makes for easy programming to have an entry that flags that fact.
 *					Programming below therefore modified to detect directory entries.
 */
					if(
						$noFolder and
						(
							( !$noAttributes ) or
							(
								( !$noSize ) and ( !$noPackedSize )
							)
						)
					) {
						// no `Folder' in $lists, and we may have the means to add it
						// (some offsets may be empty)
						if(
							(
								( isset( $this->lists['Attributes'][ $i ])) and
								( strtolower( $this->lists['Attributes'][ $i ][ 0 ]) == 'd')
							) or (
								( isset( $this->lists['Size'][ $i ]) and ( $this->lists['Size'][ $i ] < 1 )) and
								( isset( $this->lists['Packed Size'][ $i ]) and (  $this->lists['Packed Size'][ $i ] < 1 ))
							)
						) {
							// this is a directory
							// also need to change relevant `Directory' + `Filename' entries
							$this->lists['Directory'][ $i ]	= (( $this->lists['Directory'][ $i ])
								? $this->lists['Directory'][ $i ] .'/'
								: ''
								) . $this->lists['Filename'][ $i ];
							$this->lists['Filename'][ $i ]	= '';
							$this->lists['Folder'][ $i ]		= TRUE;
						}
					}	// if( $noFolder and ...)

					if(( $noAttributes ) or ( !isset( $this->lists['Attributes'][ $i ])))	$this->lists['Attributes'][ $i ]		= '';
					if(( $noComment ) or ( !isset( $this->lists['Comment'][ $i ])))			$this->lists['Comment'][ $i ]			= '';
					if(( $noCRC ) or ( !isset( $this->lists['CRC'][ $i ])))						$this->lists['CRC'][ $i ]				= '';
					if(( $noEncrypted ) or ( !isset( $this->lists['Encrypted'][ $i ])))		$this->lists['Encrypted'][ $i ]		= FALSE;
					if(( $noHost_OS ) or ( !isset( $this->lists['Host OS'][ $i ])))			$this->lists['Host OS'][ $i ]			= '';
					if(( $noMethod ) or ( !isset( $this->lists['Host OS'][ $i ])))				$this->lists['Method'][ $i ]			= '';
					if(( $noModified ) or ( !isset( $this->lists['Method'][ $i ])))			$this->lists['Modified'][ $i ]		= '';
					if(( $noPackedSize ) or ( !isset( $this->lists['Packed Size'][ $i ])))	$this->lists['Packed Size'][ $i ]	= 0;
					if(( $noSize ) or ( !isset( $this->lists['Size'][ $i ])))					$this->lists['Size'][ $i ]				= 0;
					$this->lists['Content-Type'][ $i ]	= // intentionally left empty!
					$this->lists['Charset'][ $i ]			= // intentionally left empty!
					$this->lists['text'][ $i ]				= // intentionally left empty!
					$this->lists['archive'][ $i ]			= // intentionally left empty!
					$this->lists['md5'][ $i ]				= '';
					if( $add_mime ) {
						ini_set('auto_detect_line_endings', TRUE );		// important for Mac line-end files
						if(( empty( $path )) or ( $path[ 0 ] != '/'))	$path	= "/$path";
						$filename	= "$output_dir$path";
						// try to avoid extra disk-access
						if(( $noFolder == FALSE ) and isset( $this->lists['Folder'][ $i ])) {
							$is_a_dir	= $this->lists['Folder'][ $i ];
						} else {
							$is_a_dir	= is_dir( $filename );	// well, we tried
						}
/*						2009-09-21 added tests re: Folder + Size due to some .exe archives have empty Size for files
 *						2007-09-08 `archive' + `md5' added, to allow recursion into archives stored within archives.
 *						2007-04-28 `Charset' added. Also need to take account of encrypted files in archive
 *+									(makes nonsense of `Content-Type', `text', `Charset', `archive' or `md5')
 */
						if( $is_a_dir or $this->lists['Encrypted'][ $i ]) {
							if( !isset( $this->lists['Folder'][ $i ])) {
								$this->lists['Folder'][ $i ]	= ( $is_a_dir )
									? TRUE
									: FALSE;
							}
							if( $is_a_dir ) {
								// belt 'n' braces
								$this->lists['Packed Size'][ $i ]	= // intentionally left empty!
								$this->lists['Size'][ $i ]				= 0;
							}
						} else {
/*							2008-03-14 bugfix: account for read-only files (tsk, tsk)
 *							2008-01-16 attempt to correctly type `.inf' + `.txt' files with trailing binary char(s).
 *							2007-10-09 special routine added for 'text/rtf'; requires `unrtf' on server
 *							2007-10-01 ['text/plain'] changed to ['text']
 *							2007-04-24 mime_content_type() is much too buggy, so switched to server `file'.
 *+									Exec time increases again (13-file archive increased by .12 secs).
 *							Command structure obtained at php.net/manual/en/function.mime-content-type.php,
 *+						tree2054 using hotmail, 04-Nov-2006 01:59.
 */
							if( !isset( $this->lists['Folder'][ $i ])) {
								$this->lists['Folder'][ $i ]	= FALSE;
							}
							if( $this->lists['Size'][ $i ] < 1 ) {
								$this->lists['Size'][ $i ]		= filesize( $filename );
							}

							$this->_mime_added						= TRUE;
							$this->lists['Content-Type'][ $i ]	= $type = trim( @exec('file -bi '. escapeshellarg( $filename )));

							// false non-text typing for `.inf' + .txt files?
							$tmp											= strtolower( substr( $filename, -4 ));
							if( $add_text and ( $type == 'application/octet-stream') and (( $tmp == '.inf') or ( $tmp == '.txt'))) {
								if( $tmp = file( $filename )) {
									$j					= count( $tmp );
									$tmp[ $j - 1 ]	= ";[Modem-Help edit to correctly report 'Content-Type'; see http://forums.modem-help.co.uk/viewtopic.php?t=707&postdays=0&postorder=asc&start=15]";
									$tmp				= implode('', $tmp );
									// code-changes to allow for read-only files
									if( !$fp = @fopen( $filename, 'w')) {
										// probably readonly in archive; abandon attempt on failure
										if( !chmod( $filename, 0666 ))			continue;
										if( !$fp = @fopen( $filename, 'w'))		continue;
									}
									fputs( $fp, $tmp );
									fclose( $fp );
									$tmp = trim( @exec('file -bi '. escapeshellarg( $filename )));
									if( strpos( $tmp, 'text') === 0 ) {
										$this->lists['Content-Type'][ $i ]	= $type = $tmp;
									}
								}
							}

							if( strpos( $type, 'text') === 0 ) {
								// locate text file encoding
								if((( $char = explode(';', $type )) !== $type ) and	// check for no encoding
									( !empty( $char[ 1 ])) and									// yes, it happened
									(( $char_ = explode('=', $char[ 1 ])) !== $char[ 1 ]) and
									( !empty( $char_[ 1 ])) and
									(( $charset = strtolower( trim( $char_[ 1 ]))) != 'unknown')
								) {
									$this->_charset_added					= TRUE;
									$this->lists['Charset'][ $i ]			= $charset;
								} elseif( strpos( $type, 'text/html') === 0 ) {
									// try to find the charset inside the HEAD
									$charset										= 'us-ascii';	// most likely default
									if(( $str = file_get_contents( $filename )) != FALSE ) {
										if(
											( preg_match('@<head.+head>@si', $str, $tmp ) > 0 ) and	// capture text within <head>
											( preg_match('@<meta http-equiv=[\'"]content-type[\'"][^>]+>@si', $tmp[ 0 ], $tmp ) > 0 )
										) {
											$charset	= ( preg_match('@charset=([^\'";]+)[\'";]@i', $tmp[ 0 ], $tmp ))
												? strtolower( $tmp[ 1 ])
												: $charset;
										}
										$this->_charset_added			= TRUE;
										$this->lists['Charset'][ $i ]	= $charset;
									} else if( $default_charset ) {
										$this->_charset_added			= TRUE;
										$this->lists['Charset'][ $i ]	= $default_charset;
									}
								} elseif( strpos( $type, 'text/xml') === 0 ) {
									// 2012-03-26 bugfix: 'Undefined variable: showFileContents' (s/b str)
									// try to find the charset from the encoding
									$charset										= 'utf-8';		// most likely default
									if(( $str = file_get_contents( $filename )) != FALSE ) {
										if(
											( preg_match('@<\?xml[^>]+encoding=[^>]+\?>@si', $str, $tmp ) > 0 )
										) {
											$charset	= ( preg_match('@encoding=[\'"]([^\'"]+)[\'"]@i', $tmp[ 0 ], $tmp ))
												? strtolower( $tmp[ 1 ])
												: 'utf-8';
										}
										$this->_charset_added			= TRUE;
										$this->lists['Charset'][ $i ]	= $charset;
									} else if( $default_charset ) {
										$this->_charset_added			= TRUE;
										$this->lists['Charset'][ $i ]	= $default_charset;
									}
								} else if( $default_charset ) {
									$this->_charset_added					= TRUE;
									$this->lists['Charset'][ $i ]			= $default_charset;
								}

								// add text file contents?
								if( $add_text ) {
									if( $type == 'text/rtf') {
										$this->_text_added	= TRUE;
										$lines					= array();
										$command					= '/usr/bin/unrtf -n --text '. escapeshellarg( $filename );
										$err						= '';
										@exec( $command, $lines, $err );

										if( $err or empty( $lines )) {
											$this->lists['text'][ $i ]		= '(an error occurred whilst extracting the text from the file; sorry, cannot show)';
										} else {
											$this->lists['text'][ $i ]		= implode("\n", $lines );
											// the text will output in the server default
											$this->_charset_added			= TRUE;
											$this->lists['Charset'][ $i ]	= 'utf-8';
										}
									} else if(( $str = file_get_contents( $filename )) != FALSE ) {
										$this->_text_added			= TRUE;
										$this->lists['text'][ $i ]	= $str;
									}
								}
							} else if( $recurse ) {	// if( strpos( $tmp, 'text') === 0 )
								// check for archive-type and store if so
								switch( $type ) {
									case 'application/vnd.is-cab-compressed':
									case 'application/vnd.ms-cab-compressed':
									case 'application/vnd.ms-cab-wince':
									case 'application/x-archive':
									case 'application/x-arj':
									case 'application/x-bcpio':
									case 'application/x-bzip2':
									case 'application/x-cpio':
									case 'application/x-debian-package':
									case 'application/x-gtar':
									case 'application/x-gzip':
									case 'application/x-iso9660':
									case 'application/x-java-archive':
									case 'application/x-lha':
									case 'application/x-lharc':
									case 'application/x-rar':
									case 'application/x-rpm':
									case 'application/x-tar':
									case 'application/x-zip':
									case 'application/zip':		$isArchive	= TRUE;	break;
									case 'application/x-dosexec':
										// ms .exe *may* be archives; difficult this one
										if( strtolower( substr( $this->lists['Filename'][ $i ], -4 )) == '.exe') {
											$isArchive	= TRUE;
										} else {
											$isArchive	= FALSE;
										}
										break;
									default:							$isArchive	= FALSE;
								}
								if( $isArchive ) {
									// check for large archives that could cause us to hit fatal out-of-memory errors
									// we are going to assume that files are < 2GB (!)
									if( $originalMem > 0 ) {	// else setting is no-limit
										if(( $size = filesize( $filename )) == FALSE )								continue;	// file error
										// duck out if we hit 90% of PHP runtime memory limit
										if( function_exists('memory_get_usage')) {
											if(( memory_get_usage( TRUE ) + $size ) > ( 0.9 * $originalMem ))	continue;
										} else {
											// estimate as best we can
											if(( $currentMem - $size ) < ( 0.2 * $originalMem ))					continue;
											$currentMem	-= $size;
										}
									}
									if(( $str = file_get_contents( $filename )) != FALSE ) {
										$this->_recurse							= TRUE;
										$this->lists['archive'][ $i ]			= $str;
										$this->lists['md5'][ $i ]				= md5( $str );	// reqd for subsequent caching
									}
								}	// if( $isArchive )
							}	// if( strpos( $tmp, 'text') === 0 ) else if( $recurse )
						}	// if( $is_a_dir or $this->lists['Encrypted'][ $i ]) else
					}	// if( $add_mime )
				}	// foreach( $this->lists['Path'] as $i => $path )
			}	// if( empty( $this->lists['Path'])) else

			if( $output_dir ) {
				chdir( $this->_temp_dir );
				if( !$this->del_dir( $output_dir )) {		// remove the tmp files
					$this->_7z_error	.= "<br />p7z::p7z error: unable to remove tmp dir `$output_dir'.<br />";
				}
				ignore_user_abort( $oldSetting );			// don't mind user-abort now
			}

			if( !empty( $this->lists )) {
				// re-order by `Directory' then `Filename'.
				// note: array_multisort() re-orders int keys, so original order lost.
				// shenanigans below to achieve natural-case-order sort
				$this->lists['Directory']	= array_map('strtolower', $this->lists['Directory']);
				$this->lists['Filename']	= array_map('strtolower', $this->lists['Filename']);
				array_multisort(
					$this->lists['Directory'], SORT_STRING,
					$this->lists['Filename'], SORT_STRING,
					$this->lists['Path'],		// all others are here only to keep keys in sync
					$this->lists['Size'],
					$this->lists['Packed Size'],
					$this->lists['Modified'],
					$this->lists['CRC'],
					$this->lists['Host OS'],
					$this->lists['Folder'],
					$this->lists['Attributes'],
					$this->lists['Encrypted'],
					$this->lists['Comment'],
					$this->lists['Method'],
					$this->lists['Content-Type'],
					$this->lists['Charset'],
					$this->lists['text'],
					$this->lists['archive'],
					$this->lists['md5']
				);

				// restore original case to `Directory' + `Filename', plus other admin
				// 2007-10-12 bugfix for original `$this->_encrypt_all' peverse coding
				$this->_encrypt_all	= TRUE;
				foreach( $this->lists['Path'] as $i => $path ) {
					$this->lists['kB'][ $i ]	= number_format((( $size = $this->lists['Size'][ $i ] / 1024 ) > 1 ) ? $size : 1 );
					if( $this->lists['Attributes'][ $i ]) {
						$this->_attributed	= TRUE;
					}
					if( $this->lists['Comment'][ $i ]) {
						$this->_commented		= TRUE;
					}
					if( $this->lists['Encrypted'][ $i ]) {
						$this->_encrypted		= TRUE;
					} else {
						$this->_encrypt_all	= FALSE;
					}
					if( $this->lists['Host OS'][ $i ]) {
						$this->_host_os		= $this->lists['Host OS'][ $i ];
					}
					if( $this->lists['Folder'][ $i ]) {								// it's a dir
						$this->num_dirs++;
						$this->lists['Directory'][ $i ]	= $path;
//						$this->lists['Filename'][ $i ]	= '';
					} else {
						$this->num_files++;
						$this->size				+= $this->lists['Size'][ $i ];
						$this->size_packed	+= $this->lists['Packed Size'][ $i ];
						if(( $pos = strrpos( $path, '/')) !== FALSE ) {		// a directory/filename
							$this->lists['Directory'][ $i ]	= substr( $path, 0, $pos );
							$this->lists['Filename'][ $i ]	= substr( $path, $pos + 1 );
						} else {																// just a filename
//							$this->lists['Directory'][ $i ]	= '';
							$this->lists['Filename'][ $i ]	= $path;
						}
					}
				}	// foreach( $this->lists['Path'] as $i => $path )
			}	// if( !empty( $this->lists ))
		}	// p7z::p7z()
/* del_dir()
 *
 *   Empty supplied dir + all sub-directories + (by default) remove all
 *
 *   Based on php.net/manual/en/function.rmdir.php,
 *+  eli dot hen at gmail dot com, 25-Jan-2007 03:54 (bugfixed)
 *
 * @param string   $dir        dir to remove, no end slash
 * @param boolean  $only_empty just empty $dir without deleting
 * @return boolean TRUE/FALSE  $dir was emptied/deleted
 * @access public
 *
 * 2008-03-24 added:  DIRECTORY_SEPARATOR to attempt Windows compatibility
 *+           change: no longer assumes that `$dir' is also $CWD (required for opendir()).
 *+           bugfix: is_dir() on symbolic links will report target, not link.
 *+           bugfix: added correct `!== FALSE' check on readdir().
 * Note 1: Use of `$only_empty' requires PHP5 due to use of scandir().
 * Note 2: PHP4 strips any trailing slash with realpath(), PHP5 leaves it in place.
 * Note 3: Permission or filesystem errors during opendir() or chdir() will generate E_WARNING errors.
 *+        These can be suppressed with `@' prepended to function.
 * Note 4: is_dir() always returns false if the handle from opendir() is NOT from $CWD.
 *+        see: php.net/manual/en/function.is-dir.php
 *+             alan dot rezende at light dot com dot br 29-Sep-2006 01:42
 * Note 5: File/Dir permissions: essentially, PHP can only remove files/dirs that it created.
 * Note 6: Windows problems: from experience, it is v common for another process to lock a file/dir,
 *+        which will prevent deletion.
 */
		function del_dir(
			$dir,
			$only_empty	= FALSE
		) {
			$CWD		= getcwd();
			if( chdir( $dir ) == FALSE ) return FALSE;

			$dscan	= array( realpath( $dir ));
			$darr		= array();
			while( !empty( $dscan )) {
				$dcur		= array_pop( $dscan );
				$darr[]	= $dcur;
				if( $d = opendir( $dcur )) {
					while(( $f = readdir( $d )) !== FALSE ) {
						if(( $f == '.') or ( $f == '..'))			continue;
						$f	= $dcur . DIRECTORY_SEPARATOR . $f;

						if( is_dir( $f ) and ( !is_link( $f )))	$dscan[]	= $f;
						else													unlink( $f );
					}
					closedir( $d );
				}
			}

			$i_until	= ( $only_empty ) ? 1 : 0;
			for( $i = count( $darr ) - 1; $i >= $i_until; $i-- ) {
				// echo "\nDeleting '".$darr[$i]."' ... ";
				rmdir( $darr[ $i ]);
			}

			$result	= ( $only_empty ) ? ( count( scandir ) <= 2 ) : ( !is_dir( $dir ));
			chdir( $CWD );

			return $result;
		}	// p7z::del_dir()
/* getComment()
 *
 * Return string Comment (archive-wide comment, can be multi-line)
 *
 * (See also isCommented())
 *
 * @param none
 * @return string $_comment
 * @access public
 */
		function getComment() {
			return $this->_comment;
		}
/* getList()
 *
 * Return array Listing
 *
 * Default is to return $lists or FALSE (not an archive).
 * `$lists' is a 2-level deep array of file/dir values:
 *
 *   lists[ $param ][ int ] = value
 *
 * (See `Key names for $this->lists' below for possible $param values)
 *
 * @param string
 * @return array mixed $lists, boolean False
 * @access public
 */
		function getList( $param = '') {
			if( empty( $param )) {
				return ( empty( $this->lists )) ? FALSE : $this->lists;
			} else {
				return ( empty( $this->lists[ $param ])) ? FALSE : $this->lists[ $param ];
			}
		}
/* getHostOS()
 *
 * Return Host File-System
 *
 * @param none
 * @return string $_host_os
 * @access public
 */
		function getHostOS() {
			return $this->_host_os;
		}
/* getNumDirs()
 *
 * Return Number of Directories (Folders) within archive
 *
 * @param none
 * @return int $num_dirs
 * @access public
 */
		function getNumDirs() {
			return $this->num_dirs;
		}
/* getNumFiles()
 *
 * Return Number of Files (not counting dirs) within archive
 *
 * @param none
 * @return int $num_files
 * @access public
 */
		function getNumFiles() {
			return $this->num_files;
		}
/* getSize()
 *
 * Return un-packed size in bytes of files within archive
 *
 * Note that this is NOT the size that the files will occupy on disk
 *
 * @param none
 * @return int $size
 * @access public
 */
		function getSize() {
			return $this->size;
		}
/* getSizePacked()
 *
 * Return packed size in bytes of files within archive
 *
 * @param none
 * @return int $size_packed
 * @access public
 */
		function getSizePacked() {
			return $this->size_packed;
		}
/*
 * Return 7z error string, or FALSE
 *
 * (See also isError(), isExecError)
 *
 * @param none
 * @return string $_7z_error, FALSE
 * @access public
 */
		function is7zError() {
			return ( $this->_7z_error != '') ? $this->_7z_error : FALSE;
		}
/* isAllEncrypted()
 *
 * Return boolean as to whether all files in $lists are encrypted
 *
 * (See also isEncrypted())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isAllEncrypted() {
			return ( $this->_encrypt_all ) ? TRUE : FALSE;
		}
/* isArchive()
 *
 * Return boolean as to whether Constructor parameter is an archive
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isArchive() {
			return ( empty( $this->lists )) ? FALSE : TRUE;
		}
/* isAttributed()
 *
 * Return boolean as to whether any file in $lists is attributed
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isAttributed() {
			return ( $this->_attributed ) ? TRUE : FALSE;
		}
/* isCharsetAdded()
 *
 * Return boolean as to whether any file in $lists has a charset
 *
 * (See also isMimeAdded())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isCharsetAdded() {
			return ( $this->_charset_added ) ? TRUE : FALSE;
		}
/* isCommented()
 *
 * Return boolean as to whether any dir/file in $lists is commented
 *
 * (See also getComment())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isCommented() {
			return ( $this->_commented ) ? TRUE : FALSE;
		}
/* isEncrypted()
 *
 * Return boolean as to whether any file in $lists is encrypted
 *
 * (See also isAllEncrypted())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isEncrypted() {
			return ( $this->_encrypted ) ? TRUE : FALSE;
		}
/* isError()
 *
 * Return boolean as to whether either p7z or exec() return an error code
 *
 * (See also is7zError(), isExecError)
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isError() {
			return ( $this->_7z_error or $this->_exec_error ) ? TRUE : FALSE;
		}
/* isExecError()
 *
 * Return boolean as to whether exec() returned an error code
 *
 * (See also isError(), is7zError())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isExecError() {
			return ( $this->_exec_error != '') ? $this->_exec_error : FALSE;
		}
/* isMimeAdded()
 *
 * Return boolean as to whether $lists contains `Content-Type' values
 *
 * (See also isCharsetAdded(), isTextAdded())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isMimeAdded() {
			return ( $this->_mime_added ) ? TRUE : FALSE;
		}
/* isRecursion()
 *
 * Return boolean as to whether $lists contains (at least one) `archive' content
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isRecursion() {
			return ( $this->_recurse ) ? TRUE : FALSE;
		}
/* isTextAdded()
 *
 * Return boolean as to whether $lists contains content with `text/*' (Content-Type) values
 *
 * (See also isMimeAdded())
 *
 * @param none
 * @return boolean
 * @access public
 */
		function isTextAdded() {
			return ( $this->_text_added ) ? TRUE : FALSE;
		}
// p7z Private functions follow
/* _list_7z()
 *
 *   Use p7zip to list files + directories within $archive
 *
 * (See also _list_rpm() + _list_from_dir())
 *
 * @param string $archive server path to (possibly) 7z archive file
 * @return boolean
 * @access private
 *
 * 28 Mar 2010 bugfix: missing `$dirNotAdded' means dupe `['Directory']' was added
 * 21 Sep 2009 bugfix: some .exe archives have empty Size for files + fixed logic error
 * 16 Jan 2008 mod: no dir reported on some files, so added detection for those cases
 * 12 Oct 2007 bugfix: archive comment now gathered correctly
 */
		function _list_7z(
			$archive		// full server path + filename
		) {
			$_command			= '/usr/local/bin/7z l -slt ';	// command to obtain file listing
			$hasDir				= FALSE;									// to flag archive contains directories
			$dirNotAdded		= TRUE;									// to flag `$this->lists['Directory']' has NOT been added

			$_error				= @exec("$_command'$archive'", $lines, $_exec_error );

			if( $_exec_error ) {
				$this->_exec_error	.= "p7z::_list_7z(): Exec errorcode: `$_exec_error'; return value: `$_error'.<br />\n";
				return FALSE;
			}

			$this->archive		= $archive;
			$i						= -1;
			foreach( $lines as $key => $line ) {
				// there may be both an archive-wide comment ($this _comment),
				// and a comment for individual files ($this->lists['Comment']).
				// the archive comment appeared in p7zip listing between v4.45 and v4.55
				if( strpos( $line, ' =') === FALSE ) {
					if( !empty( $this->lists ))	continue;	// inter file-info-block line
					if( empty( $this->_comment ))	continue;	// no archive comment

					// in middle of gathering multi-line archive comment
					$line					= rtrim( $line );
					$this->_comment	.= "\n$line";
				} else {
					list( $k, $v )	= explode(' =', $line, 2 );				// need ` =' to include `Comment'
					$k	= trim( $k );
					$v	= trim( $v );

					// `Path' is the first Technical info
					if( empty( $this->lists ) and ( $k != 'Path')) {
						if( !empty( $this->_comment )) {
							// still in middle of gathering multi-line archive comment!
							$line					= rtrim( $line );
							$this->_comment	.= "\n$line";
						}
						// between v4.45 and v4.55 other non-comment info can appear here...
						// ...including `Encrypted =' info. Sigh.
						continue;
					}

					if( $k == 'Path') {
						// does the archive contain directories?
						if( strrpos( $v, '/') !== FALSE )	$hasDir	= TRUE;

						$i++;
						// `Folder' and `Encrypted' designation changed between v4.45 and v4.55 (sigh)
						// (not all archives contain `Folder', and/or a dir entry)
						if(( $lines[ $key + 1 ] == 'Folder = 1') or ( $lines[ $key + 1 ] == 'Folder = +')) {	// a directory
							$this->lists['Directory'][ $i ]	= $v;
							$this->lists['Filename'][ $i ]	= '';
							$dirNotAdded							= FALSE;
						} else {
							if(( $pos = strrpos( $v, '/')) !== FALSE ) {	// a directory/filename
								// 2010-03-28 bugfix: `$dirNotAdded' was missing
								$this->lists['Directory'][ $i ]	= substr( $v, 0, $pos );
								$this->lists['Filename'][ $i ]	= substr( $v, $pos + 1 );
								$dirNotAdded							= FALSE;
							} else {														// just a filename
								$this->lists['Directory'][ $i ]	= '';
								$this->lists['Filename'][ $i ]	= $v;
							}
						}
					}	// if( $k == 'Path')
					switch( $k ) {
						// 2009-09-21 BugFix: `Size' < 1 cannot be trusted as signifier of directory in some archives
						//          + as `Size' may be empty for a file whilst `Packed Size' will not (7-Zip v4.65)
						case 'Folder':
						case 'Encrypted':		$this->lists[ $k ][ $i ]	= (( $v == '1') or ( $v == '+'));	break;
						case 'Size':
						case 'Packed Size':	$this->lists[ $k ][ $i ]	= ( int ) $v;
													if( isset( $this->lists['Size'][ $i ]) and isset( $this->lists['Packed Size'][ $i ])) {
														if(( $this->lists['Size'][ $i ] < 1 ) and ( $this->lists['Packed Size'][ $i ] < 1 )) {
															$hasDir	= TRUE;
														}
													}
													break;
						case 'Path':
						case 'Modified':
						case 'CRC':
						case 'Host OS':
						case 'Attributes':	if(( $k == 'Attributes') and ( strtolower( $v[ 0 ]) == 'd'))	$hasDir	= TRUE;
						case 'Comment':		if( $i == -1 ) {	// begin archive-wide comment
														$this->_comment	= $v;
														continue;
													}
						case 'Method':			$this->lists[ $k ][ $i ]	= ( string ) $v;							break;
						// others thrown away, to prevent array_multisort() from throwing an error
					}
				}	// if( empty( $line ) or ( strpos( $line, ' =') === FALSE )) else
			}	// foreach( $lines as $key => $line )

			// 2008-01-16 check for missing directory names
			if( $hasDir and $dirNotAdded ) {
				// archive contains sub-directories, but does not list them
				$dirs	= array();
				foreach( $this->lists['Directory'] as $k => $v ) {
					if( !empty( $v )) {	// contains sub-directory(s)
						$dir	= $v;
						while( empty( $dirs[ $dir ])) {
							$i++;
							$dirs[ $dir ]	= TRUE;
							$this->lists['Path'][ $i ]			=  // intentionally left empty!
							$this->lists['Directory'][ $i ]	= $dir;
							$this->lists['Filename'][ $i ]	= '';

							// use existing values for file as template for dir values
							if( !empty( $this->lists['Folder']))		$this->lists['Folder'][ $i ]			= TRUE;
							if( !empty( $this->lists['Encrypted']))	$this->lists['Encrypted'][ $i ]		= $this->lists['Encrypted'][ $k ];
							if( !empty( $this->lists['Size']))			$this->lists['Size'][ $i ]				= 0;
							if( !empty( $this->lists['Packed Size']))	$this->lists['Packed Size'][ $i ]	= 0;
							if( !empty( $this->lists['Modified']))		$this->lists['Modified'][ $i ]		= $this->lists['Modified'][ $k ];
							if( !empty( $this->lists['CRC']))			$this->lists['CRC'][ $i ]				= '';
							if( !empty( $this->lists['Host OS']))		$this->lists['Host OS'][ $i ]			= $this->lists['Host OS'][ $k ];
							if( !empty( $this->lists['Attributes'])) {
								$this->lists['Attributes'][ $i ]	= 'D'. substr( $this->lists['Attributes'][ $k ], 1 );
							}
							if( !empty( $this->lists['Comment']))		$this->lists['Comment'][ $i ]			= '';
							if( !empty( $this->lists['Method']))		$this->lists['Method'][ $i ]			= '';

							// check for parent to this dir
							if(( $pos = strrpos( $dir, '/')) !== FALSE )	$dir	= substr( $dir, 0, $pos );
						}	// while( empty( $dirs[ $dir ])
					}	// if( !empty( $v ))
				}	// foreach( $this->lists['Directory'] as $k => $v )
			}	// if( $hasDir and $dirNotAdded )

			if( !empty( $this->_comment ))	$this->_comment	= rtrim( $this->_comment );
			return TRUE;
		}	// p7z::_list_7z()
/* _list_from_dir()
 *
 *   Use previously extracted files to list files + directories within $archive
 *   (unfortunately the list-files, etc command in `unshield' is too buggy to use)
 *   (unfortunately the list-files, etc command in `unstuff' does not exist)
 *
 * (See also _list_7z() + _list_rpm())
 *
 * @param none
 * @return boolean
 * @access private
 *
 * 26 Sep 2009 bugfix: fixed errors in `Directory'/`Filename' setting + added date handling
 *           + (although neither unshield nor unstuff actually restores file times).
 */
		function _list_from_dir() {
			// the Class is positioned within root of extracted directory of files on entry
			$_command	= 'ls . --block-size=1 --time-style=+%s -1AgGlRs';
			$_error		= @exec( $_command, $lines, $_exec_error );

			if( $_exec_error or empty( $lines )) {
				if( $_exec_error )	$this->_exec_error	.= "p7z::_list_from_dir(): Exec errorcode: `$_exec_error'; return value: `$_error'.<br />\n";
				else						$this->_exec_error	.= "p7z::_list_from_dir(): \$lines is empty; return value: `$_error'.<br />\n";
				return FALSE;
			}

			$dir	= '';
			$i		= -1;
			foreach( $lines as $line ) {
				$line										= trim( $line );
				if( empty( $line ))							continue;
				if( strpos( $line, 'total ') === 0 )	continue;

				if( substr( $line, -1 ) == ':') {	// directory header to block of dirs/files in that directory
					if( strlen( $line ) > 2 ) {		// else is first header
						$dir	= substr( $line, 2, -1 );
					}
					continue;
				}
				// now travelling down through block of dirs/files
				// eg: `     32768 -rw------- 1    30659 1253852582 .bash_history'
				// note that unshield stores files as current time
				$i++;
				list( $scrap1, $att, $scrap2, $bytes, $secs, $name )	= split('[ ]+', $line, 6 );
				$att										= trim( $att );
				$secs										= ( int ) $secs;
				$name										= trim( $name );
				$this->lists['Modified'][ $i ]	= date( 'Y-m-d H:i:s', $secs );
				if( strtolower( $att[ 0 ]) == 'd') {	// a directory
					$this->lists['Directory'][ $i ]	= ( $dir ) ? "$dir/$name" : $name;
					$this->lists['Filename'][ $i ]	= "";
					$this->lists['Folder'][ $i ]		= TRUE;
					$this->lists['Path'][ $i ]			= ( $dir ) ? "$dir/$name" : $name;
					$this->lists['Size'][ $i ]			= 0;
				} else {										// a file
					$this->lists['Directory'][ $i ]	= $dir;
					$this->lists['Filename'][ $i ]	= $name;
					$this->lists['Folder'][ $i ]		= FALSE;
					$this->lists['Path'][ $i ]			= ( $dir ) ? "$dir/$name" : $name;
					$this->lists['Size'][ $i ]			= ( int ) $bytes;
				}
			}	// foreach( $lines as $key => $line )

			return TRUE;
		}	// p7z::_list_from_dir()
/* _list_rpm()
 *
 *   Use RPM to list files + directories within .rpm $archive
 *
 * (See also _list_7z() + _list_from_dir())
 *
 * @param string $archive server path to RPM archive file
 * @return boolean
 * @access private
 *
 * 27 Mar 2008 mod: added `--nosignature' to $_command (OK if you trust the RPM).
 */
		function _list_rpm(
			$archive		// full server path + filename
		) {
			$_command			= 'rpm -qplv --nosignature ';									// command to obtain file listing
			$_command_host		= 'rpm -qp --qf --nosignature \'%{OS}\' ';				// command to obtain OS Host
			$_command_desc		= 'rpm -qp --qf --nosignature \'%{DESCRIPTION}\' ';	// command to obtain description

			$_error				= @exec("$_command'$archive'", $lines, $_exec_error );

			if( $_exec_error or empty( $lines )) {
				if( $_exec_error )	$this->_exec_error	.= "p7z::_list_rpm(): Exec errorcode: `$_exec_error'; return value: `$_error'.<br />\n";
				else						$this->_exec_error	.= "p7z::_list_rpm(): \$lines is empty; return value: `$_error'.<br />\n";
				return FALSE;
			}

			$host					= trim( @exec("$_command_host'$archive'"));
			$this->_comment	= trim( @exec("$_command_desc'$archive'"));
			$this->archive		= $archive;
			$i						= -1;
			foreach( $lines as $scrap => $line ) {
				if( empty( $line )) {
					// skip
				} else {
					$i++;
					list( $permission, $inode, $own, $gp, $size, $mon, $day, $year, $path ) = sscanf( $line, "%s %d %s %s %d %s %s %s %s");
					$this->lists['Attributes'][ $i ]		= trim( $permission );
					$this->lists['CRC'][ $i ]				= // intentionally left empty!
					$this->lists['Method'][ $i ]			= '';
					$this->lists['Encrypted'][ $i ]		= FALSE;
					$this->lists['Comment'][ $i ]			= '';
					$this->lists['Host OS'][ $i ]			= $host;
					$this->lists['Modified'][ $i ]		= date('Y-m-d', strtotime("$mon $day $year"));
					$this->lists['Packed Size'][ $i ]	= 0;
					$this->lists['Size'][ $i ]				= ( int ) trim( $size );
					$this->lists['Path'][ $i ]				= $path = trim( $path );
					if( $permission[ 0 ] == 'd') {								// a directory
						$this->lists['Folder'][ $i ]		= TRUE;
						$this->lists['Directory'][ $i ]	= $path;
						$this->lists['Filename'][ $i ]	= '';
					} else {
						$this->lists['Folder'][ $i ]		= FALSE;
						if(( $pos = strrpos( $path, '/')) !== FALSE ) {	// a directory/filename
							$this->lists['Directory'][ $i ]	= substr( $path, 0, $pos );
							$this->lists['Filename'][ $i ]	= substr( $path, $pos + 1 );
						} else {															// just a filename
							$this->lists['Directory'][ $i ]	= '';
							$this->lists['Filename'][ $i ]	= $path;
						}
					}	// if( $permission[ 1 ] == 'd') else
				}	// if( empty( $line )) else
			}	// foreach( $lines as $scrap => $line )

			return TRUE;
		}	// p7z::_list_rpm()
/* _ret_bytes()
 *
 *   Return #bytes; supply with ini_get() value, poss in str format
 *
 * (Obtained from php.net/manual/en/function.ini-get.php)
 *
 * @param string $val value in integer or string
 * @return int
 * @access private
 */
		function _ret_bytes(
			$val	// value obtained from ini_get()
		) {
			$val	= trim( $val );
			$last	= strtolower( $val{ strlen( $val ) - 1 });
			switch( $last ) {
				// The `G' modifier is available since PHP 5.1.0
				case 'g':	$val	*= 1024;
				case 'm':	$val	*= 1024;
				case 'k':	$val	*= 1024;
			}

			return ( int ) $val;
		}	// p7z::_ret_bytes()
	}	// end class p7z
// -------------- End of Class Declaration ----------------
/*
 * Help, Advice + Other Stuff
 *
 *    This is a fairly quick 'n' dirty implementation of an interface for 7z on a computer.
 *+   At this moment it has been tested only on Linux, and restricts itself to listing an
 *+   archive's contents.
 *    It can also return the Content-Type of all files.
 *    It can also return the Charset + contents of `text/...' files.
 *    It can also return the content of archives stored within the Archive.
 *
 *
 * Stats:
 * -----
 *    Class constructor took avg 0.38 secs in v0.10 testing (`7z l -slt <filename>')
 *+   on a 4,348 kB (4.25 MB) archive of 61 files (root + 4 directories).
 *+   (see http://download.modem-help.co.uk/mfcs-0/2Wire/802-11b-Wireless-PC-Card/twwpc-78638623-exe)
 *    Non-archives took about half that.
 *    [v0.11.1 Note: use of $add_mime = TRUE increased this to > 1 sec]
 *    [v0.11.2 Note: further increase due to need to use `file' command rather than mime_content_type()]
 *    [v0.11.5 Note: page-display on 114MB archive took terrifying 51 secs (1,065 files,64 directories)]
 *    Implementing caching via Cache/Lite dropped all to 0.0006 secs.
 *
 *
 * Cache/Lite Stats:
 * ----------------
 *    The md5 + byte-size of the file was used as an ID to pass to Cache/Lite (thus is file-location
 *+   independant). After about 23 hours (v0.10) there were 307 files in the cache occupying 951 kb:
 *
 *    Non-archives files:    386 bytes
 *    Archive files: min:    918 bytes
 *                   max: 62,504 bytes
 *
 *    After 2 months this was 13,355 files occupying 807,969 kB (avg 60kB each) (v0.11.5).
 *
 *
 * Key names for $this->lists
 * --------------------------
 *    `$this->lists' is an array of archive stats for each file/dir, in the form:
 *
 *      $this->lists[ key ][ int ] = value
 *
 *    where: key =
 *      `Attributes'   => (string) format `DR..A'
 *      `CRC'          => (string)
 *      `Charset'      => (string) empty string if $default_charset == FALSE or not text/... or encrypted
 *      `Comment'      => (string) (archive-wide comment stored in $this->_comment)
 *      `Content-Type' => (string) empty string if $add_mime == FALSE or encrypted
 *      `Directory'    => (string) dir component of filename; no leading/trailing slash
 *      `Encrypted'    => (bool)
 *      `Filename'     => (string) name of file; actual path component missing
 *      `Folder'       => (bool)   (is/is not a directory)
 *      `Host OS'      => (string)
 *      `Method'       => (string) `Deflate', `Store', etc
 *      `Modified'     => (string) format `yyyy-mm-dd hh:mm:ss' (RPM: `yyyy-mm-dd')
 *      `Packed Size'  => (int)    bytes packed
 *      `Path'         => (string) [`Directory'][`/'][`Filename']
 *      `Size'         => (int)    bytes un-packed
 *      `archive'      => (varies) empty string if $recurse == FALSE or encrypted
 *      `kB'           => (string) unpacked size as formatted string in kB, min 1 kB
 *      `md5'          => (string) empty string if $recurse == FALSE or encrypted
 *      `text'         => (string) empty string if $add_text == FALSE or encrypted
 *
 *    and:   int = zero-based array key.
 *
 *    Note 1: Non-archives have an empty array [array()]
 *    Note 2: All archives have all keys, though any may be empty, with the sole exception of:
 *               `Directory'   root entries are empty
 *               `Filename'    dir values empty
 *               `Folder'
 *               `Path'
 *		Note 3: Above departs from conventional naming convention (a dir filename is empty). `Path'
 *				  contains full dir+filename for all dirs + files. That makes for easy natural-case
 *				  order sorting & follows 7-Zip convention.
 *
 *    This is the full list of possible archive attributes:
 *    (from p7zip_4.44/CPP/7zip/UI/Console/List.cpp)
 *
 *       static CPropIdToName kPropIdToName[] =
 *       {
 *          { kpidPath, L"Path" },
 *          { kpidName, L"Name" },
 *          { kpidIsFolder, L"Folder" },
 *          { kpidSize, L"Size" },
 *          { kpidPackedSize, L"Packed Size" },
 *          { kpidAttributes, L"Attributes" },
 *          { kpidCreationTime, L"Created" },
 *          { kpidLastAccessTime, L"Accessed" },
 *          { kpidLastWriteTime, L"Modified" },
 *          { kpidSolid, L"Solid" },
 *          { kpidCommented, L"Commented" },
 *          { kpidEncrypted, L"Encrypted" },
 *          { kpidSplitBefore, L"Split Before" },
 *          { kpidSplitAfter, L"Split After" },
 *          { kpidDictionarySize, L"Dictionary Size" },
 *          { kpidCRC, L"CRC" },
 *          { kpidType, L"Type" },
 *          { kpidIsAnti, L"Anti" },
 *          { kpidMethod, L"Method" },
 *          { kpidHostOS, L"Host OS" },
 *          { kpidFileSystem, L"File System" },
 *          { kpidUser, L"User" },
 *          { kpidGroup, L"Group" },
 *          { kpidBlock, L"Block" },
 *          { kpidComment, L"Comment" },
 *          { kpidPosition, L"Position" }
 *       };
 *   (any not mentioned within $this->lists[ key ] are thrown away)
 *
 *
 * Change Log:
 * ----------
 *    0.13.2 : bugfix: changes to take account of possible undefined offset(s) 30 Oct 11
 *             mod: try to find charset from text/html or text/xml files
 *    0.13.1 : bugfix: dupe Directory in $lists fixed.                         28 Mar 10
 *    0.13   : mod: option to use unstuff added (early Apple Mac files). Can   24 Sep 09
 *+            then open following filetypes:
 *+               sit, cpt, zip, arc, arj, lha, rar, gz, compress, uu, hqx,
 *+               btoa, mime, tar, bin, sitseg, sitsegN, pf, sit5, bz2, apple
 *    0.12.13: bugfix: some .exe archives have empty Size for files            21 Sep 09
 *    0.12.12: mod: early $output_dir declaration; suppress PHP Notices.       09 Apr 08
 *    0.12.11: mod: _list_rpm(): suppress NOKEY error messages from log.       27 Mar 08
 *    0.12.10: bugfixes: del_dir() bugfixes (mainly for symbolic files).       24 Mar 08
 *    0.12.9:  bugfix: account for read-only files during bad .inf/.txt fix.   14 Mar 08
 *    0.12.8:  mod: p7z(): Take account of empty `$path' whilst creating       07 Mar 08
 *+            $lists (reduces PHP Notices in the error_log).
 *    0.12.7:  mod: p7z(): Prev fix for `.inf' files with wrong Content-Type   20 Feb 08
 *+            only worked on lc `.inf' (sigh). Now also works on `.txt' files.
 *    0.12.6:  mod: p7z::_list_7z(): some files (eg Windows zip .exe) do not   08 Jan 08
 *+            report directories even if they contain some; routine added to
 *+            fix that (thus $lists['Directory'] always included).
 *+            mod: p7z(): `.inf' files that have Content-Type wrongly reported
 *+            fixed with workaround; see:
 *+            http://forums.modem-help.co.uk/viewtopic.php?t=707&postdays=0&postorder=asc&start=15
 *    0.12.5:  bugfix: changes to render Class compatible to v4.55;            12 Oct 07
 *+            changes to _list_7z() + other places; use of $_comment (and
 *+            thus getComment() and isCommented()) modified; improvements to
 *+            error-reporting. del_dir() made public so can be used externally
 *+            (also name mod to reflect that).
 *    0.12.4:  added: InstallShield archives as candidates for Recursion;      10 Oct 07
 *+            will require `unshield' on server, plus modifications to server
 *+            magic.mime file. See http://forums.modem-help.co.uk/viewtopic.php?p=1239#1239
 *    0.12.3:  mod: plain-text extraction added for `text/rtf'; requires       09 Oct 07
 *+            `unrtf' on server: http://www.gnu.org/software/unrtf/unrtf.html
 *    0.12.2:  mod: charset included in mime-type on ALL text files, not just  01 Oct 07
 *+            `text/plain'; $this->lists['text/plain'] becomes ['text'].
 *    0.12.1:  bugfix: avoid 'Out of memory' fatal errors on large archives-   14 Sep 07
 *             within-Archive when using $recurse.
 *    0.12  :  $recurse Constructor parameter, $_recurse + isRecursion() added 08 Sep 07
 *+            + $lists extended with `archive' + `md5' keys, all so that
 *+            archives-within-the-Archive can be extracted.
 *    0.11.5:  bugfix: not every archive contains `Folder'; is considered      04 May 07
 *             useful for easy programming, so now is always present in $lists.
 *             p7zip v4.45 added to site - seamless operation.
 *    0.11.4:  bugfix: if encrypted then $add_mime = $add_text = FALSE         28 Apr 07
 *+            added `Charset' to $lists, $_charset_added, isCharsetAdded() +
 *+            Constructor parameter $default_charset for when unknown.
 *    0.11.3:  added $add_text, which adds `text/plain' content to $lists      24 Apr 07
 *+            (content of all `text/plain' files - requires PHP v4.3.0)
 *    0.11.2:  bugfix: switched to use of file from mime_content_type()        24 Apr 07
 *    0.11.1:  activated $add_mime, which adds `Content-Type' to $lists        24 Apr 07
 *    0.11  :  added module for RPMs, as 7z v4.44 is broken with RPMs          23 Apr 07
 *    0.10  :  started; list stats under *nix only                             16 Apr 07
 *
 * Alex Kemp, modem-help.co.uk
 * Contact via PM at http://forums.modem-help.co.uk/
 *
 * Copyright 2007-2009 (c) All Rights Reserved.
 *                     All Responsibility Yours.
 *
 * This code is released under the GNU LGPL Go read it over here:
 * http://www.gnu.org/copyleft/lesser.html
 */
?>