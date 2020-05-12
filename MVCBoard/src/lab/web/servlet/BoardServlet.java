package lab.web.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lab.web.domain.BoardDAO;
import lab.web.domain.BoardVO;
import lab.web.domain.MemberDAO;

@WebServlet("/Board.do")
public class BoardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	BoardDAO dao;
	MemberDAO mdao;
       
    public BoardServlet() {
        super();
    }

	public void init(ServletConfig config) throws ServletException {
		dao = new BoardDAO();
		mdao = new MemberDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession();
		String url = "";
		if(action.equals("list")) {
			String sPage = request.getParameter("page"); //한번에 int로 안바꿔주는 이유: 글이 없을 때 null값이 나오므로 오류가 나기 때문
			int page = 1;
			if(sPage != null) {
				page = Integer.parseInt(sPage);
			}
			request.setAttribute("list", dao.selectArticleList(page)); //해당 페이지에 있는 글을 출력해서 저장해서 보내줌
			int bbsCount = dao.selectTotalBbsCount(); //게시글의 수 파악
			int totalPage = (int) Math.ceil(bbsCount/10.0); //게시글을 10으로 나눈 후 올리기 (double로 표현해줘야 제대로 출력 가능)
			request.setAttribute("totalPageCount", totalPage);
			request.setAttribute("page", page); //내가 몇 페이지에 있는지도 알려줘야 함
			url = "/board/list.jsp";
		} else if(action.equals("write")) {
			request.setAttribute("message", "새 글 입력");
			url = url + "/board/write.jsp";
			request.setAttribute("action", "write_do");
		} else if(action.equals("view")) {
			String bbsnoStr = request.getParameter("bbsno");
			int bbsno = Integer.parseInt(bbsnoStr);
			BoardVO board = dao.selectArticle(bbsno);
			dao.updateReadCount(bbsno);
			if(board.getContent() != null) {
				board.setContent(board.getContent().replaceAll("\n", "<br>")); //출력을 할 때 html형식으로 바꾸어주어야 함
			}
			request.setAttribute("board", board);
			request.setAttribute("message", "글 상세보기");
			url = url + "/board/view.jsp";
		} else if(action.equals("reply")) {
			String bbsno = request.getParameter("bbsno");
			BoardVO board = dao.selectArticle(Integer.parseInt(bbsno));
			board.setSubject("[re]"+board.getSubject());
			board.setContent(board.getContent()+"\n----------------\n");
			request.setAttribute("board", board);
			request.setAttribute("message", "댓글 입력");
			request.setAttribute("action", "reply_do"); //액션이 따로 들어감!!!
			url = url + "/board/write.jsp";
		} else if(action.equals("update")) {
			String bbsnoStr = request.getParameter("bbsno");
			int bbsno = Integer.parseInt(bbsnoStr);
			BoardVO board = dao.selectArticle(bbsno);
			request.setAttribute("board", board);
			request.setAttribute("message", "글 수정 화면");
			request.setAttribute("action", "update_do"); //액션이 따로 들어감!!!
			url = url + "/board/write.jsp";
		} else if(action.equals("delete")) {
			String bbsnoStr = request.getParameter("bbsno");
			String replynoStr = request.getParameter("replynumber");
			request.setAttribute("bbsno", bbsnoStr);
			request.setAttribute("replynumber", replynoStr);
			request.setAttribute("action", "delete_do");
			url = url + "/board/delete.jsp";
		}
		request.getRequestDispatcher(url).forward(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String url = "";
		HttpSession session = request.getSession();
		if(action.equals("write_do")) {
			String userId = (String) session.getAttribute("userid");
			String password = request.getParameter("password");
			String subject = request.getParameter("subject");
			String content = request.getParameter("content");
			
			BoardVO board = new BoardVO();
			board.setUserId(userId);
			board.setPassword(password);
			board.setContent(content);
			board.setSubject(subject);
			
			dao.insertArticle(board);
			
			url = "/MVC/Board.do?action=list";
			response.sendRedirect(url);
			return;
		} else if(action.equals("reply_do")) {
			String userid = (String) session.getAttribute("userid");
			String subject = request.getParameter("subject");
			String content = request.getParameter("content");
			String password = request.getParameter("password");
			int bbsno = Integer.parseInt(request.getParameter("bbsno"));
			int masterid = Integer.parseInt(request.getParameter("masterid"));
			int replynumber = Integer.parseInt(request.getParameter("replynumber"));
			int replystep = Integer.parseInt(request.getParameter("replystep"));
			
			BoardVO board = new BoardVO();
			board.setBbsno(bbsno);
			board.setUserId(userid);
			board.setSubject(subject);
			board.setContent(content);
			board.setPassword(password);
			board.setMasterId(masterid);
			board.setReplyNumber(replynumber);
			board.setReplyStep(replystep);
			
			dao.replyArticle(board);
			
			response.sendRedirect("/MVC/Board.do?action=list");
			return;
		} else if(action.equals("update_do")) {
			String password = request.getParameter("password");
			String bbsnoStr = request.getParameter("bbsno");
			int bbsno = Integer.parseInt(bbsnoStr);
			String dbpw = dao.getPassword(bbsno);
			if(dbpw.equals(password)) { //수정은 비밀번호를 확인해야 하기 때문에 한번 더 조건을 걸어줌
				BoardVO board = new BoardVO();
				board.setBbsno(bbsno);
				board.setSubject(request.getParameter("subject"));
				board.setContent(request.getParameter("content"));
				dao.updateArticle(board);
				url = "/MVC/Board.do?action=view&bbsno="+bbsno;
				response.sendRedirect(url);
				return;
			} else {
				request.setAttribute("message", "비밀번호가 다릅니다. 수정되지 않았습니다.");
				url = url + "/error/error.jsp";
			}
		} else if(action.equals("delete_do")) {
			//파라미터를 직접 보냄 (board.이 아님)
			String password = request.getParameter("password");
			int bbsno = (Integer.parseInt(request.getParameter("bbsno")));
			int replynumber = (Integer.parseInt(request.getParameter("replynumber")));
			String dbpw = dao.getPassword(bbsno);
			if(dbpw.equals(password)) {
				dao.deleteArticle(bbsno, replynumber);
				url = "/MVC/Board.do?action=list";
				response.sendRedirect(url);
				return;
			} else {
				request.setAttribute("message", "비밀번호가 다릅니다. 삭제할 수 없습니다.");
				url = url + "/error/error.jsp";
			}
		}
		request.getRequestDispatcher(url).forward(request, response);
		
	}

}
