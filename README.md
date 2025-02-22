# 🪓 Woodcutter Bot - RuneMate

This repository provides a **fully structured development environment** for RuneMate bots, including a **woodcutter bot** to learn from.

## 🚀 Getting Started

### ✅ Prerequisites
- **IntelliJ IDEA** (Recommended IDE) → [Download Here](https://www.jetbrains.com/idea/download/?section=windows)
- **Java JDK 17** (You can install this automatically upon making a New Java Project in IntelliJ by selecting the JDK dropdown list and selecting JDK 17 (Temurin).)
- **RuneMate Client** (Download from [RuneMate](https://www.runemate.com))

---

## 🔧 Installation & Setup

### **1️⃣ Clone the Repository**
Open a terminal inside IntelliJ (Ctrl+`) and run:
```
git clone https://github.com/DaBest890/cody-bots.git
```

Then open the cloned folder in IntelliJ.

2️⃣ Set Up IntelliJ
Open IntelliJ IDEA
Click "New Project" → Select Java
Ensure the project location is correct:
``` 
C:\Users\Your-User-Name\IdeaProjects\cody-bots\
```

3️⃣ Run the Bot
To launch the bot in RuneMate, open the IntelliJ Terminal (Shortcut: CTRL + `) and run:

``` 
./gradlew runClient
```

RuneMate should now open, and you should see the woodcutter bot in the bot list.

❓ Troubleshooting
Bot Not Appearing in RuneMate?
Check if the compiled bot is in the correct directory:
``` pgsql
C:\Users\Your-User-Name\IdeaProjects\cody-bots\build\libs
```

Make sure Gradle built the bot successfully:
``` 
./gradlew build
```

Restart RuneMate and try again.
By default, IntelliJ projects are stored in:

```
C:\Users\Your-User-Name\IdeaProjects\
```
You can set a different location, but make sure to adjust paths accordingly.


📌 Summary
✅ Clone the repo
✅ Set up IntelliJ
✅ Run ./gradlew runClient to launch the bot
✅ Check build/libs if the bot doesn’t appear in RuneMate
🚀 Now you’re ready to start bot development! 🔥
