package lab.web.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lab.web.domain.MemberDAO;
import lab.web.domain.MemberVO;

@WebServlet("/Member.do")
public class MemberServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	MemberDAO dao;
       
    public MemberServlet() {
        super();
    }

	public void init(ServletConfig config) throws ServletException {
		//init메서드 : 초기자원을 획득할 때 사용
		dao = new MemberDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession();
		String userid = (String) session.getAttribute("userid");
		MemberVO member = new MemberVO();
		String url = "";
		
		if(action.equals("insert")) {
			request.setAttribute("action", action);
			request.setAttribute("message", "회원 가입");
			url = "/memberform.jsp";
		} else if(action.equals("update")) {
			try {
				member = dao.selectMember(userid);
				request.setAttribute("member", member);
				request.setAttribute("action", action);
				request.setAttribute("message", "회원 정보 수정");
			} catch(RuntimeException e) {
				request.setAttribute("message", e.getMessage());
			}
			url = "/memberform.jsp";
		} else if(action.equals("delete")) {
			//delete를 액션으로 넣어준 이유 : 구조가 익숙해지라고 (페이지에 직접적으로 날려도 상관 없음)
			try {
				request.setAttribute("action", action);
			} catch(RuntimeException e) {
				request.setAttribute("message", e.getMessage());
			}
			url = "/board/memberDelete.jsp";
		}
		RequestDispatcher disp = request.getRequestDispatcher(url);
		disp.forward(request, response);
		
		
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String userid = request.getParameter("userid");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String address = request.getParameter("address");
		String url = "";
		
		MemberVO member = new MemberVO();
		member.setUserid(userid);
		member.setPassword(password);
		member.setName(name);
		member.setEmail(email);
		member.setAddress(address);
		
		if(action.equals("insert")) {
			dao.insert(member);
			request.setAttribute("message", "회원 가입 성공");
			url = "/login.jsp";
		} else if(action.equals("update")) {
			dao.updateMember(member);
			response.sendRedirect("/MVC/Board.do?action=member");
			return;
		} else if(action.equals("delete")) {
			dao.deleteMember(userid, password);
			request.setAttribute("message", "회원 정보 삭제 완료");
			HttpSession session = request.getSession();
			session.invalidate();
			url = "/login.jsp";
		}
		RequestDispatcher disp = request.getRequestDispatcher(url);
		disp.forward(request, response);
		
		
		
	}

}
