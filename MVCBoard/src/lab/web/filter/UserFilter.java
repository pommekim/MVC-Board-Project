package lab.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/Board.do")
public class UserFilter implements Filter {

    public UserFilter() {
        
    }

	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hreq = (HttpServletRequest) request;
		if(request.getParameter("action").equals("update") || request.getParameter("action").equals("delete")) {
			HttpSession session = hreq.getSession();
			if(session.getAttribute("userid").equals(request.getParameter("userid"))) { //세션의 userid와 파라미터의 userid를 비교
				
			} else {
				hreq.setAttribute("message", "본인 글이 아니면 수정 또는 삭제할 수 없습니다.");
				hreq.getRequestDispatcher("/error/error/jsp").forward(hreq, response); //hreq나 request나 같은 주소값을 가지기 때문에 상관이 없음
				return;
			}
		}
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
		
	}

}
