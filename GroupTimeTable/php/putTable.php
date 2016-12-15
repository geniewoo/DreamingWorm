<?php 
		header('Content-Type: application/json','charset=UTF-8');
	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$value = json_decode($_POST['json'],true);
	 	$id = $value['TRANS']['id'];
	 	$mid = md5($id);
	 	$num = (int)$value['TRANS']['num'];
	 	$edit = $value['TRANS']['edit'];
	 	$tableName = $value['TRANS']['tableName'];
	 	$table = $mid.'_'.$num;
	 	$count = count($value['DATA']);
	 	$query = "drop table IF EXISTS $table"; 
	 	$result = mysql_query($query,$connect);
		$query2 = "create table $table(startTime varchar(7) not null, endTime varchar(7) not null, color varchar(8) not null, name varchar(20) not null)DEFAULT CHARACTER SET=utf8";
    	$result2 = mysql_query($query2,$connect);
    	if(!$result2){
    		echo "fail";
    	}else{
    		if(!$edit){
	    		$query3 = "update num_table set num = num+1 where num_table.id = '$mid'";
	    		$result3 = mysql_query($query3,$connect);
	    		if(!$result3){
	    			echo "fail";
	    			exit();
	    		}
    		}
			if($count > 0){
    			for($i = 0; $i < $count;$i++){
    				$startTime = $value['DATA'][$i]['startTime'];
    				$endTime = $value['DATA'][$i]['endTime'];
    				$color =$value['DATA'][$i]['color'];
    				$name = $value['DATA'][$i]['name'];
    				$query4 = "insert into $table(startTime,endTime,color,name) values ('$startTime','$endTime','$color','$name')";
    				$result4 = mysql_query($query4,$connect);
    				if(!$result4){
    					echo "fail";
    					exit();
    				}
				}
			}
			$query5 = "replace into $mid(num,tableName) values('$num','$tableName')";
 			$result5 = mysql_query($query5,$connect);
 			if(!$result5){
 				echo "fail";
 			}else
				echo "success";
    	}
	
	 	mysql_close($connect);
 ?>
