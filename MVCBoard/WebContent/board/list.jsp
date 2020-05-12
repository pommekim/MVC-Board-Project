<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ElCeil" uri="/WEB-INF/tlds/el-function.tld"%>
<%-- 우리가 만든 메서드를 사용하기 위해서 써주어야 함 --%>
<%-- uri에는 우리가 만든 tld파일을 적어줘야 함! --%>
<%-- 이제 우리가 만든 메서드를 prefix에 넣은 이름으로 호출 가능 --%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="./css/default.css" media="screen">
<title>게시글 목록</title>
</head>
<body>
<table class="layout">
<tr height="50">
<td><jsp:include page="/incl/header.jsp" />
</td></tr>
<tr height="500" valign="top"><td>
<h3>게시판 목록입니다.</h3>
<table>
<c:forEach var="board" items="${list}">
<tr>
<td>${board.name}</td> <%-- 이름 --%>
<td><a href='<c:url value="/Board.do?action=view&bbsno=${board.bbsno}"/>'>${board.subject}</a></td> <%-- 게시글 번호 --%>
<td>${board.writeDate}</td> <%-- 작성일 --%>
<td>${board.readCount}</td> <%-- 조회수 --%>
</tr>
</c:forEach>

<tr>
<td colspan=4> <%-- 한칸짜리를 4칸의 크기로 만들겠다는 의미 --%>
<h6 align="center"> <%-- 가운데 정렬 --%>
<c:set var="totalPageBlock" value="${ElCeil:ElCeil(totalPageCount/10.0)}" /> <%-- 출력하는데 특화되어 있기 때문에 기본 연산 가능 (10으로 써도 무방) --%>
<c:set var="nowPageBlock" value="${ElCeil:ElCeil(page/10.0)}" /> <%-- 현재 페이지 블록이 어딘지를 계산해둬야 편해짐 --%>
<c:set var="startPage" value="${(nowPageBlock-1)*10+1}" /> <%-- start페이지 계산 --%>
<c:choose> <%-- end페이지는 유동적으로 바꿔지게 계산해야 함, if문 --%>
	<c:when test="${totalPageCount gt nowPageBlock*10}"> <%-- total페이지가 now페이지에 10을 곱한것보다 클때 --%>
		<c:set var="endPage" value="${nowPageBlock*10}" />
	</c:when>
	<c:otherwise> <%-- 반대로 작거나 같을 때 --%>
		<c:set var="endPage" value="${totalPageCount}" />
	</c:otherwise>
</c:choose> 

<%-- 계산은 끝났고 출력하는 부분 --%>
<c:if test="${nowPageBlock gt 1}"> <%-- now페이지블록이 1보다 크다면 --%>
<a href="Board.do?action=list&page=${startPage-1}]">◀</a> <%-- 전 블록 끝페이지로 넘어감 --%>
</c:if>

<c:forEach var="i" begin="${startPage}" end="${endPage}">
[<a href="Board.do?action=list&page=${i}">${i}</a>]
</c:forEach>

<c:if test="${nowPageBlock lt totalPageBlock}"> <%-- now블록이 total블록보다 작으면 --%>
<a href="Board.do?action=list&page=${endPage+1}]">▶</a>
</c:if>

</h6>
</table>
</td></tr>

<tr height="50">
<td><jsp:include page="/incl/footer.jsp" />
</td></tr>
</table>
</body>
</html>