package idc.des;


/**
 * Implements a Feistel network cipher. Followed:
 *  http://csrc.nist.gov/publications/fips/fips46-3/fips46-3.pdf
 */
public class FeistelNetworkDataCipher implements DataCipherInterface {

	/** Represents Feistel substitution boxes **/
  public static final byte[][][] SBOX = {
	  {  // S1
	    { 14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7 },
	    {  0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8 },
	    {  4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0 },
	    { 15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13 }},
	  {  // S2
	    { 15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10 },
	    {  3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5 },
	    {  0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15 },
	    { 13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9 }},
	  {  // S3
	    { 10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8 },
	    { 13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1 },
	    { 13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7 },
	    {  1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12 }},
	  {  // S4
	    {  7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15 },
	    { 13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9 },
	    { 10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4 },
	    {  3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14 }},
	  {  // S5
	    {  2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9 },
	    { 14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6 },
	    {  4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14 },
	    { 11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3 }},
	  {  // S6
	    { 12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11 },
	    { 10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8 },
	    {  9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6 },
	    {  4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13 }},
	  {  // S7
	    {  4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1 },
	    { 13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6 },
	    {  1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2 },
	    {  6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12 }},
	  {  // S8
	    { 13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7 },
	    {  1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2 },
	    {  7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8 },
	    {  2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11 }}};

	/** Represents the Feistel permutation order **/
	public static final byte[] P = {
	    16,  7, 20, 21, 29, 12, 28, 17,  1, 15, 23, 26,  5, 18, 31, 10,
	     2,  8, 24, 14, 32, 27,  3,  9, 19, 13, 30,  6, 22, 11,  4, 25};

	/** Represents the Feistel expansion order **/
	public static final byte[] E = { 
	    32,  1,  2,  3,  4,  5,  4,  5,  6,  7,  8,  9,  8,  9, 10, 11,
	    12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21,
			22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32,  1};
	

	/** Represents the Feistel initial permutation order **/
	public static final byte[] IP = { 
	    58, 50, 42, 34, 26, 18, 10,  2, 60, 52, 44, 36, 28, 20, 12,  4,
	    62, 54, 46, 38, 30, 22, 14,  6, 64, 56, 48, 40, 32, 24, 16,  8,
	    57, 49, 41, 33, 25, 17,  9,  1, 59, 51, 43, 35, 27, 19, 11,  3,
	    61, 53, 45, 37, 29, 21, 13,  5, 63, 55, 47, 39, 31, 23, 15,  7};
	
	/** Represents the Feistel reversed initial permutation order **/
	public static final byte[] IPr = {
	    40,  8, 48, 16, 56, 24, 64, 32, 39,  7, 47, 15, 55, 23, 63, 31,
	    38,  6, 46, 14, 54, 22, 62, 30, 37,  5, 45, 13, 53, 21, 61, 29,
	    36,  4, 44, 12, 52, 20, 60, 28, 35,  3, 43, 11, 51, 19, 59, 27,
	    34,  2, 42, 10, 50, 18, 58, 26, 33,  1, 41,  9, 49, 17, 57, 25};

  /** Represents the Feistel key scheduler permutation choice one **/
	public static final byte[] PC1 = {
      57, 49, 41, 33, 25, 17,  9,  8,  1, 58, 50, 42, 34, 26, 18, 16,
      10,  2, 59, 51, 43, 35, 27, 24, 19, 11,  3, 60, 52, 44, 36, 32, 
      63, 55, 47, 39, 31, 23, 15, 40,  7, 62, 54, 46, 38, 30, 22, 48,
      14,  6, 61, 53, 45, 37, 29, 56, 21, 13,  5, 28, 20, 12,  4, 64};

  /** Represents the Feistel key scheduler permutation choice two **/
	public static final byte[] PC2 = { 
      14, 17, 11, 24,  1,  5,  3, 28, 15,  6, 21, 10, 23, 19, 12,  4,
      26,  8, 16,  7, 27, 20, 13,  2, 41, 52, 31, 37, 47, 55, 30, 40,
      51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};

  /** 
   * Represents the amount of left shifts to perform on the key in
   * every key-iteration production.
   **/
	public static final int[] KEY_SHIFTS = {
       1,  1 , 2,  2,  2,  2,  2,  2,  1,  2,  2,  2,  2,  2,  2,  1 };
  
  /** A helper constant for easier access */
	private static final int NUM_KEYS = KEY_SHIFTS.length;
	
  @Override
  public long encrypt(long plain, long key) {
    return process(plain, key, false);
  }

  @Override
  public long decrypt(long cipher, long key) {
    return process(cipher, key, true);
  }

