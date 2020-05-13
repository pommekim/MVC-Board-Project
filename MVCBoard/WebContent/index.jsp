<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="./css/default.css" media="screen">
<!-- href에 주소값을 써놓아야 내가 만들어놓은 css형식을 가져옴 -->
<title>첫 화면</title>
</head>
<body>
<table class="layout">
<tr height="50">
<td><jsp:include page="/incl/header.jsp" /> <!-- 헤더 페이지 집어넣기 -->
</td></tr>
<!-- 본문 표시 코드 -->
<tr height="500" valign="top">
<td><h3>게시판에 오신걸 환영합니다.</h3><br>
</td></tr>
<tr height="50">
<td><jsp:include page="/incl/footer.jsp" /> <!-- 푸터 페이지 집어넣기 -->
</td></tr>
</table>
</body>
</html>