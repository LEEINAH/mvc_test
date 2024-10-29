package mvc.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mvc.dao.BoardDao;
import mvc.vo.BoardVo;
import mvc.vo.PageMaker;
import mvc.vo.SearchCriteria;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;

@WebServlet("/BoardController")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String location; // 전역 변수 초기화 => 이동할 페이지
	
	public BoardController(String location) {
		this.location = location;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String paramMethod="";  //전송 방식이 sendRedirect면 S   forward방식이면 F
		String url="";
		
		if (location.equals("boardList.aws")) { // 가상경로 (location이 boardList.aws 이면 실행)
			// System.out.println("boardList");
			String page = request.getParameter("page");
			if (page == null) {page = "1";	}
			int pageInt = Integer.parseInt(page);
			
			
			String searchType = request.getParameter("searchType");
			String keyword = request.getParameter("keyword");
			if (keyword == null) {keyword = "";}
			
			SearchCriteria scri = new SearchCriteria();
			scri.setPage(pageInt);
			scri.setSearchType(searchType);
			scri.setKeyword(keyword);
			
			PageMaker pm = new PageMaker();
			pm.setScri(scri);                                            // <----------- pageMaker에 SearchCriteria 담아서 가지고 다닌다
			
			BoardDao bd = new BoardDao();
			// System.out.println("bd" + bd);
			
			// 페이징 처리하기 위한 전체 데이터 갯수 가져오기
			int boardCnt = bd.boardTotalCount(scri);
			// System.out.println("게시물 수는? : " + boardCnt);         // <----------- pageMaker에 전체 게시물 수를 담아서 페이지 계산
			pm.setTotalCount(boardCnt);
			
			ArrayList<BoardVo> alist = bd.boardSelectAll(scri);
			// System.out.println("alist ==> " + alist); // 객체 주소가 나오면 객체가 생성된 것을 짐작 할 수 있다
			
			request.setAttribute("alist", alist); // 화면까지 가지고 가기 위해 request 객체에 담는다
			request.setAttribute("pm", pm);       // forward 방식으로 넘기기 때문에 공유가 가능하다
			
			paramMethod = "F";
			url = "/board/boardList.jsp"; // 실제 내부경로
			
		} else if (location.equals("boardWrite.aws")) { // 가상경로 (location이 boardWrite.aws 이면 실행)
			
			paramMethod = "F"; // 포워드 방식은 내부에서 공유하는 것이기 때문에 내부에서 활동하고 이동한다 
			url = "/board/boardWrite.jsp"; // 실제 내부경로
			
		} else if (location.equals("boardWriteAction.aws")) {
	         // System.out.println("boardWriteAction.aws");
	         // 1. 파라미터 값을 넘겨 받는다

			 // 저장되는 경로
	         String savePath = "D:\\dev\\new-eclips-workspace\\test_programming\\src\\main\\webapp\\images\\"; 
	         System.out.println("savePath" + savePath); // 찍어보기

	         // 업로드 되는 파일 사이즈
	         int fsize = (int) request.getPart("filename").getSize();
	         System.out.println("fsize생성된지 확인 : " + fsize); // 찍어보기

	         // 원본 파일 이름
	         String originFileName = "";
	         if (fsize != 0) {

	            Part filePart = (Part) request.getPart("filename"); // 넘어온 멀티파트 형식의 파일을 Part 클래스로 담는다
	            System.out.println("filePart==> " + filePart); // 찍어보기
	            originFileName = getFileName(filePart);
	            System.out.println("originFileName ==> " + originFileName); // 찍어보기
	            System.out.println("저장되는 위치  ==> " + savePath + originFileName); // 찍어보기

	            File file = new File(savePath + originFileName); // 파일 객체 생성
	            InputStream is = filePart.getInputStream(); // 파일 읽어들이는 스트림 생성
	            FileOutputStream fos = null;
	            
	            fos = new FileOutputStream(file); // 파일 작성 및 완성하는 스트림 생성
	            
	            int temp = -1;
	            

				while ((temp = is.read()) != -1) {   //반복문을 돌려서 읽어드린 데이터를 output에 작성한다
					fos.write(temp);
					} 
				is.close();   //input 스트림 객체 소명
				fos.close(); //Output 스트림 객체소명          
	         } else {
	        	 originFileName = "";
	         }
			
			// 1. 파라미터 값을 넘겨 받는다
			String subject = request.getParameter("subject");
			String contents = request.getParameter("contents");
			String writer = request.getParameter("writer");
			String password = request.getParameter("password");
			
			// String ip = request.getRemoteAddr();
			String ip = "";
			try {		  			
				  ip = getUserIp(request);						
			  } catch (Exception e) {			
				e.printStackTrace();
			 }
			
			BoardVo bv = new BoardVo();
			bv.setSubject(subject);
			bv.setContents(contents);
			bv.setWriter(writer);
			bv.setPassword(password);
			bv.setFileName(originFileName);
			bv.setIp(ip);
			
			// 2. DB 처리한다
			BoardDao bd = new BoardDao();
			int maxbidx = bd.boardReply(bv);
			
			paramMethod = "S";
			if (maxbidx != 0) {
				url = request.getContextPath() + "/board/boardContents.aws?bidx=" + maxbidx;
			} else {
				url = request.getContextPath() + "/board/boardWrite.aws";
			}
			
		} else if (location.equals("boardContents.aws")) {
			System.out.println("boardContents.aws");
			
			// 1. 넘어온 값 받기
			String bidx = request.getParameter("bidx");
			System.out.println("bidx --> " + bidx);
			int bidxInt = Integer.parseInt(bidx); // boardSelectOne에 매개 변수로 넣어주기 위해 문자형으로 넘어온 bidx를 숫자로 변환시켜준다 
			
			// 2. 처리하기
			BoardDao bd = new BoardDao(); // 객체 생성
			bd.boardViewCntUpdate(bidxInt);
			BoardVo bv = bd.boardSelectOne(bidxInt); // 생성한 메소드 호출 (해당되는 bidx의 게시물 데이터 가져옴)
			
			request.setAttribute("bv", bv); // 포워드 방식이라 같은 영역 안에 있기 때문에 공유해서 jsp 페이지에서 꺼내 쓸 수 있다
			
			// 3. 이동해서 화면 보여주기
			paramMethod = "F"; // 화면을 보여주기 위해서 같은 영역 내부 안에 jsp 페이지를 보여준다
			url = "/board/boardContents.jsp";
			
		} else if (location.equals("boardModify.aws")) {
			System.out.println("boardModify.aws");
			
			String bidx = request.getParameter("bidx");
			int bidxInt = Integer.parseInt(bidx); // boardSelectOne에 매개 변수로 넣어주기 위해 문자형으로 넘어온 bidx를 숫자로 변환시켜준다
			
			BoardDao bd = new BoardDao();  // 객체 생성
			BoardVo bv = bd.boardSelectOne(bidxInt);
			
			request.setAttribute("bv", bv);
			
			paramMethod = "F"; // 포워드 방식은 내부에서 공유하는 것이기 때문에 내부에서 활동하고 이동한다 
			url = "/board/boardModify.jsp"; // 실제 내부경로
			
		} else if (location.equals("boardModifyAction.aws")) {
			System.out.println("boardModifyAction.aws");
			
			// 1. 파라미터 값을 넘겨 받는다
			String subject = request.getParameter("subject");
			String contents = request.getParameter("contents");
			String writer = request.getParameter("writer");
			String password = request.getParameter("password");
			String bidx = request.getParameter("bidx");
			int bidxInt = Integer.parseInt(bidx); // boardSelectOne에 매개 변수로 넣어주기 위해 문자형으로 넘어온 bidx를 숫자로 변환시켜준다
			
			BoardDao bd = new BoardDao();  // 객체 생성
			BoardVo bv = bd.boardSelectOne(bidxInt);
			
			// 2. 처리하기
			
			paramMethod = "S";
			// 비밀번호 체크
			if (password.equals(bv.getPassword())) {
				// 비밀번호가 같으면
				BoardDao bd2 = new BoardDao();
				BoardVo bv2 = new BoardVo();
				
				bv2.setSubject(subject);
				bv2.setContents(contents);
				bv2.setWriter(writer);
				bv2.setPassword(password);
				bv2.setBidx(bidxInt);
								
				int value = bd2.boardUpdate(bv2);
				
				if (value == 1) {
					url = request.getContextPath() + "/board/boardContents.aws?bidx=" + bidx;
				} else {
					url = request.getContextPath() + "/board/boardModify.aws?bidx=" + bidx;
				}		
			} else {
			    // 비밀번호가 다르면
				response.setContentType("text/html; charset=UTF-8");  // 응답 콘텐츠 타입 설정
			    PrintWriter out = response.getWriter();  // PrintWriter 객체 가져오기
				
			    out.println("<script>");
			    out.println("alert('비밀번호가 다릅니다.');");
			    out.println("history.back();");
			    out.println("</script>");
			    out.flush();  // 버퍼에 남아 있는 데이터를 클라이언트로 전송
			}
		} else if (location.equals("boardRecom.aws")) {
			 
			String bidx = request.getParameter("bidx");
			int bidxInt = Integer.parseInt(bidx);
			
			BoardDao bd = new BoardDao();
			int recom = bd.boardRecomUpdate(bidxInt);
			
			PrintWriter out = response.getWriter();
			out.println("{\"recom\":\"" + recom + "\"}");

		} else if (location.equals("boardDelete.aws")) {
			
			String bidx = request.getParameter("bidx");
			
			request.setAttribute("bidx", bidx);
			
			paramMethod = "F";
			url = "/board/boardDelete.jsp";
			
		} else if (location.equals("boardDeleteAction.aws")) {
			
			String bidx = request.getParameter("bidx");
			String password = request.getParameter("password");
			
			// 처리하기
			BoardDao bd = new BoardDao();
			int value = bd.boardDelete(Integer.parseInt(bidx), password); // value = 성공하면 1, 실패하면 2
			
			paramMethod = "S";
			
			if (value == 1) {
				url = request.getContextPath() + "/board/boardList.aws";
			} else {
				url = request.getContextPath() + "/board/boardDelete.aws?bidx=" + bidx;
			}
		} else if (location.equals("boardReply.aws")) {
			
			String bidx = request.getParameter("bidx");
			
			BoardDao bd = new BoardDao();  // 객체 생성
			BoardVo bv = bd.boardSelectOne(Integer.parseInt(bidx)); // Selectone 메소드를 이용해서 해당 bidx의 게시글을 뽑아낸다
			
			int originBidx = bv.getOriginBidx(); // 뽑아낸 게시글의 originBidx, depth, level_을 각 변수에 담는다
			int depth = bv.getDepth();
			int level_ = bv.getLevel_();
			
			request.setAttribute("bidx", Integer.parseInt(bidx)); // 담은 변수를 request.setAttribute에 담고 가져간다
			request.setAttribute("originBidx", originBidx);
			request.setAttribute("depth", depth);
			request.setAttribute("level_", level_);
			
			paramMethod = "F";
			url = "/board/boardReply.jsp";
		} else if (location.equals("boardReplyAction.aws")) {
			
			 System.out.println("boardReplyAction.aws");
			
			 // 저장되는 경로
	         String savePath = "D:\\dev\\new-eclips-workspace\\test_programming\\src\\main\\webapp\\images\\"; 
	         System.out.println("savePath" + savePath); // 찍어보기

	         // 업로드 되는 파일 사이즈
	         int fsize = (int) request.getPart("filename").getSize();
	         System.out.println("fsize생성된지 확인 : " + fsize); // 찍어보기

	         // 원본 파일 이름
	         String originFileName = "";
	         if (fsize != 0) {

	            Part filePart = (Part) request.getPart("filename"); // 넘어온 멀티파트 형식의 파일을 Part 클래스로 담는다
	            // System.out.println("filePart==> " + filePart); // 찍어보기
	            originFileName = getFileName(filePart);
	            // System.out.println("originFileName ==> " + originFileName); // 찍어보기
	            // System.out.println("저장되는 위치  ==> " + savePath + originFileName); // 찍어보기

	            File file = new File(savePath + originFileName); // 파일 객체 생성
	            InputStream is = filePart.getInputStream(); // 파일 읽어들이는 스트림 생성
	            FileOutputStream fos = null;
	            
	            fos = new FileOutputStream(file); // 파일 작성 및 완성하는 스트림 생성
	            
	            int temp = -1;
	            

				while ((temp = is.read()) != -1) {   //반복문을 돌려서 읽어드린 데이터를 output에 작성한다
					fos.write(temp);
					} 
				is.close();   //input 스트림 객체 소명
				fos.close(); //Output 스트림 객체소명          
	         } else {
	        	 originFileName = "";
	         }
			
			// 1. 파라미터 값을 넘겨 받는다
			String subject = request.getParameter("subject");
			String contents = request.getParameter("contents");
			String writer = request.getParameter("writer");
			String password = request.getParameter("password");
			String bidx = request.getParameter("bidx");
			String originBidx = request.getParameter("originBidx");
			System.out.println("boardReplyAction originBidx : " + originBidx);
			String depth = request.getParameter("depth");
			String level_ = request.getParameter("level_");
			
			String ip = "";
			try {		  			
				  ip = getUserIp(request);						
			  } catch (Exception e) {			
				e.printStackTrace();
			 }
			
			BoardVo bv = new BoardVo();
			bv.setSubject(subject);
			bv.setContents(contents);
			bv.setWriter(writer);
			bv.setPassword(password);
			bv.setFileName(originFileName); // 파일 이름 DB 컬럼 추가
			bv.setBidx(Integer.parseInt(bidx));
			bv.setOriginBidx(Integer.parseInt(originBidx));
			bv.setDepth(Integer.parseInt(depth));
			bv.setLevel_(Integer.parseInt(level_));
			bv.setIp(ip);
			
			BoardDao bd = new BoardDao();
			int maxbidx = bd.boardReply(bv);
			
			paramMethod = "S";
			if (maxbidx != 0) {
				url = request.getContextPath() + "/board/boardContents.aws?bidx=" + maxbidx;
			} else {
				url = request.getContextPath() + "/board/boardReply.aws?bidx=" + bidx;
			}			
		} else if (location.equals("boardDownload.aws")) {
			System.out.println("boardDownload.aws");
			
			String filename = request.getParameter("filename");
			String savePath = "D:\\dev\\new-eclips-workspace\\test_programming\\src\\main\\webapp\\images\\"; 
			
			ServletOutputStream sos = response.getOutputStream();
			
			String downfile = savePath+filename;
			
			File f = new File(downfile);
			
			String header = request.getHeader("User-Agent");
			
			String fileName = "";
			response.setHeader("Cache-Control", "no-cache");
			if (header.contains("Chrome")) {
				
				fileName = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
		        response.setHeader("Content-disposition", "attachment;fileName="+fileName);
		        
			} else if (header.contains("MSIE") || header.contains("Trident") || header.contains("Edge")) {
				
				fileName = URLEncoder.encode(filename, "UTF-8").replace("\\+", "%20");
				response.setHeader("Content-disposition", "attachment;fileName="+fileName);
				
			} else {
				
		        response.setHeader("Content-disposition", "attachment;fileName="+filename);
		        
			}
			
			FileInputStream in = new FileInputStream(f); // 파일을 버퍼로 읽어봐서 출력한다
			
			byte[] buffer = new byte[1024*1024*8];
			
			while(true) {
				int count = in.read(buffer);
				
				if (count == -1) {
					break;
				}
				sos.write(buffer, 0, count);
			}
			
			in.close();
			sos.close();
		}

		
		if (paramMethod.equals("F")) {		
			RequestDispatcher rd  =request.getRequestDispatcher(url);  
			rd.forward(request, response); 				
		} else if (paramMethod.equals("S")) {
			response.sendRedirect(url);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public String getFileName(Part filePart) { // 메서드 하나 더 생성 첨부파일 업로드 하기 위한

	      for (String filePartData : filePart.getHeader("Content-Disposition").split(";")) {
	         System.out.println(filePartData);

	         if (filePartData.trim().startsWith("filename")) {
	            return filePartData.substring(filePartData.indexOf("=") + 1).trim().replace("\"", "");
	         }
	      }
	      return null;

	}
	
	public String getUserIp(HttpServletRequest request) throws Exception {
		
		  String ip = null;
		  //HttpServletRequest request = 
		  //((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();

		  ip = request.getHeader("X-Forwarded-For");
	        
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    	  ip = request.getHeader("Proxy-Client-IP"); 
	      } 
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("WL-Proxy-Client-IP"); 
	      } 
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("HTTP_CLIENT_IP"); 
	      } 
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
	      }
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("X-Real-IP"); 
	      }
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("X-RealIP"); 
	      }
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getHeader("REMOTE_ADDR");
	      }
	      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	          ip = request.getRemoteAddr(); 
	      }
	      
	      if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
	    	  InetAddress address = InetAddress.getLocalHost();
	    	  ip = address.getHostAddress();
	      }
	      
	    return ip;
	}
}
