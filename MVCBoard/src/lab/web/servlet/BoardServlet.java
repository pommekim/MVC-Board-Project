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
		}
		request.getRequestDispatcher(url).forward(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
