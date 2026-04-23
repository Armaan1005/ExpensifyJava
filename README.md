# Expensify 🌿

Expensify is a sophisticated, high-fidelity personal finance tracker built with Java Swing. It features a premium "Dark Pine" aesthetic, dynamic dashboard visualizations, and a focus on elegant typography using the *Parisienne* and *Hedvig Letters Serif* font families.

![Expensify Dashboard](https://github.com/Armaan1005/ExpensifyJava/raw/main/screenshots/dashboard_preview.png)

## ✨ Key Features

- **Dynamic Dashboard:** Real-time financial overview with beautiful gradient cards and interactive charts.
- **Dark Pine Aesthetic:** A meticulously crafted dark theme using a curated palette of deep greens and teals.
- **Expense Management:** Easily add, edit, and categorize your transactions with a high-performance table view.
- **Budget Tracking:** Set monthly limits and track your progress with visual status indicators.
- **Secure Authentication:** Built-in user registration and login system with SHA-256 password hashing.
- **SQLite Integration:** Zero-configuration local database for lightning-fast performance and offline access.

## 🛠️ Technology Stack

- **Core:** Java 17+
- **UI Framework:** Java Swing & AWT
- **Database:** SQLite (via JDBC)
- **Design:** Modern CSS-inspired styling, Custom Rounded Components, and SVG-powered icons.
- **Build System:** Maven / PowerShell Scripts

## 🚀 Getting Started

### Prerequisites

- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/downloads/) or higher.
- SQLite JDBC Driver (included in `lib/`).

### Launching the App

You can launch the application directly using the following PowerShell command:

```powershell
javac -cp "lib\*" -d out -encoding UTF-8 (Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }); java -cp "out;lib\*" com.expensetracker.Main
```

## 🎨 Design Philosophy

Expensify was designed to prove that Java Swing applications can look modern, premium, and state-of-the-art. It utilizes glassmorphism-inspired transparency, smooth gradients, and carefully selected typography to create a "fintech-first" user experience.

---
Developed with ❤️ by [Armaan Patel](https://github.com/Armaan1005)
