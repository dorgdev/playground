package idc.tests;

import static org.junit.Assert.assertEquals;
import idc.des.FeistelNetworkDataCipher;

import org.junit.Before;
import org.junit.Test;

public class FeistelNetworkDataCipherTest {
  /** Exposes some methods for testing. */
  class FeistelNetworkDataCipherExposer extends FeistelNetworkDataCipher {
    @Override
    public long expand(long input) {
      return super.expand(input);
    }
    @Override
    public byte getSexsta(long number, int pos) {
      return super.getSexsta(number, pos);
    }
    @Override
    public long permutate(long input, byte[] permutation) {
      return super.permutate(input, permutation);
    }
    @Override
    public long substitute(long input) {
      return super.substitute(input);
    }
  }

  /** The tested instance. */
  private FeistelNetworkDataCipherExposer fe;

	@Before
	public void setUp() throws Exception {
		this.fe = new FeistelNetworkDataCipherExposer();
	}

	@Test
	public void getSexstaTest() {
		long in = 0xF0L;
		byte out = fe.getSexsta(in, 58);
		assertEquals(0x30, out);
		
		out = fe.getSexsta(0x1F, 57);
		assertEquals(0x0F, out);
	}
	
	@Test
	public void permutateTest() {
		/**
		 * { 16, 7, 20, 21, 29, 12, 28, 17, 
		 * 1, 15, 23, 26, 5, 18, 31, 10, 
		 * 2, 8, 24, 14, 32, 27, 3, 9,
		 * 19, 13, 30, 6, 22, 11, 4, 25 };
		 */
		long in = 1L;
		long out = fe.permutate(in, FeistelNetworkDataCipher.P);
		assertEquals(1L << 11, out);
	}
	
	@Test
	public void expandTest() {
		/**
		 * { 32, 1, 2, 3, 4, 5, 4, 5, 
		 * 	6, 7, 8, 9, 8, 9, 10, 11, 
		 * 12, 13, 12, 13, 14, 15, 16, 17, 
		 * 16, 17, 18, 19, 20, 21, 20, 21, 
		 * 22, 23, 24, 25, 24, 25, 26, 27, 
		 * 28, 29, 28, 29, 30, 31, 32, 1 };
		 */
		
		long in = 1L << 31;
		long out = fe.expand(in);
		long expected = (1L << 46);
		expected |= 1;
		assertEquals( expected, out);
	}
	
	@Test
	public void subsTest() {
		long out = fe.substitute(0);
		System.out.println(out);
		long expected = -2301444932100161536L;
		assertEquals(expected, out);
	}
	
	@Test
	public void encryptDecryptBenchmarkTest() {
		long input = 0x23FBACC1003B4211L;
		long key = 0x03725ABCC332FA01EL;
		
		long startTime, duration;
		long cipher;
		long totalRuntime = 0;
		
		final long TIMES = 100000L;
		for (int i = 0; i < TIMES; i++) {
			 startTime = System.nanoTime();
			 cipher = fe.encrypt(input, key);
			 fe.decrypt(cipher, key);
			 duration = System.nanoTime() - startTime;
			 totalRuntime += duration;
		}
		System.out.println("New Feistel avarage runtime (ns): " + totalRuntime / TIMES);
	}
}
