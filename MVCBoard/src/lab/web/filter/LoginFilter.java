package lab.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter({"/board/*", "/Board.do"}) //글에 관련된 폴더(사용자의 로그인이 풀렸을 경우에도 필터에 걸칠 수 있게), 보드 서블릿
public class LoginFilter implements Filter {

    public LoginFilter() {
        
    }

	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hreq = (HttpServletRequest) request;
		HttpServletResponse hres = (HttpServletResponse) response; //sendredirect를 쓰기 위해 재정의
		HttpSession session = hreq.getSession();
		if(session.getAttribute("userid")==null) {
			//로그인이 안되어있어도 메일로 가는 것은 보내줄 수 있어야 하기 때문에 action으로 걸러줘야 함
			if(request.getParameter("action").equals("contact_do")) {
				
			} else {
				hres.sendRedirect("/MVC/login.jsp"); //로그인 페이지로 넘겨줌
				return; //밑에 체인으로 넘어가면 안되기 때문에 리턴으로 끝내줌
			}
		}
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
		
	}

}
