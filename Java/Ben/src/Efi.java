
public class Efi {

  public static void main(String[] args) {


    if (args.length !=1) {
      return;
    }
    int num = Integer.parseInt(args[0]); 
    int i = 1;
    int j = num/2;
    while (i <= num) {
        for(int count = j; count > 0; count--) {
          System.out.print(" ");
        }
        for(int count=1; count<= i; count++) {
          System.out.print("*");
        }
        i+=2;
        j-=1;
        System.out.println();
    }
    i-=4;
    j+=2;
    while (i >= 1) {
        for(int count = 0; count<j; count++) {
          System.out.print(" ");
        }
        for(int count=1; count<=i; count++) {
          System.out.print("*");
        }
        i-=2;
        j+=1;
        System.out.println();
    }
    }
}
