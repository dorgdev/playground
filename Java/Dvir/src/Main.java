import java.util.Random;


public class Main {

	private static final Random RAND = new Random();
	public static float rand() {
		return (RAND.nextFloat() * 2) - 1;
	}

	
	public static void main(String[] args) {
//		for (int i = 0; i < 20; ++i) {
//			float x = rand();
//			System.out.println(x);
//		}
		int sum = 0;
		int times = 100000000;
		for (int i = 0; i < times; i++) {
			float x = rand();
			float y = rand();
			if ((x*x) + (y*y) < 1) {
				sum++;
			}
		}
		System.out.println(sum);
		float PI = (4f * sum) / times;
		System.out.println("PI = " + PI);
		System.out.println("PI = " + Math.PI);
		
		
		int array[] = new int[]{3,1,2,2,-4,-4};
		/*****/
		
		/*****/
		System.out.println("This is serial?");
		
	}

}
