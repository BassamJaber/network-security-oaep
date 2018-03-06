import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class OAEP {

	private static String[][] hashValues = new String[32][2];
	private static Map<String, String> hashValuesMapH = new HashMap<String, String>();
	private static Map<String, String> hashValuesMapG = new HashMap<String, String>();
	private final static String HASH_H_FILE_NAME = "hash_H_Function.txt";
	private final static String HASH_G_FILE_NAME = "hash_G_Function.txt";
	private final static int n = 34189;
	private static final int e = 3;
	private static final int d = 22547;
	private static final int p = 179;
	private static final int q = 191;

	private static final int K1 = 3;
	private static final int K0 = 5;
	private static int numOfLeadingZeros = 0;

	public static void main(String[] args) {
		char letter = 'X';
		System.out.println("Character to Encrypt is :"+letter);
		System.out.println("Reading hash values ...");
		readHashValuesFromFile(HASH_G_FILE_NAME);
		createHashMap(hashValuesMapG);
		// printHashValues();
		readHashValuesFromFile(HASH_H_FILE_NAME);
		createHashMap(hashValuesMapH);
		System.out.println("Reading hash values done!");
		// printHashValues();

		long mPrime;
		do {
			mPrime = getMPrime(letter);
			// System.out.println(mPrime);
		} while (mPrime > n);
		
//		System.out.println(Long.toBinaryString(mPrime));
		System.out.println("Encryption of Data ...");
		BigInteger c = encrypt(new BigInteger(mPrime + ""));
		System.out.println("Cipher :");
		System.out.println(Long.toBinaryString(c.intValue()));

		System.out.println("Decryption of Data ...");
		BigInteger m = decrypt(new BigInteger(c + ""));
		System.out.println("Letter with OAEP scheme");
		System.out.println(Long.toBinaryString(m.intValue()));
		System.out.println("Remove Padding...");
		String number=Long.toBinaryString(m.intValue());
		int length=number.length()+numOfLeadingZeros;
		String format="%"+length+"s";
		String numberWithLeadingZeros=String.format(format, number).replace(' ', '0');
		removePadding(numberWithLeadingZeros);
	}

	public static void setNumberOfLeadingZeros(String number) {
		int count = 0;
		for (int i = 0; i < number.length(); i++) {
			if (number.charAt(i) == '0') {
				count++;
			} else {
				break;
			}
		}
		numOfLeadingZeros = count;
		System.out.println(numOfLeadingZeros);
	}

	public static BigInteger encrypt(BigInteger m) {
		return (m.pow(e).mod(new BigInteger("" + n)));
	}

	public static BigInteger decrypt(BigInteger c) {
		return (c.pow(d).mod(new BigInteger("" + n)));
	}

	public static void removePadding(String mWithPadding) {
//		System.out.println(mWithPadding);
		String m0Prime = mWithPadding.substring(0, mWithPadding.length() - K0);
		// System.out.println(m0Prime);
		String m1Prime = mWithPadding.substring(mWithPadding.length() - K0); // only
		// System.out.println(m1Prime);

		String hHash = hashValuesMapH.get(m0Prime.subSequence(m0Prime.length() - 5, m0Prime.length()));
		// System.out.println(hHash);
		String rValue = XorStrings(m1Prime, hHash.substring(6, 11));

		String gHash = hashValuesMapG.get(rValue);
		// System.out.println(gHash);
		String mWithK0Padding = XorStrings(gHash, m0Prime);
		// System.out.println(mWithK0Padding);
		String letter = mWithK0Padding.substring(0, mWithK0Padding.length() - K1);
		System.out.println("Character is :");
		System.out.println(Integer.parseInt(letter, 2));
		System.out.println((char) Integer.parseInt(letter, 2));

	}

	public static long getMPrime(char letter) {
		// 8th character in ASCII is always 0 for parity check
		String binaryLetter = "0" + Integer.toBinaryString((int) letter);
		String r = getRandomROfLengthK0();
		String gHash = hashValuesMapG.get(r);
		// padd the letter before XOR
		String m0Prime = XorStrings(gHash, binaryLetter + getPaddingLengthK1());
		// take the least 5 significant bits for H hash function ( only require
		// 5 bits)
		String hHash = hashValuesMapH.get(m0Prime.substring(6, 11));
		// also take the least 5 significant bits for H output
		String m1Prime = XorStrings(r, hHash.substring(6, 11));
		String mPrime = m0Prime + m1Prime;
		System.out.println("Letter after performing OAEP scheme :");
		System.out.println(mPrime);
		setNumberOfLeadingZeros(mPrime);
		return Long.parseLong(mPrime, 2);
	}

	public static String XorStrings(String a, String b) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < a.length(); i++) {
			sb.append(charOf(bitOf(a.charAt(i)) ^ bitOf(b.charAt(i))));
		}

		String result = sb.toString();
		return result;
	}

	private static boolean bitOf(char in) {
		return (in == '1');
	}

	private static char charOf(boolean in) {
		return (in) ? '1' : '0';
	}

	public static String getPaddingLengthK1() {
		String padding = "";
		for (int i = 0; i < K1; i++) {
			padding += "0";
		}
		return padding;
	}

	public static String getRandomROfLengthK0() {
		String rWorks = "00101";
		String rNotWork = "10101";
		int randNum = (int) (Math.random() * Math.pow(2, K0));
		// need to pad it to five;
		String randString = Integer.toBinaryString(randNum);
		int length = randString.length();
		for (int i = 0; i < 5 - length; i++) {
			randString = "0" + randString;
		}
		return randString;
	}

	public static void createHashMap(Map<String, String> map) {
		for (int i = 0; i < hashValues.length; i++) {
			String key = hashValues[i][0];
			String value = hashValues[i][1];
			map.put(key, value);
		}
	}

	public static void printHashValues() {
		for (int i = 0; i < hashValues.length; i++) {
			for (int j = 0; j < hashValues[i].length; j++) {
				System.out.print(hashValues[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void readHashValuesFromFile(String fileName) {
		File file = new File(fileName);
		try {
			Scanner in = new Scanner(file);
			int c = 0;
			int r = 0;
			while (in.hasNext()) {
				hashValues[r][c] = in.next();
				c = (++c) % 2;
				if (c == 0) {
					r++;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist !");
			System.exit(-1);
		}
	}
}
