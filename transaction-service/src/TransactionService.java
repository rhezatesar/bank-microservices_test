package transaction;

public class TransactionService {
    public static void main(String[] args) {
        String input = "test";
        String query = "SELECT * FROM transactions WHERE name = '" + input + "'"; // SQL injection
        System.out.println(query);
    }
}
