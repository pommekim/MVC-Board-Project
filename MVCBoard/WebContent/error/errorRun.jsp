<%@ page language="java" contentType="text/html; charset=UTF-8" isErrorPage="true"
    pageEncoding="UTF-8"%>
    <!-- isErrorPage를 true로 설정해둬야 exception이 있다고 간주함!!! -->
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>에러 페이지</title>
</head>
<body>
<script type="text/javascript">
alert("<%=exception.getMessage()%>");
history.back(); //전 페이지로 이동
//location.링크 걸어주면 원하는 장소로 이동
</script>
</body>
</html>