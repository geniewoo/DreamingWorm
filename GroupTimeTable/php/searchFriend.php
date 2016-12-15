<?php 

	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5(id);
	 	$table = $mid + "_Friend";
	 	$nickname = $_POST['nickname'];
	 	$query = "select * from profile where profile.nickname = '$nickname'";
	 	$result = mysql_query($query,$connect);
	 	if (!$result) {
    		printf("Error: %s\n", mysql_error($connect));
    		exit();
		}
	 	$data = mysql_fetch_array($result);
 		$query2 = "insert into $table(fmid,name,nickname) values ('$data[4]','$data[2]','$data[5]')";
 		$result2 = mysql_query($query2,$connect);
 		if (!$result) {
    		printf("Error: %s\n", mysql_error($connect));
    		exit();
		}else
			echo "success";
	 	mysql_close($connect);
 ?>
