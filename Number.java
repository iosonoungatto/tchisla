import java.util.*;

public class Number {
  // This is the class of the intermediate numbers in Tchisla.
  // The numbers look like (numerator/denominator)^(1/index).
  // We have some restrictions:
  // All the numbers have to be positive.
  // Indices have to 1 or 2. 
  // Numerator<=0 indicates NAN.

  // List of n! for n=1 to 12.
  private static long[] FACTORIALS = {1, 2, 6, 24, 120, 720, 5040, 40320,
                                      362880, 3628800, 39916800, 479001600};

  // Max value of the following numerators.
  static long MAX_VALUE = 1L << 31;

  // Max value of integer.
  // It can be set later, and can be no larger than sqrt(Long.MAX_VALUE).
  static long MAX_INTEGER = 1L << 10;

  // Max value of numerator of a fraction.
  // It can be set later, and can be no larger than MAX_INTEGER.
  static long MAX_NUMERATOR = 11;

  // Max value of denominator of a fraction.
  // It can be set later, and can be no larger than MAX_INTEGER.
  static long MAX_DENOMINATOR = 11;

  // Max value inside the square root.
  // It can be set later, and can be no larger than MAX_INTEGER.
  static long MAX_SQUARE_ROOT_NUMERATOR = 1L << 7;

  // Though we set the MAX_NUMERATOR, we might allow some larger integers.
  // We allow a large integer n if there is a number in
  // [n-WHITELIST_PARAMETER, n+WHITELIST_PARAMETER]
  // which equals a perfect square.
  static long WHITELIST_PARAMETER = 1L << 7;

  // Using long to avoid potential integer overflow.
  public long numerator;
  public long denominator;
  public int index;


  public Number(long numerator, long denominator, int index) {
    if (numerator <= 0) {
      this.numerator = 0;
      this.denominator = 1;
      this.index = 1;
      return;
    }
    // Divide by the great common divisor.
    long gcd = greatCommonDivisor(numerator, denominator);
    if (gcd == 0) {
      this.numerator = 0;
      this.denominator = 1;
      this.index = 1;
      return;
    }
    this.numerator = numerator / gcd;
    this.denominator = denominator / gcd;
    this.index = index;

    if (this.index == 1) {
      // For fractions, the numerator needs to <= MAX_NUMERATOR and
      // the denominator needs to <=MAX_DENOMINATOR.
      if (this.denominator > 1) {
        if (this.denominator > MAX_DENOMINATOR || this.numerator > MAX_NUMERATOR) {
          this.numerator = 0;
        }
        return;
      }
      // For integers, the numerator n needs to <= MAX_INTEGER or
      // n <= MAX_VALUE and there is a number in
      // [n-WHITELIST_PARAMETER, n+WHITELIST_PARAMETER]
      // which equals a perfect square.
      if (this.numerator > MAX_INTEGER) {
        if (this.numerator > MAX_VALUE) {
          this.numerator = 0;
          return;
        }
        if ((long) Math.sqrt(this.numerator - WHITELIST_PARAMETER) ==
            (long) Math.sqrt(this.numerator + WHITELIST_PARAMETER)) {
          this.numerator = 0;
          return;
        }
      }
      return;
    }
    // For square roots, we do not consider the square root of a fraction
    // even if it can be simplified. 
    if (this.denominator != 1) {
      this.numerator = 0;
      return;
    }
    long value = Math.round(Math.sqrt(this.numerator));
    // If value * value == this.numerator, then we can simplify it.
    if (value * value == this.numerator) {
      this.numerator = value;
      this.index = 1;
    } else {
      // The integer in the square root needs to <= MAX_SQUARE_ROOT_NUMERATOR.
      if (this.numerator > MAX_SQUARE_ROOT_NUMERATOR){
        this.numerator = 0;
      }
    } 
  }

  public Number(long value) {
    this(value, 1, 1);
  }

  private long greatCommonDivisor(long p, long q) {
    if (p == 0 || q == 0) return 0;
    if (p < 0) p = -p;
    if (q < 0) q = -q;
    if (p == 1 || q == 1) return 1;
    long r;
    while (p % q != 0) {
      r = p % q;
      p = q;
      q = r;
    }
    return q;    
  }

  private boolean checkLargeNumber() {
    return true;
  }

  public boolean isValidNumber() {
    return numerator > 0;
  }

  public boolean isInteger() {
    return numerator > 0 && denominator == 1 && index == 1; 
  }

  public boolean isFraction() {
    return denominator > 1;
  }

  public boolean isSquareRoot() {
    return index > 1;
  }

  public double getValue() {
    if (!isValidNumber()) return 0;
    return Math.pow((double) numerator / denominator,
                    (1.0 / index));
  }


