package hk.ignition.commons;

public class InstapaperException extends Exception {

	private static final long serialVersionUID = 887980168086021094L;
	private int code;
	private String message;

	public InstapaperException(int code) {
		this.code = code;
		switch (code) {
			case 400:
				setMessage("Bad request. Probably missing a required parameter, such as url.");
				break;
			case 403:
				setMessage("Invalid username or password");
				break;
			case 500:
				setMessage("The service encountered an error. Please try again later.");
				break;
			default:
				setMessage("Unknown error");
		}
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

	public static void throwExceptionFromStatus(int statusCode) throws InstapaperException {
		if (statusCode >= 400) {
			throw new InstapaperException(statusCode);
		}
	}
}
