package test.org.eif.models.utils;

import org.eif.models.utils.SnowFlake;
import org.junit.Test;

public class TestSnowFlake {
	@Test
	public void testSnowFlake() {
		SnowFlake snowFlake = new SnowFlake(2, 3);
		for (int i = 0; i < (1 << 12); i++) {
			System.out.println(snowFlake.nextId());
		}
	}
}
