<?php 
		header('Content-Type: application/json; charset=UTF-8');
	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5($id);
	 	$num = $_POST['num'];
	 	$table = $mid.'_'.$num;
	 	$query = "select * from $table";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		echo ("fail");
    		exit();
		}

		$jsonArray = array();

	 	while($data = mysql_fetch_array($result)){
	 		array_push($jsonArray,array('startTime'=>$data[0],'endTime'=>$data[1],'color'=>$data[2],'name'=>$data[3]));
	 	}
	 	$query2 = "select * from $mid where $mid.num = '$num'";
	 	$result2 = mysql_query($query2,$connect);
	 	if (!$result2) {
    		echo ("fail");
    		exit();
		}
	 	$data = mysql_fetch_array($result2);
	 	if($data[num] != $num){
	 		echo ("fail");
	 	}
	 	else{
	 		echo "$data[tableName]"."\n";
	 	}
	 	echo json_encode($jsonArray);
	 	mysql_close($connect);
 ?>
