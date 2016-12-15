<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$phone = $_POST['phone'];
	 	$query = "select * from profile where profile.phone = '$phone'";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		printf("Error: %s\n", mysql_error($connect));
    		exit();
		}
	 	$data = mysql_fetch_array($result);
	 	if($data[phone] != $phone){
	 		echo ("fail");
	 	}
	 	else{
	 		echo "$data[id]";
	 	}
	 	mysql_close($connect);
 ?>
