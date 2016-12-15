<?php 
		header('charset=UTF-8');
	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$id = $_POST['id'];
	 	$mid = md5($id);
	 	$pw = $_POST['password'];
	 	$password = md5($pw);
	 	$name = $_POST['name'];
	 	$phone = $_POST['phone'];
	 	$nickname = $_POST['nickname'];
	 	$table = $mid+"_Friend";
	 	$sql = "insert into profile (id, password, name, phone, mid) values ('$id','$password','$name','$phone','$mid','$nickname')";
	 	$result = mysql_query($sql);
	 	if($result == true){
	 		$sql = "insert into num_table (id, num) values ('$mid','0')";
	 		$result2 = mysql_query($sql);
	 		if($result2 == true){
	 			$sql = "create table $mid (num int not null primary key, tableName varchar(20) not null ) DEFAULT character set = utf8";
	 			$result3 = mysql_query($sql);
	 			if($result3 == true){
	 				$sql = "create table $table (fmid varchar(50) not null primary key, name varchar(20) not null, nickname varchar(20) not null tag varchar(20) not null ) DEFAULT character set = utf8";
	 				$result4 = mysql_query($sql);
	 				if($result4 == true){
	 					echo ("success");
	 				}
	 				else{
	 					echo ("fail");
	 				}
	 			}
	 			else{
	 				echo ("fail");
	 			}
	 		}
	 		else{
	 			echo ("fail");
	 		}
	 	}
	 	else{
	 		echo ("fail");
	 	}
 ?>
