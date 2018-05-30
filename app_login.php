<?php 
require_once("includes\connector.php");
$response=array();
if(isset($_POST['username']) && isset ($_POST['password'])){
	if(!empty($_POST['username'])& !empty($_POST['password'])){
		//Sanitising the input
		$username=htmlentities($_POST['username'],ENT_QUOTES);
		$password=htmlentities($_POST['password'],ENT_QUOTES);
		
		//Preparing and executing login query			
		$query="SELECT user_id,username,password,designation FROM users WHERE username=? LIMIT 1";
		$stmt=$conn->prepare($query);
		$stmt->bind_param("s",$username);
		$res=$stmt->execute();
		$stmt->bind_result($user_id,$user_name,$hash,$designation);
		
		
		
		if ($row=$stmt->fetch()){		
			if(password_verify($password,$hash)){
					$stmt->close();
					$user_name;
					$date=date("Y-m-d H:i:s",time());
					$ip_address=$_SERVER['REMOTE_ADDR'];
					$log_query="INSERT INTO login_logs(user_id,ip_address,timestamp) VALUES (?,?,?)";
					@$log_stmt=$conn->prepare($log_query);
					@$log_stmt->bind_param("sss",$user_id,$ip_address,$date);
					@$res=$log_stmt->execute();
					
					if($res){
						$user=array();
						$user['username']=$user_name;
						$user['designation']=$designation;
						$user['token']=md5(uniqid(mt_rand(),true));
						
						$response["user"]=array();
						
						array_push($response["user"],$user);
						
						$response["success"]=1;
						echo json_encode($response);
						}
						else{
						$response["success"]=0;
						$response["message"]="Couldn't create log<br />".$log_stmt->error;
						echo json_encode($response);
						}
					
				}else{
					$response["success"]=0;
					$response["message"]="Username  or password is incorrect";
					echo json_encode($response);			
					}
		}else{
			$response["success"]=0;
			$response["message"]="Username  or password is incorrect";
			echo json_encode($response);					
			}	
	}else {
			$response["success"]=0;
			$response["message"]="Please fill in all fields";
			echo json_encode($response);
			}
}
?>