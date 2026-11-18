package src.main.java;

public class Main {

    public static void main(String[] args) {

        Transaction income = new Transaction(
                "Monthly income",
                1000.00,
                "income",
                "Savings"
        );

        Transaction expense = new Transaction(
                "Groceries",
                35.50,
                "expense",
                "Food"
        );

        System.out.println("Welcome to PennyWise!");
        System.out.println(income);
        System.out.println(expense);

        System.out.println("\n--- Category Testing ---");

        BudgetCategory food = new BudgetCategory("Food");

        food.addMoney(300.00);

        System.out.println(food);

        food.spendMoney(35.50);

        System.out.println(food);
        
    }
}
