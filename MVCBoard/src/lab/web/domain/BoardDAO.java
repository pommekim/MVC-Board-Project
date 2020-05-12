package lab.web.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {
	
	static {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		} catch(SQLException e) {
			e.printStackTrace();
			System.out.println("드라이버 로드 실패");
		}
	}
	
	private Connection getConnection() {
		DataSource ds = null;
		Connection con = null;
		try {
			Context ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Oracle");
			con = ds.getConnection();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return con;
	}
	
	private void closeConnection(Connection con) {
		if(con != null) {
			try {con.close();} catch(SQLException e) {}
		}
	}
	
	
	
	//글을 삽입하기 위한 메서드
	public void insertArticle(BoardVO board) {
		Connection con = null;
		String sql1 = "select nvl(max(bbsno), 0) from board";
		int bbsno = 0;
		String sql2 = "insert into board (bbsno, userid, password, subject, "
				+ "content, writedate, masterid, readcount, replynumber, replystep) "
				+ "values (?,?,?,?,?,SYSDATE,?,0,0,0)";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql1);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			bbsno = rs.getInt(1)+1;
			
			stmt = con.prepareStatement(sql2);
			stmt.setInt(1, bbsno);
			stmt.setString(2, board.getUserId());
			stmt.setString(3, board.getPassword());
			stmt.setString(4, board.getSubject());
			stmt.setString(5, board.getContent());
			stmt.setInt(6, bbsno);
			stmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.insertArticle예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//글 목록을 가져오는 메서드
	public Collection<BoardVO> selectArticleList(int page) {
		Connection con = null;
		ArrayList<BoardVO> list = new ArrayList<>();
		String sql = "select bbsno, name, subject, writedate, readcount, rnum "
				+ "from (select bbsno, name, subject, writedate, readcount, rownum as rnum "
				+ "from (select bbsno, name, subject, writedate, readcount "
				+ "from board b "
				+ "join member m on b.userid=m.userid "
				+ "order by masterid desc, replynumber, replystep)) " //어떤 기준으로 정렬하고 있는가!!!
				+ "where rnum between ? and ?"; //rnum을 몇 번부터 몇 번까지 정의할 것인가
		int start = (page-1) * 10 + 1;
		int end = start + 9;
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, start);
			stmt.setInt(2, end);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				BoardVO board = new BoardVO();
				board.setBbsno(rs.getInt("bbsno"));
				board.setName(rs.getString("name"));
				board.setSubject(rs.getString("subject"));
				board.setWriteDate(rs.getDate("writedate"));
				board.setReadCount(rs.getInt("readcount"));
				list.add(board);
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.selectArticleList예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return list;
	}
	
	//글의 상세내용을 보여주기 위해 게시글 한개만 가져오는 메서드
	public BoardVO selectArticle(int bbsno) {
		Connection con = null;
		BoardVO board = null;
		String sql = "select bbsno, name, b.userid, subject, content, readcount, writedate, "
				+ "masterid, replynumber, replystep "
				+ "from board b join member m "
				+ "on b.userid=m.userid where bbsno=?";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, bbsno);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				board = new BoardVO();
				board.setBbsno(rs.getInt("bbsno"));
				board.setName(rs.getString("name"));
				board.setUserId(rs.getString("userid"));
				board.setSubject(rs.getString("subject"));
				board.setContent(rs.getString("content"));
				board.setReadCount(rs.getInt("readcount"));
				board.setWriteDate(rs.getDate("writedate"));
				board.setMasterId(rs.getInt("masterid"));
				board.setReplyNumber(rs.getInt("replynumber"));
				board.setReplyStep(rs.getInt("replystep"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.selectArticle예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return board;
	}
	
	//글을 클릭할 경우 조회수를 늘리기 위한 메서드
	public void updateReadCount(int bbsno) {
		Connection con = null;
		String sql = "update board set readcount=readcount+1 where bbsno=?";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, bbsno);
			stmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.updateReadCount예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//글 삭제시 비밀번호를 체크할 메서드
	public String getPassword(int bbsno) {
		Connection con = null;
		String password = "";
		String sql = "select password from board where bbsno=?";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, bbsno);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				password = rs.getString("password");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.getPassword예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return password;
	}
	
	//댓글을 달 경우 답변글을 작성하는 메서드
	public void replyArticle(BoardVO board) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			con.setAutoCommit(false);
			
			String sql1 = "update board set replynumber=replynumber+1 where masterid=? and replynumber>?";
			//1. 나한테 달려있던 댓글의 순서를 일단 바꿔주기 (기존 댓글 번호에 +1)
			stmt = con.prepareStatement(sql1);
			stmt.setInt(1, board.getMasterId());
			stmt.setInt(2, board.getReplyNumber());
			stmt.executeUpdate();
			
			String sql2 = "select max(bbsno) from board";
			//2. 댓글은 원본 글이 없으면 못 달리므로 nvl을 빼버린거임
			stmt = con.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if(rs.next()) {
				board.setBbsno(rs.getInt(1)+1);
			}
			
			String sql3 = "insert into board values (?,?,?,?,?,SYSDATE,?,0,?,?)";
			//3. 댓글 삽입
			stmt = con.prepareStatement(sql3);
			stmt.setInt(1, board.getBbsno());
			stmt.setString(2, board.getUserId());
			stmt.setString(3, board.getPassword());
			stmt.setString(4, board.getSubject());
			stmt.setString(5, board.getContent());
			stmt.setInt(6, board.getMasterId());
			stmt.setInt(7, board.getReplyNumber()+1); //기존 글보다 +1을 해줘야 댓글로 표현 가능
			stmt.setInt(8, board.getReplyStep()+1);
			stmt.executeUpdate();
			
			con.commit(); //3개의 쿼리가 모두 정상 실행되면 커밋!
		} catch(Exception e) {
			try {
				con.rollback(); //정상 실행이 안되면 롤백!
			} catch(SQLException e1) {}
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.replyArticle예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//글과 답변 모두를 삭제하는 메서드
	public void deleteArticle(int bbsno, int replynumber) { //replynumber를 받아서 댓글인지 아닌지 여부를 가림
		String sql = "";
		Connection con = null;
		try {
			con = getConnection();
			if(replynumber>0) { //0보다 크면 무조건 댓글
				sql = "delete from board where bbsno=?";
			} else { //원본 글
				sql = "delete from board where masterid=?";
			}
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, bbsno);
			stmt.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.deleteArticle예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//글의 총 갯수를 세어주는 메서드
	public int selectTotalBbsCount() {
		Connection con = null;
		String sql = "select count(bbsno) from board";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int bbsCount = rs.getInt(1);
			return bbsCount;
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.selectTotalBbsCount예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//글을 수정하는 메서드
	public void updateArticle(BoardVO board) {
		Connection con = null;
		String sql = "update board set subject=?, content=?, writedate=SYSDATE where bbsno=?";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, board.getSubject());
			stmt.setString(2, board.getContent());
			stmt.setInt(3, board.getBbsno());
			stmt.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.updateArticle예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//마이페이지에서 내가 쓴 글이 총 몇개인지를 확인하는 메서드
	public int selectCount(String userid) {
		Connection con = null;
		String sql = "select count(bbsno) from board where userid=?";
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, userid);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			return count;
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.selectCount예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//내가 쓴 글들만 가져오는 메서드
	public Collection<BoardVO> memberList(String userid, int page) {
		Connection con = null;
		String sql = "select rnum, bbsno, name, subject, readcount, writedate "
				+ "from (select rownum rnum, bbsno, name, subject, readcount, writedate "
				+ "from (select bbsno, name, subject, readcount, writedate "
				+ "from board b "
				+ "join member m"
				+ "on b.userid=m.userid "
				+ "where b.userid=? order by bbsno desc)) "
				+ "where rnum between ? and ?";
		
		ArrayList<BoardVO> list = new ArrayList<>();
		int start = (page-1) * 20 + 1;
		int end = start + 19;
		try {
			con = getConnection();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, userid);
			stmt.setInt(2, start);
			stmt.setInt(3, end);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				BoardVO board = new BoardVO();
				board.setBbsno(rs.getInt("bbsno"));
				board.setName(rs.getString("name"));
				board.setWriteDate(rs.getDate("writedate"));
				board.setSubject(rs.getString("subject"));
				board.setReadCount(rs.getInt("readcount"));
				list.add(board);
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BoardDAO.memberList예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return list;
	}
	
	
	
	

}
