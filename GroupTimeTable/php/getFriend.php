<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5($id);
		$table = $mid+"_Friend";
	 	$jsonArray = array();
	 	$query2 = "select * from $table order by (name)";
	 	$result2 = mysql_query($query2,$connect);
	 	if (!$result2) {
    		echo ("fail");
    		exit();
		}
	 	while($data2 = mysql_fetch_array($result2)){
	 		array_push($jsonArray,array('fmid'=>$data2[0],'name'=>$data2[1],'nickname'=>$data2[2], 'tag'=>$data2[3]));
	 	}
	 	echo json_encode($jsonArray);
	 	mysql_close($connect);
 ?>
