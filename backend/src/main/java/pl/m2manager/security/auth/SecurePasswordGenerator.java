package pl.m2manager.security.auth;

import java.security.SecureRandom;

public final class SecurePasswordGenerator {

	private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
	private static final String DIGITS = "23456789";
	private static final String SPECIAL = "!@#$%&*-_+=?";
	private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
	private static final int DEFAULT_LENGTH = 16;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private SecurePasswordGenerator() {
	}

	public static String generateTemporaryPassword() {
		return generateTemporaryPassword(DEFAULT_LENGTH);
	}

	public static String generateTemporaryPassword(int length) {
		if (length < 12) {
			throw new IllegalArgumentException("Password length must be at least 12");
		}

		char[] password = new char[length];
		password[0] = randomChar(UPPERCASE);
		password[1] = randomChar(LOWERCASE);
		password[2] = randomChar(DIGITS);
		password[3] = randomChar(SPECIAL);

		for (int i = 4; i < length; i++) {
			password[i] = randomChar(ALL);
		}

		shuffle(password);
		return new String(password);
	}

	private static char randomChar(String alphabet) {
		return alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length()));
	}

	private static void shuffle(char[] values) {
		for (int i = values.length - 1; i > 0; i--) {
			int j = SECURE_RANDOM.nextInt(i + 1);
			char temp = values[i];
			values[i] = values[j];
			values[j] = temp;
		}
	}
}
