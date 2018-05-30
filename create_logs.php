<?php 
require_once("includes\connector.php");
$response=array();
if (isset($_POST['logs']) && isset($_POST['exam_id'])){
	if(!empty($_POST['logs']) && !empty($_POST['exam_id'])){
		//$what='[{"student_no":215001,"timestamp":2000000},{"student_no":"300m","timestamp":"30b00000"}]';
		$log_array=json_decode($_POST['logs'], true);
		$exam_id=htmlentities($_POST['exam_id'],ENT_QUOTES);
		
		$query="INSERT INTO sittings(student_no,exam_id,timestamp) VALUES (?,?,?)";
		$stmt=$conn->prepare($query);
		
		
				
		foreach($log_array as $key=>$value){
			$std_no= $value['student_no'];
			//$timestamp= $value['timestamp'];
			$timestamp=date("Y-m-d H:i:s",$value['timestamp']);
			$stmt->bind_param("sss",$std_no,$exam_id,$timestamp);
			$stmt->execute();			
			
		}
		
		$response['success']=1;
		$response['message']="Student no is ".$std_no." Timestamp is ".$timestamp;
		
		echo json_encode($response);
	
	}
	else{
		$response['success']=0;
		$response['message']="No log data posted";
		
		echo json_encode($response);
	}
}
?>