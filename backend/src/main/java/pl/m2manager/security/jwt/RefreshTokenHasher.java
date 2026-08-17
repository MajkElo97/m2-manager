package pl.m2manager.security.jwt;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class RefreshTokenHasher {

	public String hash(String rawRefreshToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}
}