	/**
	 * Encrypt/Decrypt the given input with the key using to the Feistel 
	 * network algorithm. For decrypting, reversed should be true.
	 * 
	 * @param input The data to encrypt/decrypt.
	 * @param key The key for the operation.
	 * @param reversed Whether the key permutations should be read in reversed
	 * order (in decryption mode). 
	 * @return The encrypted/decrypted data after processing the input.
	 */
	private long process(long input, long key, boolean reversed) {
		// Apply initial permutation.
		long permutated = permutate(input, IP);
	
		// Split the result into left and right halves.
		long left = permutated >>> (Byte.SIZE * 4);
		long right = permutated & 0xFFFFFFFFL;
		
		// Produce the keys for process.
		long[] keys = produceKeys(key);
		
		// Run Feistel network algorithm.
		for (int i = 0; i < NUM_KEYS; i++) {
			long tempRight = right;
			long iterKey = keys[reversed ? NUM_KEYS - i - 1: i];
			right = left ^ fFunction(right, iterKey);
			left = tempRight;
		}
		
		// Join both parts together again.
    long preoutput = (right << Long.SIZE / 2) | left;
		
		// Apply reversed permutation.
		return permutate(preoutput, IPr);
	}
	
	/**
	 * Produces the keys for the different iterations based on the 64bit key
	 * given as an input.
	 * @param key The key from which the 16 keys are generated.
	 * @return 16 keys for the encryption/decryption process.
	 * @see FeistelNetworkDataCipher#NUM_KEYS for the exact amount of keys.
	 */
	private long[] produceKeys(long key) {
	  // Perform initial permutation and break the key into 2 parts.
	  key = permutate(key, PC1);
    long c = key >>> Byte.SIZE * 4;
    long d = key & 0xFFFFFFFFL;
    // Create the keys.
    long[] keys = new long[NUM_KEYS];
    for (int i = 0; i < keys.length; ++i) {
      // Shift left.
      int shifts = KEY_SHIFTS[i];
      c = (c << shifts) | (c >>> (Integer.SIZE - shifts));
      d = (d << shifts) | (d >>> (Integer.SIZE - shifts));
      // Join the 2 parts back together to produce the iteration key.
      long joined = (c << Long.SIZE / 2) | d;
      keys[i] = permutate(joined, PC2);
    }
    return keys;
	}
	
	/**
	 * Feistel F Function. The function maps from a 32-bit value and a 48-bit
	 * value to a single 32-bit result: OUTPUT = F(R,K).
	 * @param input The function source (R), up to 32 bits (cipher/plain).
	 * @param key The function key (K) up to 48 bits.
	 * @return The function result - 32 bit value (OUTPUT).
	 */
	private long fFunction (long input, long key) {
		// Expands input from 32bit to 48bit
		long expandedInput = expand(input);

		// Xor the expended input and the key
		long xored = expandedInput ^ key;

		// Run S-box on the xored data
		long subtitued = substitute(xored);

		// Apply final permutation and return ciphered data
		return permutate(subtitued, P);
	}

	/**
	 * Expand the given 32-bit input into a 48-bits value using 
	 * Feistel expansion algorithm.
	 * @param input The 32-bit number to be expended.
	 * @return An expended 48-bit number
	 */
	protected long expand(long input) {
		long expanded = 0;
		// Iterate the E array, taking each bit from input and applies it into output
		for (int i = 0; i < E.length; i++) {
			int setPosition = Long.SIZE - E.length + i;  
			int getPosition = Long.SIZE - Byte.SIZE * 4 + E[i] - 1;
			expanded = Utils.setBit(expanded, setPosition, Utils.getBit(input, getPosition));
		}
		return expanded;
	}

	/**
	 * Returns a byte consisting six bits from given long number starting at given position
	 * @param number The number from which the bit are extracted.
	 * @param start The starting position of the sexta.
	 * @return 6 bits from given number in byte format
	 */
	protected byte getSexsta(long number, int pos) {
		return (byte) ((number >>> (Long.SIZE - pos - 6)) & 0x3F);
	}
	
	/**
	 * Apply Substitution box transformation on given input.
	 * @param input the 48-bit value to be substituted.
	 * @return The subtitution 32-bit result.
	 */
	protected long substitute(long input) {
		long out = 0L;
		for (int i = 0; i < 6; i++) {
			byte cur = getSexsta(input, i * 6);
			
			// Sets row to be the outcome of joining the leftmost bit with the
			// rightmost one
			int row = 2 * (cur >>> 5) + (cur & 0x01);

			// Sets column to be the middle 4 bits
			int col = (cur >>> 1) & 0x0F;

			// Applies substitution
			byte modified = Utils.getByte(out, i); 
			modified |= SBOX[i][row][col];
			
			// Each SBOX element is actually 4 bits long
			// We should shift it when i is an even number
			if (i % 2 == 0) {
				modified <<= 4;
			}
			
			// Modify the relevant output byte
			out = Utils.setByte(out, modified, i);
		}
		return out;
	}

	/**
	 * Apply bitwise permutation of choice on a given input.
	 * <b>Important:</b> The method assumes that the permutation's numbers are
	 * values in the range [0, Long.SIZE).
	 * @param input a number to be permutates
	 * @return The permutated value.
	 */
	protected long permutate(long input, byte[] permutation) {
		long out = 0L;
		
		// Applies permutation
		for (int i = 0; i < permutation.length; i++) {
			int setPosition = Long.SIZE - permutation.length + i;
			int getPosition = Long.SIZE - permutation.length + permutation[i] - 1;
			out = Utils.setBit(out, setPosition, Utils.getBit(input, getPosition));
		}
		return out;
	}
}
