# Daaku Billi

A Java Swing side-scrolling platform game about a cat fighting forest bandits.

## Run it

Open the folder in VS Code and run `main.MainClass` from the Java Projects view. The project uses `src/main/java` as its source folder and `bin` as its output folder.

## Controls

- **A / D** or **Left / Right**: move
- **Space**, **W**, or **Up**: jump
- **X** or **J**: attack
- **Esc**: pause / resume

## Waves and upgrades

Clear each wave to bring on the next. Enemy health, speed, and damage scale with each wave; armored brutes join from wave 2, forest wisps from wave 3, and a Bandit King boss appears every wave.

Enemies drop XP orbs. Leveling increases attack damage and maximum health. Bosses always drop a random bomb, heart, or blade upgrade; regular enemies can drop power-ups too. Bombs also appear at random and explode when collected, defeating enemies within range.

Art assets are in `src/main/java/res` and are loaded from the classpath under `/res`.
