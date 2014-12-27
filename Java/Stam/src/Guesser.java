import java.util.Random;

public class Guesser {
  public Guesser(int low, int high) {
    this.low = low;
    this.high = high;
    this.initiated = false;
    this.random = new Random();
  }
  
  public void newGame() {
    current = low + random.nextInt(high + 1);
    initiated = true;
    counter = 0;
  }
  
  public int guess(int guess) {
    if (!initiated) {
      newGame();
    }
    counter++;
    if (current == guess) {
      System.out.println("(" + guess + ") Correct - it took you " + counter + " guess(es) to find the answer!");
      initiated = false;
      return 0;
    }
    if (current < guess) {
      System.out.println("(" + guess + ") Too high!");
      return 1;
    }
    System.out.println("(" + guess + ") Too low!");
    return -1;
  }

  public int getCounter() {
    return counter;
  }
  
  public static void main(String[] args) {
    int LOW = 1;
    int HIGH = 1000;
    Guesser guesser = new Guesser(LOW, HIGH);
    for (int i = 0; i < 20; ++i) {
      int low = LOW;
      int high = HIGH;
      int guess = low + (high - low) / 2;
      guesser.newGame();
      int res = guesser.guess(guess);
      while (res != 0 && guesser.getCounter() < 11) {
        if (res < 0) {
          low = guess + 1;
        } else {
          high = guess - 1;
        }
        guess = low + (high - low) / 2;
        res = guesser.guess(guess);
      }
      if (guesser.getCounter() > 10) {
        System.out.println("Failed to find the number in 10 guesses");
      } else {
        System.out.println("Found the number!!!");
      }
    }
  }
  
  private int low;
  private int high;
  private int current;
  private int counter;
  private boolean initiated;
  private Random random;
}
