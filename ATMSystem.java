import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// ---------- Custom Exceptions ----------
class InvalidPinException extends Exception {
    public InvalidPinException(String message) { super(message); }
}

class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) { super(message); }
}

class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) { super(message); }
}

class WithdrawalLimitExceededException extends Exception {
    public WithdrawalLimitExceededException(String message) { super(message); }
}

// ---------- Account ----------
class Account {
    private final String accountNumber;
    private final String holderName;
    private final int pin;
    private double balance;

    public Account(String accountNumber, String holderName, int pin, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public boolean verifyPin(int enteredPin) { return this.pin == enteredPin; }

    public void credit(double amount) { balance += amount; }
    public void debit(double amount) { balance -= amount; }
}

// ---------- ATM ----------
class ATM {
    private static final double PER_TRANSACTION_LIMIT = 20000;
    private static final double DAILY_WITHDRAWAL_LIMIT = 50000;
    private static final int MAX_PIN_ATTEMPTS = 3;

    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, Double> withdrawnToday = new HashMap<>();
    private Account currentAccount;

    public void addAccount(Account acc) {
        accounts.put(acc.getAccountNumber(), acc);
        withdrawnToday.put(acc.getAccountNumber(), 0.0);
    }

    // PIN verification
    public void login(String accNo, int pin) throws InvalidPinException {
        Account acc = accounts.get(accNo);
        if (acc == null || !acc.verifyPin(pin)) {
            throw new InvalidPinException("Invalid account number or PIN.");
        }
        currentAccount = acc;
    }

    public int getMaxPinAttempts() { return MAX_PIN_ATTEMPTS; }

    public void logout() { currentAccount = null; }

    public String getHolderName() { return currentAccount.getHolderName(); }

    // Balance enquiry
    public double checkBalance() {
        return currentAccount.getBalance();
    }

    // Deposit
    public void deposit(double amount) throws InvalidAmountException {
        validateAmount(amount);
        currentAccount.credit(amount);
    }

    // Withdrawal
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientBalanceException, WithdrawalLimitExceededException {
        validateAmount(amount);

        if (amount % 100 != 0) {
            throw new InvalidAmountException("Amount must be a multiple of 100.");
        }
        if (amount > PER_TRANSACTION_LIMIT) {
            throw new WithdrawalLimitExceededException(
                    "Per-transaction limit exceeded. Maximum allowed: Rs." + PER_TRANSACTION_LIMIT);
        }

        String accNo = currentAccount.getAccountNumber();
        double alreadyWithdrawn = withdrawnToday.get(accNo);
        if (alreadyWithdrawn + amount > DAILY_WITHDRAWAL_LIMIT) {
            throw new WithdrawalLimitExceededException(
                    "Daily withdrawal limit exceeded. Remaining today: Rs."
                            + (DAILY_WITHDRAWAL_LIMIT - alreadyWithdrawn));
        }
        if (amount > currentAccount.getBalance()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: Rs." + currentAccount.getBalance());
        }

        currentAccount.debit(amount);
        withdrawnToday.put(accNo, alreadyWithdrawn + amount);
    }

    private void validateAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }
}

// ---------- Main ----------
public class ATMSystem {
    public static void main(String[] args) {
        ATM atm = new ATM();
        atm.addAccount(new Account("1001", "Arun", 1234, 25000));
        atm.addAccount(new Account("1002", "Priya", 4321, 60000));

        Scanner sc = new Scanner(System.in);
        System.out.println("===== WELCOME TO THE ATM =====");

        // ----- PIN verification (max 3 attempts) -----
        boolean loggedIn = false;
        for (int attempt = 1; attempt <= atm.getMaxPinAttempts(); attempt++) {
            try {
                System.out.print("Enter Account Number: ");
                String accNo = sc.nextLine().trim();
                System.out.print("Enter PIN: ");
                int pin = Integer.parseInt(sc.nextLine().trim());
                atm.login(accNo, pin);
                loggedIn = true;
                break;
            } catch (InvalidPinException e) {
                System.out.println("Error: " + e.getMessage()
                        + " Attempts left: " + (atm.getMaxPinAttempts() - attempt));
            } catch (NumberFormatException e) {
                System.out.println("Error: PIN must be numeric. Attempts left: "
                        + (atm.getMaxPinAttempts() - attempt));
            }
        }

        if (!loggedIn) {
            System.out.println("Too many failed attempts. Card blocked.");
            sc.close();
            return;
        }

        System.out.println("\nLogin successful. Welcome, " + atm.getHolderName() + "!");

        // ----- Menu -----
        int choice = 0;
        do {
            System.out.println("\n1. Balance Enquiry");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid option number.");
                choice = 0;
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.println("Available balance: Rs." + atm.checkBalance());
                        precisionCheck(atm.checkBalance()); // Helper or just standard print
                        break;
                    case 2:
                        System.out.print("Enter amount to withdraw: ");
                        double w = Double.parseDouble(sc.nextLine().trim());
                        atm.withdraw(w);
                        System.out.println("Please collect your cash. Rs." + w
                                + " withdrawn. Balance: Rs." + atm.checkBalance());
                        break;
                    case 3:
                        System.out.print("Enter amount to deposit: ");
                        double d = Double.parseDouble(sc.nextLine().trim());
                        atm.deposit(d);
                        System.out.println("Rs." + d + " deposited. Balance: Rs." + atm.checkBalance());
                        break;
                    case 4:
                        System.out.println("Thank you for using the ATM. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (InvalidAmountException | InsufficientBalanceException
                     | WithdrawalLimitExceededException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Transaction failed: Please enter a numeric amount.");
            }
        } while (choice != 4);

        atm.logout();
        sc.close();
    }
    
    private static void precisionCheck(double balance) {
        // Optional placeholder if formatting decimal points is needed in future
    }
}
