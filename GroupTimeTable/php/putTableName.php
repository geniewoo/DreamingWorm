<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$num = $_POST['num'];
	 	$tableName = $_POST['tableName'];
	 	$mid = md5($id);
	 	$query = "insert into $mid(num,tableName) values('$num','$tableName')";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		echo ("fail");
		}else{
			echo("success");
		}
	 	
	 	mysql_close($connect);
 ?>
