package idc.des;

import idc.des.DesConfig.BlockMode;
import idc.des.DesConfig.Op;
import idc.des.DesConfig.PlainFormat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A utility class for creating the configuration file for DES program.
 */
public class ConfigCreator {

  /**
   * The main method of the application.
   */
  public static void main(String[] args) {
    try {
      Scanner scanner = new Scanner(System.in);
      // DES Operation.
      System.out.println("Please select the operation:");
      System.out.println(" (1) Encrypt");
      System.out.println(" (2) Decrypt");
      System.out.println(" (3) Verify");
      int op = readInt(scanner);
      while (op < 1 || op > 3) {
        System.out.println("Please choose a valid option - 1/2/3: ");
        op = readInt(scanner);
      }
      DesConfig.Op opValue = (op == 1 ? Op.ENCRYPT : (op == 2 ? Op.DECRYPT : Op.VERIFY));
      // Plain Format.
      System.out.println("Please select the plain format:");
      System.out.println(" (1) ASCII");
      System.out.println(" (2) Radix-64");
      int format = readInt(scanner);
      while (format < 1 || format > 2) {
        System.out.println("Please choose a valid option - 1/2: ");
        format = readInt(scanner);
      }
      DesConfig.PlainFormat formatValue = (format == 1 ? PlainFormat.ASCII : PlainFormat.RADIX64);
      // Block mode.
      System.out.println("Please select the block mode:");
      System.out.println(" (1) CBC");
      System.out.println(" (2) ECB");
      int mode = readInt(scanner);
      while (mode < 1 || mode > 2) {
        System.out.println("Please choose a valid option - 1/2: ");
        mode = readInt(scanner);
      }
      DesConfig.BlockMode modeValue = (mode == 1 ? BlockMode.CBC : BlockMode.ECB);
      
      DesConfig config = new DesConfig(opValue, formatValue, modeValue);
      // Filename
      System.out.println("Please enter the configuration filename: [cfg.txt]");
      System.out.print(">>> ");
      System.out.flush();
      String filename = scanner.nextLine();
      if (filename.trim().equals("")) {
        filename = "cfg.txt";
      }
      try {
        config.writeConfig(new FileWriter(filename));
      } catch (IOException e) {
        System.err.println("Failed writing to the configuration file: " + e.getMessage());
      }
    } catch (Exception e) {
      System.err.println();
      System.err.println(e.getMessage());
    }      
  }
  
  /**
   * Reads an <code>int</code> from the given scanner and returning its result.
   * This makes sure that each <code>int</code> is given in a different line, as the
   * application expects. Will also retry while an invalid value is given.
   * @param scanner The <code>Scanner</code> to read from.
   * @return The read value.
   * @throws Exception In case the scanner have no more input to scan.
   */
  private static int readInt(Scanner scanner) throws Exception {
    try {
      try {
        System.out.print(">>> ");
        System.out.flush();
        return Integer.parseInt(scanner.nextLine());
      } catch (NumberFormatException e) {
        System.out.println("Please enter a valid number:");
      }
    } catch (NoSuchElementException e) {
      // No more data (scenario #1). Quit.
      throw new Exception("Quit.");
    } catch (IllegalStateException e) {
      // No more data (scenario #2). Quit.
      throw new Exception("Quit.");
    }
    // Irelevant. Only for copmliation correctness.
    return -1;
  }
}
