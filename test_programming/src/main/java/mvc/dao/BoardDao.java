package mvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import mvc.dbcon.Dbconn;
import mvc.vo.BoardVo;
import mvc.vo.SearchCriteria;

public class BoardDao {

	private Connection conn; // 전역적으로 연결 객체를 쓴다
	private PreparedStatement pstmt; // 쿼리를 실행하기 위한 구문 객체

	
	public BoardDao() { // 생성자를 만든다. 왜 ? DB 연결하는 Dbconn 객체 생성하려고. 생성해야 mysql 접속하니까
		Dbconn db = new Dbconn();
		this.conn = db.getConnection();
	}
	
	// 모든 게시물 보기
	public ArrayList<BoardVo> boardSelectAll(SearchCriteria scri) {
			
		int page = scri.getPage(); // 페이지 번호 
		int perPageNum = scri.getPerPageNum(); // 화면 노출 개수
			
		String str = "";
		String keyword = scri.getKeyword();
		String searchType = scri.getSearchType();
			
		// 키워드가 존재한다면 like 구문을 활용한다
		if (!scri.getKeyword().equals("")) {
			str = "AND "+searchType+" LIKE CONCAT('%','"+keyword+"','%')";
		}
			
		ArrayList<BoardVo> alist = new ArrayList<BoardVo>(); // ArrayList 컬렉션 객체에 BoardVo를 담겠다 BoardVo는 컬럼 값을 담겠다
		String sql = "SELECT *\r\n"
				+ "FROM BOARD\r\n where delyn='N'"+str+""
				+ "ORDER BY originBidx desc, depth ASC LIMIT ?, ?";
		ResultSet rs = null; // DB 값을 가져오기 위한 전용 클래스
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (page-1) * perPageNum);
			pstmt.setInt(2, perPageNum);
				
			rs = pstmt.executeQuery();
				
			while (rs.next()) { // 커서가 다음으로 이동해서 첫 글이 있느냐 물어보고 true면 진행
				int bidx = rs.getInt("bidx");
				String subject = rs.getString("subject");
				String contents = rs.getString("contents");
				String writer = rs.getString("writer");
				String writeDay = rs.getString("writeDay");
				int viewCnt = rs.getInt("viewCnt");
				int recom = rs.getInt("recom");
				int level_ = rs.getInt("level_");
					
				BoardVo bv = new BoardVo(); // 첫 행부터 bv에 옮겨 담기
				bv.setBidx(bidx);
				bv.setSubject(subject);
				bv.setContents(contents);
				bv.setWriter(writer);
				bv.setViewCnt(viewCnt);
				bv.setRecom(recom);
				bv.setWriteDay(writeDay);
				bv.setLevel_(level_);
				alist.add(bv);                // ArrayList 객체에 하나씩 추가한다
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
			
		return alist;
	}
		
