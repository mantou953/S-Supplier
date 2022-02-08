package com.xbz.web.vo;

public class ResponseResult<T> implements java.io.Serializable {

	private static final long serialVersionUID = 2817427100444859803L;

	public static final int FAIL_STATUS = 400;

	public static final int SUCC_STATUS = 200;

	public static final String FAIL_MESSAGE = "请求失败";

	public static final String SUCC_MESSAGE = "请求成功";

	private int code = FAIL_STATUS;

	private String message = FAIL_MESSAGE;

	private T data;

	public ResponseResult() {

	}

	public ResponseResult(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean ok() {
		return this.code == SUCC_STATUS;
	}
	
	public boolean authFailure() {
		return this.code == 401;
	}

	public static <T> ResponseResult<T> success() {
		return new ResponseResult<T>(SUCC_STATUS, SUCC_MESSAGE, null);
	}

	public static <T> ResponseResult<T> success(String message) {
		return new ResponseResult<T>(SUCC_STATUS, message, null);
	}

	public static <T> ResponseResult<T> success(T data) {
		return new ResponseResult<T>(SUCC_STATUS, SUCC_MESSAGE, data);
	}

	public static <T> ResponseResult<T> success(String message, T data) {
		return new ResponseResult<T>(SUCC_STATUS, message, data);
	}

	public static <T> ResponseResult<T> fail() {
		return new ResponseResult<T>(FAIL_STATUS, FAIL_MESSAGE, null);
	}

	public static <T> ResponseResult<T> fail(String message) {
		return new ResponseResult<T>(FAIL_STATUS, message, null);
	}

	public static <T> ResponseResult<T> fail(T data) {
		return new ResponseResult<T>(FAIL_STATUS, FAIL_MESSAGE, data);
	}

	public static <T> ResponseResult<T> fail(String message, T data) {
		return new ResponseResult<T>(FAIL_STATUS, message, data);
	}

}