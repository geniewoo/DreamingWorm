<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5($id);
	 	$query = "select * from $mid";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		echo ("fail");
		}
	 	$data = mysql_fetch_array($result);
	 	while($data = mysql_fetch_array($result)){
	 		array_push($jsonArray,array('num'=>$data[0],'tableName'=>$data[1]));
	 	}
	 	echo json_encode($jsonArray);
	 	mysql_close($connect);
 ?>