  public static Number add(Number n1, Number n2) {
    if (!n1.isSquareRoot() && !n2.isSquareRoot()) {
      return new Number(n1.numerator * n2.denominator 
                        + n1.denominator * n2.numerator,
                        n1.denominator * n2.denominator, 1);
    }
    if (n1.isSquareRoot() && n2.isSquareRoot()) {
      if (n1.numerator >= n2.numerator) {
        long factor = Math.round(Math.sqrt(n1.numerator / n2.numerator));
        if (n2.numerator * factor * factor == n1.numerator) {
          long newSquaredFactor = (factor + 1) * (factor + 1);
          return new Number(n2.numerator * newSquaredFactor, 1, 2);
        }
      } else {
        return add(n2, n1);
      }
    }
    return new Number(0);
  }

  public static Number subtract(Number n1, Number n2) {
    if (!n1.isSquareRoot() && !n2.isSquareRoot()) {
      return new Number(n1.numerator * n2.denominator
                        - n1.denominator * n2.numerator,
                        n1.denominator * n2.denominator, 1);
    }
    if (n1.isSquareRoot() && n2.isSquareRoot()) {
      if (n1.numerator > n2.numerator) {
        long factor = Math.round(Math.sqrt(n1.numerator / n2.numerator));
        if (n2.numerator * factor * factor == n1.numerator) {
          long newSquaredFactor = (factor - 1) * (factor - 1);
          return new Number(n2.numerator * newSquaredFactor, 1, 2);
        }
      }
    }
    return new Number(0);
  }

  public static Number multiply(Number n1, Number n2) {
    if (n1.isSquareRoot()) {
      if (n2.isSquareRoot()) {
        return new Number(n1.numerator * n2.numerator, 1, 2);
      } else {
        return new Number(n1.numerator * n2.numerator * n2.numerator,
                          n2.denominator * n2.denominator, 2);
      }
    } else {
      if (n2.isSquareRoot()) {
        return new Number(n1.numerator * n1.numerator * n2.numerator,
                          n1.denominator * n1.denominator, 2);    
      } else {
        return new Number(n1.numerator * n2.numerator,
                          n1.denominator * n2.denominator, 1);
      }
    }
  }

  public static Number divide(Number n1, Number n2) {
    if (n1.isSquareRoot()) {
      if (n2.isSquareRoot()) {
        return new Number(n1.numerator, n2.numerator, 2);
      } else {
        return new Number(n1.numerator * n2.denominator * n2.denominator,
                          n2.numerator * n2.numerator, 2);
      }
    } else {
      if (n2.isSquareRoot()) {
        return new Number(n1.numerator * n1.numerator,
                          n1.denominator * n1.denominator * n2.numerator, 2);    
      } else {
        return new Number(n1.numerator * n2.denominator,
                          n1.denominator * n2.numerator, 1);
      }
    }
  }

  public static Number power(Number n1, Number n2) {
    if (!n2.isInteger()) return new Number(0);
    if (n1.isFraction()) return new Number(0);
    if (n1.numerator == 1) return new Number(1);
    long newNumerator = 1;
    long temp = n1.numerator;
    long n = n2.numerator;
		while (n > 0) {
      // Return NAN if temp is larger than MAX_VALUE 
      // and we still need to multiply it.
      if (temp > MAX_VALUE) return new Number(0);
			if ( (n & 1) != 0) {
				newNumerator *= temp;
        // Return NAN if the partial result is larger than MAX_VALUE.
        if (newNumerator > MAX_VALUE) return new Number(0);
			}
			n = n >> 1;
			temp *= temp;
		}
		return new Number(newNumerator, 1, n1.index);
  }

  public static Number compute(Number n1, char operator, Number n2) {
    switch (operator) {
      case '+' : 
        return add(n1, n2);
      case '-' : 
        return subtract(n1, n2);
      case '*' : 
        return multiply(n1, n2);
      case '/' : 
        return divide(n1, n2);
      case '^' : 
        return power(n1, n2);
      default : 
        return new Number(0);
    }
  }

  public static Number sqrt(Number n) {
    if (!n.isInteger()) return new Number(0);
    return new Number(n.numerator, 1, 2);   
  }

  public static Number factorial(Number n) {
    if (!n.isInteger()) return new Number(0);
    if (n.numerator > 12) return new Number(0);
    return new Number(FACTORIALS[(int) n.numerator - 1]);
  }



  @Override public String toString() {
    if (!this.isValidNumber()) return "NAN";
    String result = "";
    if (index == 1) {
      result += numerator;
      if (denominator != 1) {
        result += "/";
        result += denominator;
      }
    } else {
      result += "\u221a(";
      result += numerator;
      if (denominator != 1) {
        result += "/";
        result += denominator;
      }
      result += ")";
    }
    return result;
  }

  @Override public boolean equals(Object otherObject) {
    if (this == otherObject) return true;
    if (otherObject == null) return false;
    if (this.getClass() != otherObject.getClass()) return false;
    Number other = (Number) otherObject;
    return (this.numerator == other.numerator &&
            this.denominator == other.denominator &&
            this.index == other.index); 
  }

  @Override public int hashCode() {
    return Objects.hash(this.numerator, this.denominator, this.index);
  }
}