	// 게시판 전체 갯수 구하기
	public int boardTotalCount(SearchCriteria scri) {
		
		String str = "";
		String keyword = scri.getKeyword();
		String searchType = scri.getSearchType();
		
		// 키워드가 존재한다면 like 구문을 활용한다
		if (!scri.getKeyword().equals("")) {
			str = "AND "+searchType+" LIKE CONCAT('%','"+keyword+"','%')";
		}
		
		int value = 0;
		// 1. 쿼리 만들기
		String sql = "SELECT COUNT(*) AS cnt\r\n"
				+ "FROM BOARD\r\n"
				+ "WHERE delyn = 'N'"+str+"";
		
		// 2. conn 객체 안에 있는 구문 클래스 호출하기
		// 3. DB 컬럼 값을 받는 전용 클래스 ResultSet 호출 (ResultSet 특징은 데이터를 그대로 복사하기 때문에 전달이 빠름)
		
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if (rs.next()) { // 커서를 이동시켜서 첫 줄로 옮긴다
				value = rs.getInt("cnt"); // 지역 변수 value에 담아서 리턴해서 가져간다
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				rs.close();
				pstmt.close();
				// conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return value;
	}	
	
	// 게시물 생성하기
	public int boardInsert(BoardVo bv) {
		int value = 0;
		int maxbidx = 0;
		
		String subject = bv.getSubject();
		String contents = bv.getContents();
		String writer = bv.getWriter();
		String password = bv.getPassword();
		String fileName = bv.getFileName();
		String ip = bv.getIp();
		
		String sql = "INSERT INTO BOARD(originBidx, depth, level_, subject, contents, writer, password, fileName, ip)\r\n"
				   + "value(null, 0, 0, ?, ?, ?, ?, ?, ?)";
		String sql2 = "update board \r\n"
	            + "set originBidx = (select * from (select max(bidx) from board) as temp) \r\n"
	            + "where bidx = (select * from (select max(bidx) from board) as temp)";
		String sql3 = "SELECT MAX(bidx) AS maxbidx FROM board WHERE originBidx=?";
		try {
			conn.setAutoCommit(false); // 수동 커밋으로 하겠다
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, subject);
			pstmt.setString(2, contents);
			pstmt.setString(3, writer);
			pstmt.setString(4, password);
			pstmt.setString(5, fileName);
			pstmt.setString(6, ip);
			int exec = pstmt.executeUpdate(); // 실행 되면 1 안되면 0
			
			pstmt = conn.prepareStatement(sql2);
			int exec2 = pstmt.executeUpdate(); // 실행 되면 1 안되면 0
			
			ResultSet rs = null;
			pstmt = conn.prepareStatement(sql3);
			pstmt.setInt(1, bv.getOriginBidx());
			rs = pstmt.executeQuery();
			
			if (rs.next() == true) {
				maxbidx = rs.getInt("maxbidx");
			}
			
			conn.commit(); // 일괄 처리 커밋
			
			// value = exec + exec2;
			
		} catch (SQLException e) {
			try {
				conn.rollback(); // 실행 중 오류 발생 시 rollback 처리 
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return maxbidx;
	}	
	
	// 특정 게시물 찾기
	public BoardVo boardSelectOne(int bidx) {
		
		// 1. 형식부터 만든다
		BoardVo bv = null;
		ResultSet rs = null;
		// 2. 사용 할 쿼리를 준비한다
		String sql = "SELECT *\r\n"
				+ "FROM BOARD \r\n"
				+ "WHERE delyn='N' and bidx = ?";
		try {
			// 3. conn 연결 객체에서 쿼리 실행 구문 클래스를 불러온다
			pstmt = conn.prepareStatement(sql); // 전역 변수로 선언한 PreparedStatement 객체로 담음
			pstmt.setInt(1, bidx); // 첫번째 물음표에 매개 변수 bidx를 담아서 구문을 완성한다
			rs = pstmt.executeQuery(); // 쿼리를 실행해서 결과값을 컬럼 전용 클래스인 ResultSet 객체에 담는다(복사 기능)
			
			if (rs.next() == true) { // rs.next()는 커서를 다음 줄로 이동시킨다. 맨 처음 커서는 상단에 위치되어있다.
				// 값이 존재 한다면 BoardVo 객체에 담는다
				String subject = rs.getString("subject");
				String contents = rs.getString("contents");
				String writer = rs.getString("writer");
				String writeDay = rs.getString("writeDay");
				int viewCnt = rs.getInt("viewCnt");
				int recom = rs.getInt("recom");
				String fileName = rs.getString("fileName");
				int rtnBidx = rs.getInt("bidx");
				int originBidx = rs.getInt("originBidx");
				int depth = rs.getInt("depth");
				int level_ = rs.getInt("level_");
				String password = rs.getString("password");
				
				bv = new BoardVo(); // 객체 생성해서 지역 변수 bv로 담아서 리턴 해서 가져간다
				
				bv.setSubject(subject);
				bv.setContents(contents);
				bv.setWriter(writer);
				bv.setWriteDay(writeDay);
				bv.setViewCnt(viewCnt);
				bv.setRecom(recom);
				bv.setFileName(fileName);
				bv.setBidx(rtnBidx);
				bv.setOriginBidx(originBidx);
				bv.setDepth(depth);
				bv.setLevel_(level_);
				bv.setPassword(password);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return bv;
	}
	
	// 게시물 수정하기
	public int boardUpdate(BoardVo bv) {
		
		int value = 0;
		String sql = "UPDATE BOARD\r\n"
				+ "SET subject=?, contents=?, writer=?, modifyDay=now()\r\n"
				+ "WHERE bidx=? and password=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bv.getSubject());
			pstmt.setString(2, bv.getContents());
			pstmt.setString(3, bv.getWriter());
			pstmt.setInt(4, bv.getBidx());
			pstmt.setString(5, bv.getPassword());
			value = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	// 조회수 업데이트하기
	public int boardViewCntUpdate(int bidx) {
		
		int value = 0;
		String sql = "UPDATE board\r\n"
				+ "SET viewCnt = viewCnt + 1 \r\n"
				+ "WHERE bidx=?";
		// 메소드를 완성시켜보세요
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bidx);
			value = pstmt.executeUpdate(); // 쿼리를 실행해서 결과값을 컬럼 전용 클래스인 ResultSet 객체에 담는다(복사 기능)
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				// conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	// 추천 수 업데이트하기
	public int boardRecomUpdate(int bidx) {
		
		int value = 0;
		int recom = 0;
		ResultSet rs = null;
		String sql = "UPDATE board\r\n"
				+ "SET recom = recom + 1 \r\n"
				+ "WHERE bidx = ?";
		String sql2 = "SELECT recom\r\n"
				+ "FROM board \r\n"
				+ "WHERE bidx=?";
		// 메소드를 완성시켜보세요
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bidx);
			value = pstmt.executeUpdate(); // 쿼리를 실행해서 결과값을 컬럼 전용 클래스인 ResultSet 객체에 담는다(복사 기능)
			
			pstmt = conn.prepareStatement(sql2);
			pstmt.setInt(1, bidx);
			rs = pstmt.executeQuery();
			
			if (rs.next() == true) {
				recom = rs.getInt("recom");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return recom;
	}
	
	// 게시물 삭제하기
	public int boardDelete(int bidx, String password) {
		
		int value = 0;
		String sql = "UPDATE board\r\n"
				+ "SET delyn = 'Y'\r\n"
				+ "WHERE bidx=? AND password=?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bidx);
			pstmt.setString(2, password);
			value = pstmt.executeUpdate(); // 성공하면 1, 실패하면 0
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	// 게시물 답변 기능
	public int boardReply(BoardVo bv) {
		
		int value = 0;
		int maxbidx = 0;
		
		String sql = "UPDATE board SET depth = depth + 1 WHERE originBidx = ? AND depth > ?";
		String sql2 = "INSERT INTO board (originBidx, depth, level_, subject, contents, writer, fileName, password, ip)"
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String sql3 = "SELECT MAX(bidx) AS maxbidx FROM board WHERE originBidx=?";
		
		try {
			conn.setAutoCommit(false); // 수동 커밋으로 하겠다
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bv.getOriginBidx());
			pstmt.setInt(2, bv.getDepth());
			int exec = pstmt.executeUpdate(); // 실행 되면 1 안되면 0
			
			pstmt = conn.prepareStatement(sql2);
			pstmt.setInt(1, bv.getOriginBidx());
			pstmt.setInt(2, bv.getDepth() + 1);
			pstmt.setInt(3, bv.getLevel_() + 1);
			pstmt.setString(4, bv.getSubject());
			pstmt.setString(5, bv.getContents());
			pstmt.setString(6, bv.getWriter());
			pstmt.setString(7, bv.getFileName());
			pstmt.setString(8, bv.getPassword());
			pstmt.setString(9, bv.getIp());
			int exec2 = pstmt.executeUpdate(); // 실행 되면 1 안되면 0
			
			ResultSet rs = null;
			pstmt = conn.prepareStatement(sql3);
			pstmt.setInt(1, bv.getOriginBidx());
			rs = pstmt.executeQuery();
			
			if (rs.next() == true) {
				maxbidx = rs.getInt("maxbidx");
			}
			
			conn.commit(); // 일괄 처리 커밋
			
			// value = exec + exec2;
			
		} catch (SQLException e) {
			try {
				conn.rollback(); // 실행 중 오류 발생 시 rollback 처리 
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try { // 각 객체를 소멸시키고 DB 연결 끊는다
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return maxbidx;
	}	
}
