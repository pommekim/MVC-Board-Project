package lab.web.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class MemberDAO {

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



	//회원가입 메서드
	public void insert(MemberVO member) {
		Connection con = null;
		try {
			con = getConnection();
			String sql = "insert into member values (?, ?, ?, ?, ?)";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, member.getUserid());
			stmt.setString(2, member.getName());
			stmt.setString(3, member.getPassword());
			stmt.setString(4, member.getEmail());
			stmt.setString(5, member.getAddress());
			stmt.executeUpdate();
		} catch(SQLException e) {
			if(e.getMessage().contains("무결성")) { //or 00001 or PK
				throw new RuntimeException("아이디가 중복됩니다.");
			} else {
				e.printStackTrace();
				throw new RuntimeException("MemberDAO.insert()예외발생-콘솔확인");
			}
		} finally {
			closeConnection(con);
		}
	}

	//회원정보조회 메서드
	public MemberVO selectMember(String userid) {
		Connection con = null;
		MemberVO member = new MemberVO();
		try {
			con = getConnection();
			String sql = "select * from member where userid=?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, userid);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				member.setUserid(userid);
				member.setName(rs.getString("name"));
				member.setPassword(rs.getString("password"));
				member.setEmail(rs.getString("email"));
				member.setAddress(rs.getString("address"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("MemberDAO.selectMember예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return member;
	}
	
    //회원정보수정 메서드
	public void updateMember(MemberVO member) {
		Connection con = null;
		try {
			con = getConnection();
			String sql = "update member set name=?, password=?, email=?, address=? where userid=?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, member.getName());
			stmt.setString(2, member.getPassword());
			stmt.setString(3, member.getEmail());
			stmt.setString(4, member.getAddress());
			stmt.setString(5, member.getUserid());
			stmt.executeUpdate();
			//로그인만 제대로 되어 있다면 에러가 날 일이 없기 때문에 오류 검사를 안해도 됨
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("MemberDAO.updateMember예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}
	
	//회원비밀번호를 가져오는 메서드
	//회원정보삭제에 사용
	public String getPassword(String userid) {
		String pw = "";
		Connection con = null;
		try {
			con = getConnection();
			String sql = "select password from member where userid=?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, userid);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				pw = rs.getString("password");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("MemberDAO.getPassword예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
		return pw;
	}

	//회원정보삭제 메서드
	public void deleteMember(String userid, String password) {
		Connection con = null;
		String pw = "";
		try {
			con = getConnection();
			con.setAutoCommit(false); //오토커밋 해제 (여러 개의 쿼리문을 실행할 때)
			String sql = "select password from member where userid=?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, userid);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				pw = rs.getString("password");
			} else {
				throw new RuntimeException("아이디가 잘못 입력되었습니다.");
				
			} if(pw.equals(password)) {
				try {
					String sql2 = "delete from board where masterid in "
							+ "(select masterid from board where userid=?) and "
							+ "(replynumber>0 or userid=?)";
					//내 글은 모두 삭제 + 내가 쓴 댓글만 삭제
					stmt = con.prepareStatement(sql2);
					stmt.setString(1, userid);
					stmt.executeUpdate();
					
					String sql3 = "delete from member where userid=?";
					stmt = con.prepareStatement(sql3);
					stmt.setString(1, userid);
					stmt.executeUpdate();
					
					con.commit(); //커밋 시켜주기
				} catch(SQLException e) {
					con.rollback(); //되돌리기
					throw new RuntimeException("삭제가 되지 않았습니다: "+e.getMessage());
				}
			} else {
				throw new RuntimeException("비밀번호가 다릅니다.");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("MemberDAO.deleteMember예외발생-콘솔확인");
		} finally {
			closeConnection(con);
		}
	}



	
	
}
