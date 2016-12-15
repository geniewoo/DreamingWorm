<?php 
		header('Content-Type: application/json','charset=UTF-8');
	 	$connect = mysql_connect("localhost","pama","1q2w3e4r!") or die("Fail to connect DB");
	 	$db_con = mysql_select_db("pama",$connect);
	 	mysql_query("SET NAMES 'utf8'");
	 	$value = json_decode($_POST['json'],true);
	 	$id = $value['TRANS']['id'];
	 	$mid = md5($id);
	 	$num = $value['TRANS']['num'];
	 	$max = $value['TRANS']['max'];
	 	$table = $mid.'_'.$num;
	 	$query = "drop table IF EXISTS $table"; 
	 	$result = mysql_query($query,$connect);
    	if(!$result){
    		echo "fail1";
    	}else{
    		$query4 = "delete from $mid where $mid.num = '$num'";
    		$result4 = mysql_query($query4,$connect);
    		if(!$result4){
    			echo "fail2";
    				exit();
    		}
    		if($max!=1){
    			for($i = (int)$num+1 ; $i < (int)$max+1; $i++){
    				$table = $mid.'_'.$i;
    				$j = (int)$i - 1;
    				$ntable = $mid.'_'.$j;
    				$query2 = "alter table $table rename to $ntable";
    				$result2 = mysql_query($query2,$connect);
    				if(!$result2){
    					echo "fail3";
    					exit();
    				}
    				$query4 = "update $mid set num = '$j' where num = '$i'";
    				$result4 = mysql_query($query4,$connect);
    				if(!$result4){
    					echo "fail4";
    					exit();
    				}
    			}
    		}
    		$query3 = "update num_table set num = num-1 where num_table.id = '$mid'";
    		$result3 = mysql_query($query3,$connect);
    		if(!$result3)
    			echo "fail5";
    		else
    			echo "success";
    	}
	
	 	mysql_close($connect);
 ?>
