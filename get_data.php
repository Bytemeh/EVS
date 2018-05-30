<?php 
require_once("includes\connector.php");
$response=array();
if(isset($_POST['courseCode']) && isset($_POST['program']) && isset($_POST['academicYear'])){
	if(!empty($_POST['courseCode']) && !empty($_POST['program']) &&!empty($_POST['academicYear'])){
		$courseCode=htmlentities($_POST['courseCode'],ENT_QUOTES);
		$program=htmlentities($_POST['program'],ENT_QUOTES);
		$academicYear=htmlentities($_POST['academicYear'],ENT_QUOTES);
		
		/*$courseCode=htmlentities("CSC3205");
		$program=htmlentities("Computer Science");
		$academicYear=htmlentities("2017/18");*/
		
		$query="SELECT * FROM students,registry WHERE students.student_no=registry.student_no AND students.program=? AND registry.course_code=? AND registry.academic_year=? ";
		$stmt=$conn->prepare($query);
		$stmt->bind_param("sss",$program,$courseCode,$academicYear);
		$res=$stmt->execute();
		$stmt->bind_result($student_no,$reg_no,$first_name,$last_name,$school,$program,$year_of_study,$offering_type,$account_balance,$picture,$password,$a,$b,$c,$d,$e);
		
		if($res){
			$response['students']=array();
			while($row=$stmt->fetch()){
				$student=array();
				//$student['']=
				$student['student_no']=$student_no;
				$student['reg_no']=$reg_no;
				$student['first_name']=$first_name;
				$student['last_name']=$last_name;
				$student['school']=$school;
				$student['program']=$program;
				$student['year_of_study']=$year_of_study;
				$student['offering_type']=$offering_type;
				$student['account_balance']=$account_balance;
				//$student['picture']=$picture;		
				$student['picture']=base64_encode($picture);	
				$student['a']= $a;
				$student['b']=$b;
				$student['c']=$c;
				$student['d']=$d;
				$student['e']=$e;
				//echo $b."&nbsp;";echo $c."&nbsp;";echo $d."&nbsp;";echo $e."&nbsp;";
				
				
				array_push($response['students'],$student);
				
			}
			
			$stmt->close();
			$query2="SELECT course_units.course_name,exam.exam_id FROM course_units,exam WHERE course_units.course_code=? AND exam.course_code=? AND exam.academic_year=?";
			$stmt2=$conn->prepare($query2);
			$stmt2->bind_param("sss",$courseCode,$courseCode,$academicYear);
			$res2=$stmt2->execute();
			$stmt2->bind_result($course_name,$exam_id);
			
			if($row=$stmt2->fetch()){
				$response['course_name']=$course_name;
				$response['exam_id']=$exam_id;
			}
			
			if($res2){				
				$response['success']=1;
				echo json_encode($response);
			}else{
			$response['success']=0;
			$response['message']="Query 2 failed";
			echo json_encode($response);			
			}
			
		}else{
			$response['success']=0;
			$response['message']="No results found";
			echo json_encode($response);
		}
		
	
		
		 
	}
}
?>