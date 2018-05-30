<?php 
require_once("includes\connector.php");
$response=array();

if(isset($_POST['student_no'])){
	if(!empty($_POST['student_no'])){
		$student_no=htmlentities($_POST['student_no'],ENT_QUOTES);
		//$student_no=215001201;
		$query="SELECT * FROM students WHERE student_no=?";
		$stmt=$conn->prepare($query);
		$stmt->bind_param("s",$student_no);
		$res=$stmt->execute();
		$stmt->bind_result($student_no,$reg_no,$first_name,$last_name,$school,$program,$year_of_study,$offering_type,$account_balance,$picture,$password);
		echo mysqli_error($conn);
		
		if($res){
			if($row=$stmt->fetch()){
				$response['student_no']=$student_no;
				$response['reg_no']=$reg_no;
				$response['first_name']=$first_name;
				$response['last_name']=$last_name;
				$response['school']=$school;
				$response['program']=$program;
				$response['year_of_study']=$year_of_study;
				$response['offering_type']=$offering_type;
				$response['account_balance']=$account_balance;
				$response['picture']=base64_encode($picture);
				
				$stmt->close();
			}
			$query2="SELECT * FROM registry WHERE student_no=?";
			$stmt2=$conn->prepare($query2);	
			$stmt2->bind_param("s",$student_no);
			$res2=$stmt2->execute();
			$stmt2->bind_result($student_no,$course_code,$academic_year,$semester,$retake);
			
			if($res2){
			$response['subjects']=array();
			while($row=$stmt2->fetch()){
				$subject=array();
				$subject['course_code']=$course_code;
				$subject['retake']=$retake;
				
				array_push($response['subjects'],$subject);
			}
			
			$response['success']=1;
			$response['message']="Student record found succcessfully";
			echo json_encode($response);
			
			
			
			}else{
			$response['success']=0;
			$response['message']="No registration information found";
			echo json_encode($response);
			}
			
			
		
		
		
		}else{
			$response['success']=0;
			$response['message']="Student record not found";
			echo json_encode($response);	
		}
		
	}else{
		$response['success']=0;
		$response['message']="No student number posted";
		echo json_encode($response);
	}
}
?>