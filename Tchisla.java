import java.util.*;

public class Tchisla {
  // The base number that is used to form others.  
  public int baseNumber;
  // The maximum times that a base number can be used.
  private int limit;
  // The maximum level allowing non integer value.
  private int maxLevelWithNonInteger;
  // ArrayList of numbers formed by repeating the base number like 11, 222.
  private ArrayList<Long> unitDigitNumber;
  // The reperesentations of numbers.
  public HashMap<Number, String> representations;
  // level.get(i-1) store all the possible numbers that can be formed
  // by i base numbers.
  public ArrayList<ArrayList<Number>> levels;

  private static char[] BINARY_OPERATORS = {'+', '-', '*', '/', '^'};

  Tchisla(int baseNumber, int limit, int maxLevelWithNonInteger) {
    this.baseNumber = baseNumber;
    this.limit = limit;
    this.maxLevelWithNonInteger = maxLevelWithNonInteger;
    long value = baseNumber;
    this.unitDigitNumber = new ArrayList<>();
    this.representations = new HashMap<>();
    this.levels = new ArrayList<>();
    for (int i = 0; i < 9; ++i) {
      this.unitDigitNumber.add(value);
      value = value * 10 + baseNumber;
    }
  }

  Tchisla(int baseNumber, int limit) {
    this(baseNumber, limit, 4);
  }

  Tchisla(int baseNumber) {
    this(baseNumber, 9);
  }

  public void addLevel(int l) {
    ArrayList<Number> newLevel = new ArrayList<>();
    String s1;
    String s2;
    Number temp;
    if (l - 1 < unitDigitNumber.size()) {    
      temp = new Number(unitDigitNumber.get(l - 1));
      newLevel.add(temp);
      representations.put(temp, temp.toString());
    }
    for (int i = 0; i < l - 1; ++i) {
      for (Number n1 : levels.get(i)) {
        for (Number n2 : levels.get(l - 2 - i)) { 
          for (char operator : BINARY_OPERATORS) {
            s1 = representations.get(n1);
            s2 = representations.get(n2);

            temp = Number.compute(n1, operator, n2);
            if (!temp.isValidNumber()) continue;
            if (l > maxLevelWithNonInteger && !temp.isInteger()) continue;
            if (representations.containsKey(temp)) continue;
            newLevel.add(temp);
            representations.put(
                temp, "(" + s1 + ")" + operator + "(" + s2 + ")");
          }  
        }
      }
    }
    int start = 0;
    int end = newLevel.size();
    while (start < end) {
      for (int i = start; i < end; ++i) {
        s1 = representations.get(newLevel.get(i));

        temp = Number.sqrt(newLevel.get(i));
        if (!temp.isValidNumber()) continue;
        if (l > maxLevelWithNonInteger && !temp.isInteger()) continue;
        if (representations.containsKey(temp)) continue;
        newLevel.add(temp);
        representations.put(temp, "\u221a(" + s1 + ")");
        
        temp = Number.factorial(newLevel.get(i));
        if (!temp.isValidNumber()) continue;
        if (representations.containsKey(temp)) continue;
        newLevel.add(temp);
        representations.put(temp, "(" + s1 + ")!");
      }
      start = end;
      end = newLevel.size();
    }
    levels.add(newLevel);
    // System.out.println(newLevel);
  }

  public void run() {
    for (int i = 1; i <= limit; ++i) {
      addLevel(i);
      // System.out.println(i + " " + representations.size());
    }
  }

  public static void main(String[] args) {
    for (int j = 1; j <= 9; ++j) {
      Tchisla t = new Tchisla(j);
      t.run();
      
      for (int i = 1; i <= 100; ++i) {
        System.out.print(i + "#" + t.baseNumber + ": ");
        Number temp = new Number(i);
        if (t.representations.containsKey(temp)) {
          System.out.println(t.representations.get(temp));
        } else {
          System.out.println();
        }
      }
    }
  }

}
