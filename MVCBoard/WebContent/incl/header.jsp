<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<header>
<table>
<c:choose>
	<c:when test="${!empty userid}"> <!-- 로그인이 되어있을 때 (세션에서 userid를 찾을 것임) -->
	<tr>
	<td colspan=5>${name}님 환영합니다.</td>
	</tr>
	<tr>
	<td><a href='<c:url value="/"/>'>홈으로</a></td>
	<td><a href='<c:url value="/Board.do?action=write"/>'>게시글 쓰기</a></td>
	<td><a href='<c:url value="/Board.do?action=list"/>'>게시글 목록</a></td>
	<td><a href='<c:url value="/Login.do?action=logout"/>'>로그아웃</a></td>
	<td><a href='<c:url value="/Board.do?action=member"/>'>마이페이지</a></td>
	<!-- core태그를 가지고 쓸 수 있는 것은 내 프로젝트 내에서만 가능 (컨텍스트 루트를 뺌) -->
	</tr>
	</c:when>
	<c:when test="${empty userid}"> <!-- 로그인이 되어있지 않을 때 -->
	<tr>
	<td><a href='<c:url value="/Member.do?action=insert"/>'>회원가입</a></td>
	<td><a href='<c:url value="/login.jsp"/>'>로그인</a></td>
	</tr>
	</c:when>
</c:choose>
</table>
</header>