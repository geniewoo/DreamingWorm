<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$pw = $_POST['password'];
	 	$password = md5($pw);
	 	$query = "select * from profile where id = '$id'";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		echo("fail");
    		exit();
		}
	 	$data = mysql_fetch_array($result);
	 	if($data[id] != $id){
	 		echo ("fail");
	 	}
	 	else{
	 		if($data[password] != $password){
	 			echo ("fail");
	 		}
	 		else{
	 			echo ("success");
	 		}
	 	}
	 	mysql_close($connect);
 ?>
