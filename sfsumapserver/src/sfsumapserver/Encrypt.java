package sfsumapserver;

/**
 * For encrypting passwords
 * 
 * @author Terry Wong
 */
public class Encrypt {

	private String password = "";

	public Encrypt(String pw) {
		password = pw;
	}

	public String getPassword() {
		Base64Encoder encoder = new Base64Encoder(password);
		password = encoder.processString();
		System.out.println("Encrypted password: " + password);
		return password;
	}
}
