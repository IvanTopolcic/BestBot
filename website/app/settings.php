<?php

/**
 * Our settings
 */
return array(
	// Maximum filename length
	'max_filename_length' => 45,
	// Maximum file size in bytes
	// Make sure you change this on the HTML page since it isn't linked currently
	'max_upload_size' => 419430400, // 400MB
	// WAD directory
	'wad_directory' => '',
	// CFG directory
	'cfg_directory' => '',
	// Directory of logs
	'log_directory' => '',
	// List of bad IWADS
	'bad_iwads' => array(
		'DOOM.WAD',
		'DOOM2.WAD',
		'PLUTONIA.WAD',
		'TNT.WAD',
		'HEXDD.WAD',
		'HERETIC.WAD',
		'HEXEN.WAD',
		'NERVE.WAD',
		'GERYON.WAD',
		'MINOS.WAD',
		'NESSUS.WAD',
		'VESPERAS.WAD',
		'VIRGIL.WAD',
		'MANOR.WAD',
		'TTRAP.WAD',
		'CATWALK.WAD',
		'COMBINE.WAD',
		'FISTULA.WAD',
		'GARRISON.WAD',
		'SUBSPACE.WAD',
		'SUBTERRA.WAD',
		'BLACKTWR.WAD',
		'BLOODSEA.WAD',
		'MEPHISTO.WAD',
		'TEETH.WAD',
		'PARADOX.WAD',
		'ATTACK.WAD',
		'CANYON.WAD',
		'STRIFE1.WAD',
		'STRIFE.WAD',
		'VOICES1.WAD',
		'VOICES.WAD'
	),
	// List of commands that shouldn't be used on server startup
	'bad_commands' => array(
		'sv_rconpassword',
		'exec',
		'dir',
		'writeini',
		'masterhostname',
		'sv_adminlistfile',
		'sv_banexemptionfile',
		'sv_banfilereparsetime',
		'sv_enforcemasterbanlist',
		'sv_enforcebans',
		'sv_website',
		'sv_hostemail',
		'sv_updatemaster',
		'sv_showlauncherqueries',
		'sv_queryignoretime',
		'sv_pure',
		'sv_logfile_append',
		'sv_logfilenametimestamp',
		'sv_logfiletimestamp',
		'sv_logfiletimestamp_usedate',
		'sv_markchatlines',
		'sv_timestamp',
		'sv_timestampformat',
		'sv_colorstripmethod',
		'logfile',
		'crashout',
		'error',
		'error_fatal',
		'wait',
		'quit',
		'crashlogs',
		'crashlogs_dir'
	)
);
