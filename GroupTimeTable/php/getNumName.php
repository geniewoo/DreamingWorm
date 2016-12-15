<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5($id);
	 	$query = "select * from num_table where num_table.id = '$mid'";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		echo ("fail");
    		exit();
		}
	 	$data = mysql_fetch_array($result);
	 	if($data[id] != $mid){
	 		echo ("fail");
	 		exit();
	 	}
	 	else{
	 		echo ("$data[num]"."\n");
	 	}
	 	$jsonArray = array();
	 	$query2 = "select * from $mid order by (num)";
	 	$result2 = mysql_query($query2,$connect);
	 	if (!$result2) {
    		echo ("fail");
    		exit();
		}
	 	while($data2 = mysql_fetch_array($result2)){
	 		array_push($jsonArray,array('num'=>$data2[0],'tableName'=>$data2[1]));
	 	}
	 	echo json_encode($jsonArray);
	 	mysql_close($connect);
 ?>
