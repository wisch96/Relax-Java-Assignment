import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Relax Gaming - Java clean code test
 *
 * For the purposes of this test, the candidate can assume that the code
 * compiles and that references
 * to other classes do what you would expect them to.
 *
 * The objective is for the candidate to list down the things in plain text
 * which can be improved in this class
 *
 * Good luck!
 *
 */

// Wilhelm comment: Capitalized class name according to java
// standard. Change to: public class Account
public class account {

    // Wilhelm comment: Change to private and final so its only accessible through
    // getAccountNumber() method to prevent unwanted changes to the variable.
    // Preserves
    // encapsulation.

    public String accountNumber;

    // Wilhelm comment: Start constructor name with capital letter to match class
    // name.
    public account(String accountNumber) {
        // Constructor
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber; // return the account number
    }

    public ArrayList getTransactions() throws Exception {
        try {
            // Wilhelm comment: Use generics to ensure type safety, <DbRow> for list and
            // <Transaction> for Arraylist. Use <Transaction> in return statement.

            List dbTransactionList = Db.getTransactions(accountNumber.trim()); // Get the list of transactions
            ArrayList transactionList = new ArrayList();
            /**
             * Wilhelm comment: Declare variable "i" inside the "for loop" statement.
             */
            int i;
            for (i = 0; i < dbTransactionList.size(); i++) {
                // Wilhelm comment: remove casting if using generics.
                DbRow dbRow = (DbRow) dbTransactionList.get(i);
                Transaction trans = makeTransactionFromDbRow(dbRow);
                /**
                 * Wilhelm comment: Avoid calling createTimestampAndExpiryDate multiple times.
                 * Store it in a variable and then reuse it to improve performance.
                 * Example:
                 * String[] timestampAndExpiryDate = createTimestampAndExpiryDate(trans)
                 * trans.setTimestamp(timestampAndExpiryDate[0]);
                 * trans.setExpiryDate(timestampAndExpiryDate[1]);
                 */
                trans.setTimestamp(createTimestampAndExpiryDate(trans)[0]);
                trans.setExpiryDate(createTimestampAndExpiryDate(trans)[1]);
                transactionList.add(trans);
            }
            return transactionList;

        } catch (SQLException ex) {
            // There was a database error
            throw new Exception("Can't retrieve transactions from the database");
        }
    }

    // Wilhelm comment: { symbol on separate line. Have it on the same line as
    // method declaration to be consistent with the other methods in class.
    public Transaction makeTransactionFromDbRow(DbRow row) 
    {
        double currencyAmountInPounds = Double.parseDouble(row.getValueForField("amt"));
        // Wilhelm comment: Avoid magic number: 1.10. Declare the number to a constant
        // alternatively change it dynamically using an API to get current transaction
        // rates

        // Wilhelm comment: Change casting to: (float)(...);
        float currencyAmountInEuros = new Float(currencyAmountInPounds * 1.10);
        String description = row.getValueForField("desc");
        // Wilhelm comment: Remove unused code instead of commenting it out.

        // description = fixDescription(description);
        return new Transaction(description, currencyAmountInEuros); // return the new Transaction object
    }

    // Wilhelm comment: Capitalize the d in createTimestampAndExpirydate to match
    // java naming conventions.
    // Change name to: createTimestampAndExpiryDate

    public String[] createTimestampAndExpirydate(Transaction trans) {
        // Wilhelm comment: Declare size 2 for array to avoid
        // ArrayIndexOutOfBoundsException when accessing since current size is 0.

        // Wilhelm comment: return1 is not a descriptive name and is similar to
        // statement return. Suggestion of name change: TimestampAndExpiryDate

        String[] return1 = new String[] {};
        LocalDateTime now = LocalDateTime.now();
        return1[0] = now.toString();
        // Wilhelm comment: Already have LocalDateTime.now() as a variable, reuse it.

        return1[1] = LocalDateTime.now().plusDays(60).toString();

        return return1;

    }

    // Wilhelm comment: Remove unused method. If used set
    // private since it only seems to be used within the class.
    public String fixDescription(String desc) {
        String newDesc = "Transaction [" + desc + "]";
        return newDesc;
    }

    // Wilhelm comment: @Override missing.Change parameter type Account to Object to
    // override the method. Check if object is an instance of this class or
    // if object is null. Also change variable name o to obj since its standard java
    // practice.

    // Override the equals method
    public boolean equals(Account o) {

        // Wilhelm comment: This compares references not accountNumber. Change to
        // .equals() to properly compare strings.

        return o.getAccountNumber() == getAccountNumber(); // check account numbers are the same
    }

    // Wilhelm comment: Always override hashcode if equals is overriden.
}
