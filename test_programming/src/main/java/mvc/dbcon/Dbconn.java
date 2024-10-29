package mvc.dbcon;

import java.sql.Connection;
import java.sql.DriverManager;

public class Dbconn {
	
	private Connection conn; // 멤버 변수는 선언만 해도 자동 초기화가 됨
	private String url = "jdbc:mysql://127.0.0.1/hello?serverTimezone=UTC"; // 서버 주소 ?(구분자) 파라미터 값
	private String user = "root";
	private String password = "1234";
	
	public Connection getConnection() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// System.out.println("객체 생성 확인 ==> " + conn); // null값이면 연결이 되지 않았다 라는 뜻
		
		return conn; // 연결 객체가 생겨났을 때의 객체 정보를 담고 있는 객체 참조 변수            
	}
}