package test.org.eif.models.utils;

import java.io.IOException;

import org.eif.models.utils.Base64;
import org.eif.models.utils.CryptoJS;
import org.junit.Test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestCryptoJS {
	String priavteKey = 
			  "MIICWwIBAAKBgQCBq1Pu0qf7HrCebIQZyID/O1lEzbHGjXWxTNJGgrPi1vOrLxe4"
			+ "61JkbUxZTe1bLQ4fM9LlNi/M+zB6mPiY6ujSm73vkFTXTxgGrCWVgGgFsaGAFzVU"
			+ "3myBNUB7965QB7/FIBz5ROKvEWl+G0EWN+DiZvCLc2sDr3OMSO5272JS6wIDAQAB"
			+ "AoGAWG6WrD5gL2PLwg9xu//A8c/w3nqFrkqVJFim402KDr7XqTxRqlxacYEU26bI"
			+ "pDDC+8alIork6J+6fTHMAMXcB5sMye203pjNza4D8Y/t235X/hetqNoF7XCJz/N+"
			+ "LSA9CxfqYPWDZiinx9DHK0OD61/77ONhcSVYXyj/zBxiRAECQQDzYXjUijbpUWCI"
			+ "6YPsHaT1M48I/KwRqZKh+zUWIar5YLVn4GS0rjgEpPQkoxfnifRgRiW71lkO3ndn"
			+ "z79PNTYBAkEAiGR/+anT9F1yAsK8OzvTuuogmPExE0m7Ht7isihX4OvFrA0UxeIL"
			+ "kDgR4mpl4NwvgvGZZveQWYnhiLgAawnA6wJANZOggNFIU0ZpxNn7A5BnDt9hvm25"
			+ "B05YDDZ7sjqJI8qpRIu+P5QWmtchM6t/iKXy2Pq4Nchy5HYOvRibmbkyAQJAXKR+"
			+ "9LMxH+O1BEEHn+hUObq754a0gdhr/F0CeGbEwicDfojoGj0fA5fCNht1FdYZg4Td"
			+ "YL45U0g/ZCEvT4UxfwJAfvyCQKUZPiWX03yoq60O3S0xMyrkqo3Bft56OdBGwZ5z"
			+ "8PMAXE28D9WA5CqUMs+c9fcij5dYqIXCDlasppls4Q==";

	/**
	 * 测试RSA解密
	 * 
	 * @throws Exception
	 * @throws IOException
	 */
	@Test
	public void testRSADecrypt() throws IOException, Exception {
		java.security.Security.addProvider(
		         new BouncyCastleProvider()
		);
		String data = "gUvlW5E6YcBeaJGINHxKLeFv5dpeWrWJiMx/0cv/llmKR/f38TBu5/G9MgYJRluR41abjIsnuyqVKGW152ks9S9qR6JRAt3CQjliiyunIYZBVca3GmJ2ietBEhZbnqvGqHtz27PloIJ4avzEEFH+D/Qbba/7KcZ9SCjNBq33NBc=";
		byte[] reData = CryptoJS.RSA.decrypt(CryptoJS.RSA.getPrivateKey(priavteKey), Base64.decode(data));
		System.out.println(new String(reData));
	}


}
