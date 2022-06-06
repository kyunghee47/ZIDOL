package com.zidol.fc.controller;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zidol.fc.domain.Board;
import com.zidol.fc.domain.User;
import com.zidol.fc.error.ErrorResponse;
import com.zidol.fc.error.board.DeleteByUnauthUserException;
import com.zidol.fc.error.board.UpdateByUnauthUserException;
import com.zidol.fc.service.BoardService;
import com.zidol.fc.service.UserService;

@RestController
public class BoardController {

	@Autowired
	BoardService boardService;

	@Autowired
	UserService userService;

	// 게시글 전체 리스트업
	@GetMapping("/find-all-board.act")
	public ResponseEntity<DataResponse> findAllBoard(@PageableDefault(page = 0, size = 10) Pageable pageable) {
		DataResponse dataResponse = new DataResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

		dataResponse.setStatus(StatusCode.OK.getStatus());
		dataResponse.setCode(StatusCode.OK.getCode());
		dataResponse.setData(boardService.findAllBoard(pageable));

		return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.OK);
	}

	// 게시글 작성
	@PostMapping("/insert-board.act")
	public ResponseEntity<DataResponse> insertBoard(@RequestBody Map<String, String> params) {
		DataResponse dataResponse = new DataResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

		User user = userService.findByUserCode(Long.parseLong(params.get("userCode")));
		Board board = Board.builder().user(user).boardType(params.get("boardType")).boardTitle(params.get("boardTitle"))
				.boardContent(params.get("boardContent")).build();

		if (boardService.insertBoard(board) != null) {
			dataResponse.setStatus(StatusCode.OK.getStatus());
			dataResponse.setCode(StatusCode.OK.getCode());
			dataResponse.setData(board);

			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.OK);
		} else {
			dataResponse.setStatus(StatusCode.NOT_FOUND.getStatus());
			dataResponse.setCode(StatusCode.NOT_FOUND.getCode());

			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.NOT_FOUND);
		}

	}

	// 게시글 수정
	@PostMapping("/board-modify.act")
	public ResponseEntity<DataResponse> boardModify(@RequestBody Map<String, String> params) {
		DataResponse dataResponse = new DataResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

		Board board = boardService.findByBoardCode(Long.parseLong(params.get("boardCode")));
		User user = board.getUser();
		
		board.setBoardTitle(params.get("boardTitle"));
		board.setBoardContent(params.get("boardContent"));

		if (Long.parseLong(params.get("userCode")) == user.getUserCode()) {
			dataResponse.setStatus(StatusCode.OK.getStatus());
			dataResponse.setCode(StatusCode.OK.getCode());
			dataResponse.setData(boardService.modifyBoard(board));

			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.OK);
		} else {
			ErrorResponse.CustomFieldError customFieldError = new ErrorResponse.CustomFieldError("Board",
					params.get("userCode"), "작성자만 글을 수정할 수 있습니다.");
			throw new UpdateByUnauthUserException(customFieldError);
		}
	}

	// 게시글 상세페이지 이동
	@GetMapping("/board-detail.act")
	public ResponseEntity<DataResponse> boardDetail(@RequestParam long boardCode) {
		DataResponse dataResponse = new DataResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

		Board board = boardService.findByBoardCode(boardCode);

		if (board != null) {
			dataResponse.setStatus(StatusCode.OK.getStatus());
			dataResponse.setCode(StatusCode.OK.getCode());
			dataResponse.setData(board);

			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.OK);
		} else {
			dataResponse.setStatus(StatusCode.NOT_FOUND.getStatus());
			dataResponse.setCode(StatusCode.NOT_FOUND.getCode());

			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.NOT_FOUND);
		}

	}

	// POST 방식으로 삭제 중
	@PostMapping("/board-detail-delete.act")
	public ResponseEntity<DataResponse> boardDelete(@RequestBody Map<String, Long> params) {
		DataResponse dataResponse = new DataResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

		Board board = boardService.findByBoardCode(params.get("boardCode"));
		User user = board.getUser();

		if (params.get("userCode") == user.getUserCode()) {
			boardService.deleteBoard(params.get("boardCode"));
			dataResponse.setStatus(StatusCode.OK.getStatus());
			dataResponse.setCode(StatusCode.OK.getCode());
			
			return new ResponseEntity<DataResponse>(dataResponse, headers, HttpStatus.OK);
		}else {
			ErrorResponse.CustomFieldError customFieldError = new ErrorResponse.CustomFieldError("Board",
					String.valueOf(params.get("userCode")), "작성자만 글을 삭제할 수 있습니다.");
			throw new DeleteByUnauthUserException(customFieldError);
		}

	}

	@PostMapping("/reply-insert")
	public Map<String, Long> insertReply(@RequestBody Map<String, Long> params) {
		Map<String, Long> result = new HashMap<>();

		return result;
	}

//	//실험 Delete로 하는것
//	@DeleteMapping("/board-detail-delete2")
//	public Board boardDelete2(@RequestParam long boardCode) {
//		Board board = boardService.findByBoardCode(boardCode);
//		return board;
//	}
//
//	// 전체 리스트업 샘플
//	@GetMapping("/read-all")
//	public ResponseEntity<DataResponse> readAllBoard(@PageableDefault(page = 0, size = 10) Pageable pageable) {
//		DataResponse dataResponse = new DataResponse();
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
//
//		dataResponse.setData(boardService.findAllBoard(pageable));
//
//		return new ResponseEntity<>(dataResponse, headers, HttpStatus.OK);
//	}

}