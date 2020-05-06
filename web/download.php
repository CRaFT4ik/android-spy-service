<?

	$file = $_GET["file"];
	$time = $_GET["lastModifiedTime"];

	if (!is_numeric($time) || $time < 0)
		return404();

	if ($file == 'dex')
	{
		$path_to_file = "files/loadable.dex";
		$filename_for_save = "classes.dex";

		$time /= 1000;
		$filemtime = filemtime($path_to_file);

		if ($time < $filemtime)
		{
			/* Скачивание файла */
			header("HTTP/1.1 200 OK");
			header("Cache-Control: private");
			header("Content-Type: application/force-download");
			header("Content-Length: ".filesize($path_to_file));
			header("Content-Disposition: filename=".$filename_for_save);
			readfile($path_to_file);
		} else
			return204();
		
		exit();
	} else
	{
		return404();
	}
	
	function return204()
	{
		header("HTTP/1.1 204 No Content");
		header("Cache-Control: private");
		exit();
	}
	
	function return404()
	{
		header("HTTP/1.1 400 Bad Request");
		header("Cache-Control: private");
		exit();
	}

?>