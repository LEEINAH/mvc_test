package mvc.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@MultipartConfig(      // 멀티파일을 설정한다 
	      fileSizeThreshold = 1024 * 1024 * 1, // 1mb   
	      maxFileSize = 1024 * 1024 * 10, // 10mb
	      maxRequestSize = 1024 * 1024 * 15, // 15mb
	      location = "D:\\dev\\temp"// 임시로 보관하는 위치 (물리적으로 만들어놔야 한다: dev폴더로 가서 새폴더> 이름 지정하고 만들어주기)
	)

@WebServlet("/FrontController")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
	    response.setContentType("text/html;charset=UTF-8");
		
		// 전체 주소를 추출
		// uri = /member/memberJoinAction.aws
		String uri = request.getRequestURI();
		System.out.println(uri);
		
		// split으로 주소를 잘라서 배열(entity) 안에 넣기
		String[] entity = uri.split("/");
		
		if (entity[1].equals("board")) {
			BoardController bc = new BoardController(entity[2]);
			bc.doGet(request, response);
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
