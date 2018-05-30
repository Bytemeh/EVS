<?php 
require_once("includes\connector.php");
$response=array();
if(isset($_POST['user_id'])){
	if(!empty($_POST['user_id'])){
		$user_id=htmlentities($_POST['user_id']);
		
		$query="SELECT * FROM course_units";
		$stmt=$conn->prepare($query);
		$res=$stmt->execute();
		$stmt->bind_result($course_code,$course_name);
		
		
		if($res){	
			$response['courses']=array();	
			while($row=$stmt->fetch()){
				$course=array();
				$course['course_code']=$course_code;
;				$course['course_name']=$course_name;
				
				array_push($response['courses'],$course);								
			}
			$response['success']=1;
			echo json_encode($response);
		}else{
			$response['success']=0;
			$response['message']="No course units found";
			echo json_encode($response);
		
		}
		
				
		
	}
}
?>