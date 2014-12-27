package tests.ex07;

import ex07.Classroom;
import ex07.Lecture;

public class Main {

	public static void main(String[] args) {
		Classroom c1 = new Classroom("CL03");
		Classroom c2 = new Classroom("CB02");
		Lecture ret;
		
		ret = c1.addLecture(new Lecture(10, 12, "Data Structures"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		ret = c1.addLecture(new Lecture(13, 15, "Linear Algebra"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		ret = c1.addLecture(new Lecture(16, 18, "Operating Systems"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		ret = c1.addLecture(new Lecture(14, 15, "English"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		
		ret = c2.addLecture(new Lecture(10, 12, "Marketing"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		ret = c2.addLecture(new Lecture(11, 14, "Italian History"));
		System.out.println(ret == null ? "OK" : "Conflicts with: " + ret);
		
		System.out.println(c1);
		System.out.println(c2);
	}
	
}
