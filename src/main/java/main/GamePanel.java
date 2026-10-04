package main;

import Inputs.Keyinputs;
import Inputs.mouseInputs;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import static utilities.constants.playerConstants.*;

public class GamePanel extends JPanel {
    private static final int WIDTH = 1280, HEIGHT = 800;
    private static final int WORLD_WIDTH = 3000;
    private static final int PLAYER_WIDTH = 112, PLAYER_HEIGHT = 70;
    private static final int ENEMY_WIDTH = 104, ENEMY_HEIGHT = 65;
    private static final int PLAYER_HIT_X = 28, PLAYER_HIT_Y = 5;
    private static final int PLAYER_HIT_W = 56, PLAYER_HIT_H = 60;
    private static final double GRAVITY = 0.62, JUMP_SPEED = 13.5, MOVE_SPEED = 5.0;

    private enum Screen { MENU, PLAYING, PAUSED, GAME_OVER }
    private Screen screen = Screen.MENU;
    private BufferedImage background;
    private BufferedImage[][] playerFrames, enemyFrames;
    private BufferedImage bruteImage, wispImage, bossImage;
    private double playerX = 100, playerY = 0, velocityY;
    private int cameraX, playerHealth = 100, maxHealth = 100, playerDamage = 45, playerAction = IDLE;
    private int animationTick, animationFrame, invulnerabilityTicks;
    private int attackTicks, attackCooldown, hitTicks, kills, wave, level = 1, xp, xpToNext = 80;
    private int waveBreakTicks, waveBannerTicks, levelUpTicks, bombSpawnTicks;
    private int explosionTicks;
    private double explosionX, explosionY;
    private boolean facingLeft, grounded, jumpQueued;
    private boolean up, down, left, right;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final Random random = new Random();

    public GamePanel() {
        setFocusable(true);
        setBackground(new Color(20, 30, 49));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        buildLevel();
        addKeyListener(new Keyinputs(this));
        mouseInputs mouse = new mouseInputs(this);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        try {
            background = readImage("/res/forest_background.png");
            playerFrames = readSheet("/res/player_sprites.png");
            enemyFrames = readSheet("/res/hitAnimations.png");
            bruteImage = readImage("/res/brute_bandit.png");
            wispImage = readImage("/res/forest_wisp.png");
            bossImage = readImage("/res/bandit_king.png");
        } catch (IOException e) {
            System.err.println("Game art could not be loaded: " + e.getMessage());
        }
    }

    private void buildLevel() {
        // One-way platforms: land on top, jump up through the underside.
        platforms.add(new Platform(0, 714, WORLD_WIDTH, 100, true));
        platforms.add(new Platform(210, 604, 220, 22, false));
        platforms.add(new Platform(525, 510, 190, 22, false));
        platforms.add(new Platform(805, 616, 230, 22, false));
        platforms.add(new Platform(1120, 500, 220, 22, false));
        platforms.add(new Platform(1430, 600, 240, 22, false));
        platforms.add(new Platform(1760, 480, 210, 22, false));
        platforms.add(new Platform(2070, 590, 260, 22, false));
        platforms.add(new Platform(2440, 495, 230, 22, false));
        platforms.add(new Platform(2740, 610, 210, 22, false));
    }

    private BufferedImage readImage(String path) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing asset " + path);
            return ImageIO.read(stream);
        }
    }

    private BufferedImage[][] readSheet(String path) throws IOException {
        BufferedImage sheet = readImage(path);
        BufferedImage[][] frames = new BufferedImage[9][6];
        for (int row = 0; row < frames.length; row++)
            for (int col = 0; col < frames[row].length; col++)
                frames[row][col] = sheet.getSubimage(col * 64, row * 40, 64, 40);
        return frames;
    }

    public void setKey(int keyCode, boolean pressed) {
        if (keyCode == KeyEvent.VK_ESCAPE && pressed) {
            if (screen == Screen.PLAYING) screen = Screen.PAUSED;
            else if (screen == Screen.PAUSED) screen = Screen.PLAYING;
            else if (screen == Screen.GAME_OVER) screen = Screen.MENU;
            return;
        }
        if (keyCode == KeyEvent.VK_ENTER && pressed) {
            if (screen == Screen.MENU || screen == Screen.GAME_OVER) startGame();
            else if (screen == Screen.PAUSED) screen = Screen.PLAYING;
            return;
        }
        if ((keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_SPACE)
                && pressed && screen == Screen.PLAYING) jumpQueued = true;
        if ((keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_J) && pressed && screen == Screen.PLAYING) attack();
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) down = pressed;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) left = pressed;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) right = pressed;
    }

    public void handleMouseClick(int mx, int my) {
        if ((screen == Screen.MENU || screen == Screen.GAME_OVER) && startButton().contains(mx, my)) startGame();
        else if (screen == Screen.PAUSED) {
            if (startButton().contains(mx, my)) screen = Screen.PLAYING;
            else if (menuButton().contains(mx, my)) screen = Screen.MENU;
        }
        requestFocusInWindow();
        repaint();
    }

    public void updateGame() {
        if (screen != Screen.PLAYING) return;
        if (invulnerabilityTicks > 0) invulnerabilityTicks--;
        if (attackCooldown > 0) attackCooldown--;
        if (hitTicks > 0) hitTicks--;
        if (waveBannerTicks > 0) waveBannerTicks--;
        if (levelUpTicks > 0) levelUpTicks--;
        if (explosionTicks > 0) explosionTicks--;
        if (attackTicks > 0) {
            attackTicks--;
            if (attackTicks == 8) hitEnemies();
        }

        double move = (right ? 1 : 0) - (left ? 1 : 0);
        if (move != 0) facingLeft = move < 0;
        playerX = clamp(playerX + move * MOVE_SPEED, 0, WORLD_WIDTH - PLAYER_WIDTH);

        if (jumpQueued && grounded) {
            velocityY = -JUMP_SPEED;
            grounded = false;
        }
        jumpQueued = false;

        double oldBottom = playerY + PLAYER_HIT_Y + PLAYER_HIT_H;
        velocityY = Math.min(16, velocityY + GRAVITY);
        playerY += velocityY;
        grounded = false;
        Rectangle playerBody = playerRect();
        for (Platform platform : platforms) {
            if (velocityY >= 0 && oldBottom <= platform.y && playerBody.y + playerBody.height >= platform.y
                    && playerBody.x + playerBody.width > platform.x && playerBody.x < platform.x + platform.width) {
                playerY = platform.y - PLAYER_HIT_Y - PLAYER_HIT_H;
                velocityY = 0;
                grounded = true;
                break;
            }
        }
        if (playerY > HEIGHT + 120) damagePlayer(100);

        updatePlayerAnimation(move != 0);
        for (Enemy enemy : enemies) if (enemy.health > 0) updateEnemy(enemy);
        updatePickups();
        enemies.removeIf(enemy -> enemy.health <= 0);
        if (bombSpawnTicks > 0) bombSpawnTicks--;
        if (bombSpawnTicks == 0) {
            spawnPickupNearPlayer(PickupKind.BOMB, 0);
            bombSpawnTicks = 520 + random.nextInt(420);
        }
        if (screen == Screen.PLAYING && enemies.isEmpty()) {
            if (waveBreakTicks == 0) waveBreakTicks = 115;
            else if (--waveBreakTicks == 0) beginWave();
        }
        cameraX = (int) clamp(playerX + PLAYER_WIDTH / 2.0 - WIDTH / 2.0, 0, WORLD_WIDTH - WIDTH);
    }

    private void updateEnemy(Enemy enemy) {
        if (enemy.hitTicks > 0) enemy.hitTicks--;
        if (enemy.contactCooldown > 0) enemy.contactCooldown--;
        Rectangle playerBody = playerRect();
        Rectangle enemyBody = enemy.rect();
        double dx = playerBody.getCenterX() - enemyBody.getCenterX();
        double dy = playerBody.getCenterY() - enemyBody.getCenterY();
        boolean sameLevel = Math.abs(dy) < 190;
        if (enemy.kind == EnemyKind.WISP) {
            double distance = Math.max(1, Math.hypot(dx, dy));
            enemy.x += dx / distance * enemy.speed;
            enemy.y += dy / distance * enemy.speed;
            enemy.facingLeft = dx < 0;
        } else if (sameLevel && Math.abs(dx) > 28) {
            double step = Math.min(enemy.speed, Math.abs(dx) - 28) * Math.signum(dx);
            enemy.x += step;
            enemy.facingLeft = step < 0;
        }
        if (enemy.kind != EnemyKind.WISP) {
            enemy.velocityY = Math.min(15, enemy.velocityY + GRAVITY);
            double oldBottom = enemy.y + ENEMY_HIT_Y + ENEMY_HIT_H;
            enemy.y += enemy.velocityY;
            enemy.grounded = false;
            Rectangle body = enemy.rect();
            for (Platform platform : platforms) {
                if (enemy.velocityY >= 0 && oldBottom <= platform.y && body.y + body.height >= platform.y
                        && body.x + body.width > platform.x && body.x < platform.x + platform.width) {
                    enemy.y = platform.y - ENEMY_HIT_Y - ENEMY_HIT_H;
                    enemy.velocityY = 0;
                    enemy.grounded = true;
                    break;
                }
            }
        }
        enemy.animationTick++;
        if (enemy.animationTick >= 9) {
            enemy.animationTick = 0;
            enemy.animationFrame = (enemy.animationFrame + 1) % 6;
        }
        if (enemy.rect().intersects(playerRect()) && enemy.contactCooldown == 0) {
            damagePlayer(enemy.contactDamage);
            enemy.contactCooldown = 55;
        }
    }

    private void updatePlayerAnimation(boolean moving) {
        if (hitTicks > 0) playerAction = HIT;
        else if (attackTicks > 0) playerAction = ATTACKING_1;
        else if (!grounded) playerAction = velocityY < 0 ? JUMPING : FALLING;
        else playerAction = moving ? RUNNING : IDLE;
        animationTick++;
        if (animationTick >= 8) {
            animationTick = 0;
            animationFrame = (animationFrame + 1) % GetCatAmount(playerAction);
        }
    }

    private void attack() {
        if (attackCooldown > 0 || attackTicks > 0) return;
        attackTicks = 15;
        attackCooldown = 25;
    }

    private void hitEnemies() {
        Rectangle body = playerRect();
        int reach = 82;
        Rectangle swing = facingLeft
                ? new Rectangle(body.x - reach, body.y - 4, reach + 10, body.height + 8)
                : new Rectangle(body.x + body.width - 10, body.y - 4, reach + 10, body.height + 8);
        for (Enemy enemy : enemies) {
            if (swing.intersects(enemy.rect()) && enemy.health > 0) {
                enemy.health -= playerDamage;
                enemy.hitTicks = 10;
                enemy.x += facingLeft ? -16 : 16;
                if (enemy.health <= 0) defeatEnemy(enemy);
            }
        }
    }

    private void defeatEnemy(Enemy enemy) {
        if (enemy.rewarded) return;
        enemy.rewarded = true;
        kills++;
        pickups.add(new Pickup(PickupKind.XP, enemy.x + ENEMY_WIDTH / 2.0, enemy.y + ENEMY_HEIGHT / 2.0,
                enemy.xpReward));
        if (enemy.kind == EnemyKind.BOSS) {
            PickupKind reward = switch (random.nextInt(3)) {
                case 0 -> PickupKind.BOMB;
                case 1 -> PickupKind.HEART;
                default -> PickupKind.BLADE;
            };
            pickups.add(new Pickup(reward, enemy.x + ENEMY_WIDTH / 2.0 + 22, enemy.y + ENEMY_HEIGHT / 2.0,
                    reward == PickupKind.BLADE ? 20 : 0));
        } else if (random.nextInt(100) < 8) {
            PickupKind reward = random.nextBoolean() ? PickupKind.BOMB : PickupKind.HEART;
            pickups.add(new Pickup(reward, enemy.x + ENEMY_WIDTH / 2.0, enemy.y + ENEMY_HEIGHT / 2.0, 0));
        }
    }

    private void updatePickups() {
        Rectangle body = playerRect();
        for (Pickup pickup : pickups) {
            pickup.bob += 0.11;
            Rectangle pickupRect = new Rectangle((int) pickup.x - 15, (int) (pickup.y + Math.sin(pickup.bob) * 4) - 15, 30, 30);
            if (!pickupRect.intersects(body)) continue;
            pickup.collected = true;
            switch (pickup.kind) {
                case XP -> gainXp(pickup.value);
                case HEART -> playerHealth = Math.min(maxHealth, playerHealth + 38);
                case BLADE -> playerDamage += pickup.value;
                case BOMB -> detonateBomb(pickup.x, pickup.y);
            }
        }
        pickups.removeIf(pickup -> pickup.collected);
    }

    private void gainXp(int amount) {
        xp += amount;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
            playerDamage += 8;
            maxHealth += 10;
            playerHealth = Math.min(maxHealth, playerHealth + 25);
            xpToNext = Math.min(100_000, 80 + level * 35);
            levelUpTicks = 150;
        }
    }

    private void detonateBomb(double x, double y) {
        explosionX = x;
        explosionY = y;
        explosionTicks = 30;
        for (Enemy enemy : enemies) {
            double distance = Math.hypot(enemy.x + ENEMY_WIDTH / 2.0 - x, enemy.y + ENEMY_HEIGHT / 2.0 - y);
            if (distance <= 430 && enemy.health > 0) {
                enemy.health = 0;
                defeatEnemy(enemy);
            }
        }
    }

    private void spawnPickupNearPlayer(PickupKind kind, int value) {
        double offset = 180 + random.nextInt(360);
        double spawnX = clamp(playerX + (random.nextBoolean() ? offset : -offset), 35, WORLD_WIDTH - 35);
        Platform platform = platformUnder(spawnX);
        pickups.add(new Pickup(kind, spawnX, platform.y - 22, value));
    }

    private void damagePlayer(int amount) {
        if (invulnerabilityTicks > 0 || screen != Screen.PLAYING) return;
        playerHealth = Math.max(0, playerHealth - amount);
        invulnerabilityTicks = 55;
        hitTicks = 12;
        if (playerHealth == 0) screen = Screen.GAME_OVER;
    }

    private void startGame() {
        playerX = 90;
        playerY = platforms.get(0).y - PLAYER_HIT_Y - PLAYER_HIT_H;
        velocityY = 0;
        grounded = true;
        playerHealth = maxHealth = 100;
        playerDamage = 45;
        kills = wave = xp = 0;
        level = 1;
        xpToNext = 80;
        cameraX = 0;
        attackTicks = attackCooldown = hitTicks = invulnerabilityTicks = 0;
        enemies.clear();
        pickups.clear();
        waveBreakTicks = waveBannerTicks = levelUpTicks = 0;
        screen = Screen.PLAYING;
        beginWave();
        requestFocusInWindow();
        repaint();
    }

    private void beginWave() {
        wave++;
        waveBannerTicks = 165;
        waveBreakTicks = 0;
        bombSpawnTicks = 480 + random.nextInt(360);
        int count = Math.min(3 + wave * 2, 13);
        for (int i = 0; i < count; i++) {
            EnemyKind kind = EnemyKind.BANDIT;
            if (wave >= 2 && i % 4 == 1) kind = EnemyKind.BRUTE;
            if (wave >= 3 && i % 4 == 2) kind = EnemyKind.WISP;
            double ex = clamp(playerX + 260 + i * 118, 45, WORLD_WIDTH - 150);
            enemies.add(makeEnemy(kind, ex));
        }
        double bossX = clamp(playerX + 540, 120, WORLD_WIDTH - 200);
        enemies.add(makeEnemy(EnemyKind.BOSS, bossX));
    }

    private Enemy makeEnemy(EnemyKind kind, double x) {
        double scale = Math.pow(1.16, Math.max(0, wave - 1));
        int baseHealth = switch (kind) {
            case BANDIT -> 70;
            case WISP -> 58;
            case BRUTE -> 150;
            case BOSS -> 320;
        };
        double baseSpeed = switch (kind) {
            case BANDIT -> 1.0;
            case WISP -> 1.8;
            case BRUTE -> 0.67;
            case BOSS -> 1.08;
        };
        int health = (int) Math.min(1_000_000, baseHealth * scale);
        Enemy enemy = new Enemy(kind, x, 0, Math.max(1, health),
                Math.min(3.3, baseSpeed + wave * 0.055), Math.min(120, 8 + wave * 2));
        if (kind == EnemyKind.BRUTE) enemy.contactDamage = Math.min(160, 13 + wave * 3);
        if (kind == EnemyKind.WISP) enemy.contactDamage = Math.min(100, 7 + wave);
        if (kind == EnemyKind.BOSS) enemy.contactDamage = Math.min(220, 22 + wave * 3);
        enemy.xpReward = kind == EnemyKind.BOSS ? 45 + wave * 6 : 12 + wave * 2;
        enemy.maxHealth = enemy.health;
        Platform platform = platformUnder(enemy.x + ENEMY_WIDTH / 2);
        enemy.y = platform.y - ENEMY_HIT_Y - ENEMY_HIT_H;
        return enemy;
    }

    private Platform platformUnder(double x) {
        Platform result = platforms.get(0);
        for (Platform platform : platforms) {
            if (platform.x <= x && x < platform.x + platform.width && platform.y < result.y) result = platform;
        }
        return result;
    }

    private Rectangle playerRect() {
        return new Rectangle((int) playerX + PLAYER_HIT_X, (int) playerY + PLAYER_HIT_Y, PLAYER_HIT_W, PLAYER_HIT_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D screenGraphics = (Graphics2D) g.create();
        screenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(screenGraphics);
        if (screen != Screen.MENU) {
            Graphics2D world = (Graphics2D) screenGraphics.create();
            world.translate(-cameraX, 0);
            drawPlatforms(world);
            drawPickups(world);
            drawEnemies(world);
            drawPlayer(world);
            drawExplosion(world);
            world.dispose();
            drawHud(screenGraphics);
            if (screen == Screen.PAUSED) drawOverlay(screenGraphics, "PAUSED", "Take a breather.", "RESUME", true);
            if (screen == Screen.GAME_OVER) drawOverlay(screenGraphics, "GAME OVER", "Bandits defeated: " + kills, "PLAY AGAIN", false);
        } else drawMenu(screenGraphics);
        screenGraphics.dispose();
    }

    private void drawBackground(Graphics2D g) {
        if (background == null) {
            g.setColor(new Color(20, 30, 49));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            return;
        }
        int parallaxX = (int) (cameraX * 0.25);
        int bgWidth = 1800;
        g.drawImage(background, -parallaxX, 0, bgWidth, HEIGHT, null);
        g.drawImage(background, bgWidth - parallaxX, 0, bgWidth, HEIGHT, null);
    }

    private void drawPlatforms(Graphics2D g) {
        for (Platform platform : platforms) {
            if (platform.ground) {
                g.setColor(new Color(39, 48, 44));
                g.fillRect(platform.x, platform.y, platform.width, HEIGHT - platform.y + 100);
                g.setColor(new Color(103, 151, 83));
                g.fillRect(platform.x, platform.y, platform.width, 12);
                g.setColor(new Color(57, 105, 62));
                for (int px = 0; px < platform.width; px += 54) g.fillRect(platform.x + px, platform.y + 12, 3, 10);
            } else {
                g.setColor(new Color(97, 61, 54));
                g.fillRoundRect(platform.x, platform.y, platform.width, platform.height + 9, 8, 8);
                g.setColor(new Color(181, 125, 73));
                g.fillRoundRect(platform.x, platform.y, platform.width, 10, 8, 8);
                g.setColor(new Color(124, 78, 53));
                g.setStroke(new BasicStroke(2));
                for (int px = platform.x + 18; px < platform.x + platform.width; px += 42)
                    g.drawLine(px, platform.y + 12, px, platform.y + platform.height + 7);
                g.setColor(new Color(222, 169, 104));
                for (int px = platform.x + 12; px < platform.x + platform.width - 5; px += 42)
                    g.fillOval(px, platform.y + 2, 4, 4);
            }
        }
    }

    private void drawPlayer(Graphics2D g) {
        if (invulnerabilityTicks > 0 && invulnerabilityTicks % 8 < 4) return;
        drawSprite(g, playerFrames, playerAction, animationFrame, (int) playerX, (int) playerY,
                PLAYER_WIDTH, PLAYER_HEIGHT, facingLeft);
        if (attackTicks > 4 && attackTicks < 13) {
            g.setColor(new Color(255, 239, 157, 210));
            g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = (int) playerX + PLAYER_WIDTH / 2 + (facingLeft ? -55 : 55);
            int cy = (int) playerY + PLAYER_HEIGHT / 2;
            g.drawArc(cx - 35, cy - 35, 70, 70, facingLeft ? 210 : -30, 150);
        }
    }

    private void drawEnemies(Graphics2D g) {
        for (Enemy enemy : enemies) {
            if (enemy.health <= 0) continue;
            int row = enemy.hitTicks > 0 ? HIT : (Math.abs(enemy.x - playerX) > 30 ? RUNNING : IDLE);
            int frame = enemy.animationFrame % GetCatAmount(row);
            drawEnemyModel(g, enemy, row, frame);
            if (enemy.hitTicks > 0) {
                g.setColor(new Color(255, 90, 75, 130));
                g.fillOval((int) enemy.x + 8, (int) enemy.y + 8, ENEMY_WIDTH - 16, ENEMY_HEIGHT - 16);
            }
            int barWidth = enemy.kind == EnemyKind.BOSS ? 150 : enemy.kind == EnemyKind.BRUTE ? 82 : 62;
            int barY = switch (enemy.kind) {
                case BOSS -> (int) enemy.y - 178;
                case BRUTE -> (int) enemy.y - 69;
                case WISP -> (int) enemy.y - 35;
                case BANDIT -> (int) enemy.y - 12;
            };
            drawBar(g, (int) enemy.x + (ENEMY_WIDTH - barWidth) / 2, barY, barWidth, 7,
                    enemy.health, enemy.maxHealth, new Color(238, 95, 79));
        }
    }

    private void drawEnemyModel(Graphics2D g, Enemy enemy, int row, int frame) {
        if (enemy.kind == EnemyKind.BRUTE && bruteImage != null) {
            drawSingleSprite(g, bruteImage, (int) enemy.x - 18, (int) enemy.y - 59, 142, 122, enemy.facingLeft);
        } else if (enemy.kind == EnemyKind.WISP && wispImage != null) {
            int bob = (int) Math.round(Math.sin(enemy.animationTick * 0.18) * 5);
            drawSingleSprite(g, wispImage, (int) enemy.x - 2, (int) enemy.y - 17 + bob, 110, 102, enemy.facingLeft);
        } else if (enemy.kind == EnemyKind.BOSS && bossImage != null) {
            drawSingleSprite(g, bossImage, (int) enemy.x - 54, (int) enemy.y - 151, 212, 212, enemy.facingLeft);
        } else {
            drawSprite(g, enemyFrames, row, frame, (int) enemy.x, (int) enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT, enemy.facingLeft);
        }
    }

    private void drawSingleSprite(Graphics2D g, BufferedImage sprite, int x, int y, int width, int height, boolean flip) {
        if (flip) g.drawImage(sprite, x + width, y, -width, height, null);
        else g.drawImage(sprite, x, y, width, height, null);
    }

    private void drawPickups(Graphics2D g) {
        for (Pickup pickup : pickups) {
            int px = (int) pickup.x;
            int py = (int) (pickup.y + Math.sin(pickup.bob) * 4);
            switch (pickup.kind) {
                case XP -> {
                    g.setColor(new Color(78, 214, 255, 80));
                    g.fillOval(px - 15, py - 15, 30, 30);
                    g.setColor(new Color(99, 225, 255));
                    g.fillOval(px - 7, py - 7, 14, 14);
                    g.setColor(Color.WHITE);
                    g.fillOval(px - 3, py - 5, 5, 5);
                }
                case BOMB -> {
                    g.setColor(new Color(36, 32, 42));
                    g.fillOval(px - 11, py - 10, 22, 22);
                    g.setColor(new Color(237, 93, 62));
                    g.setStroke(new BasicStroke(3));
                    g.drawLine(px + 4, py - 8, px + 10, py - 15);
                    g.setColor(new Color(255, 213, 103));
                    g.fillOval(px + 7, py - 19, 6, 6);
                }
                case HEART -> {
                    g.setColor(new Color(242, 83, 104));
                    g.fillOval(px - 12, py - 8, 14, 14);
                    g.fillOval(px - 1, py - 8, 14, 14);
                    int[] xs = { px - 12, px + 13, px };
                    int[] ys = { py, py, py + 15 };
                    g.fillPolygon(xs, ys, 3);
                }
                case BLADE -> {
                    g.setColor(new Color(255, 227, 133));
                    g.fillOval(px - 13, py - 13, 26, 26);
                    g.setColor(new Color(73, 49, 37));
                    g.setStroke(new BasicStroke(3));
                    g.drawLine(px - 5, py + 7, px + 7, py - 7);
                    g.drawLine(px - 7, py + 3, px - 1, py + 9);
                }
            }
        }
    }

    private void drawExplosion(Graphics2D g) {
        if (explosionTicks <= 0) return;
        int radius = (31 - explosionTicks) * 16 + 24;
        g.setColor(new Color(255, 183, 63, 90));
        g.fillOval((int) explosionX - radius, (int) explosionY - radius, radius * 2, radius * 2);
        g.setColor(new Color(255, 231, 143, 180));
        g.setStroke(new BasicStroke(5));
        g.drawOval((int) explosionX - radius, (int) explosionY - radius, radius * 2, radius * 2);
    }

    private void drawSprite(Graphics2D g, BufferedImage[][] sheet, int action, int frame,
            int x, int y, int width, int height, boolean flip) {
        if (sheet == null) {
            g.setColor(new Color(222, 185, 132));
            g.fillOval(x, y, width, height);
            return;
        }
        BufferedImage sprite = sheet[action][frame % GetCatAmount(action)];
        if (flip) g.drawImage(sprite, x + width, y, -width, height, null);
        else g.drawImage(sprite, x, y, width, height, null);
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(10, 15, 24, 225));
        g.fillRoundRect(26, 22, 385, 112, 18, 18);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("DAAKU BILLI", 46, 49);
        drawBar(g, 46, 61, 270, 17, playerHealth, maxHealth, new Color(87, 210, 129));
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("HP " + playerHealth + "/" + maxHealth + "    LV " + level + "    KOs " + kills, 46, 99);
        drawBar(g, 46, 108, 270, 8, xp, xpToNext, new Color(83, 197, 255));
        g.setColor(new Color(255, 255, 255, 220));
        g.drawString("A/D move  •  SPACE jump  •  X attack  •  ESC pause", 755, 42);
        g.setFont(new Font("SansSerif", Font.BOLD, 19));
        g.setColor(new Color(255, 231, 173));
        g.drawString("WAVE " + wave, 1130, 42);
        Enemy boss = enemies.stream().filter(enemy -> enemy.kind == EnemyKind.BOSS && enemy.health > 0).findFirst().orElse(null);
        if (boss != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            String bossName = "BANDIT KING";
            int labelX = (WIDTH - g.getFontMetrics().stringWidth(bossName)) / 2;
            g.drawString(bossName, labelX, 73);
            drawBar(g, 480, 83, 320, 12, boss.health, boss.maxHealth, new Color(214, 68, 76));
        }
        if (waveBannerTicks > 0) {
            g.setFont(new Font("Serif", Font.BOLD, 40));
            g.setColor(new Color(255, 228, 163, Math.min(255, waveBannerTicks * 2)));
            String banner = "WAVE " + wave;
            g.drawString(banner, (WIDTH - g.getFontMetrics().stringWidth(banner)) / 2, 170);
        }
        if (levelUpTicks > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.setColor(new Color(132, 241, 255));
            g.drawString("LEVEL UP! Damage and max health increased", 780, 75);
        }
        if (waveBreakTicks > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.setColor(new Color(255, 238, 185));
            g.drawString("Wave " + (wave + 1) + " incoming...", 515, 160);
        }
    }

    private void drawBar(Graphics2D g, int x, int y, int width, int height, int value, int max, Color fill) {
        g.setColor(new Color(20, 23, 25));
        g.fillRoundRect(x, y, width, height, height, height);
        g.setColor(fill);
        int filledWidth = (int) ((long) width * Math.max(0, value) / Math.max(1, max));
        g.fillRoundRect(x, y, Math.min(width, filledWidth), height, height, height);
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(7, 12, 19, 115));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(255, 223, 146));
        g.setFont(new Font("Serif", Font.BOLD, 76));
        center(g, "DAAKU BILLI", 270);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 23));
        center(g, "The moonlit bandit run", 325);
        drawButton(g, "START GAME", startButton());
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        center(g, "A/D move   •   SPACE jump   •   X attack", 520);
        center(g, "Collect blue XP orbs to level up. Bombs clear nearby enemies!", 555);
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle, String button, boolean paused) {
        g.setColor(new Color(5, 9, 15, 190));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(255, 223, 146));
        g.setFont(new Font("Serif", Font.BOLD, 62));
        center(g, title, 290);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        center(g, subtitle, 345);
        drawButton(g, button, startButton());
        if (paused) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 17));
            center(g, "Press ESC to resume", 465);
            drawButton(g, "MAIN MENU", menuButton());
        } else {
            g.setFont(new Font("SansSerif", Font.PLAIN, 17));
            center(g, "Press ENTER to try again", 465);
        }
    }

    private void drawButton(Graphics2D g, String text, Rectangle bounds) {
        g.setColor(new Color(191, 117, 63));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g.setColor(new Color(255, 222, 164));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString(text, bounds.x + (bounds.width - g.getFontMetrics().stringWidth(text)) / 2, bounds.y + 41);
    }

    private void center(Graphics2D g, String text, int baseline) {
        g.drawString(text, (WIDTH - g.getFontMetrics().stringWidth(text)) / 2, baseline);
    }

    private Rectangle startButton() { return new Rectangle(500, 390, 280, 64); }
    private Rectangle menuButton() { return new Rectangle(500, 490, 280, 64); }
    private double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    private static final int ENEMY_HIT_X = 28, ENEMY_HIT_Y = 6, ENEMY_HIT_W = 48, ENEMY_HIT_H = 53;

    private enum EnemyKind { BANDIT, BRUTE, WISP, BOSS }
    private enum PickupKind { XP, BOMB, HEART, BLADE }

    private static final class Platform {
        final int x, y, width, height;
        final boolean ground;
        Platform(int x, int y, int width, int height, boolean ground) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.ground = ground;
        }
        Rectangle rect() { return new Rectangle(x, y, width, height); }
    }

    private static final class Enemy {
        double x, y, velocityY, speed;
        int health, maxHealth, contactCooldown, hitTicks, animationTick, animationFrame, contactDamage = 10, xpReward;
        boolean grounded, facingLeft, rewarded;
        final EnemyKind kind;
        Enemy(EnemyKind kind, double x, double y, int health, double speed, int contactDamage) {
            this.kind = kind; this.x = x; this.y = y; this.health = health; this.maxHealth = health;
            this.speed = speed; this.contactDamage = contactDamage;
        }
        Rectangle rect() { return new Rectangle((int) x + ENEMY_HIT_X, (int) y + ENEMY_HIT_Y, ENEMY_HIT_W, ENEMY_HIT_H); }
    }

    private static final class Pickup {
        final PickupKind kind;
        final double x, y;
        final int value;
        double bob;
        boolean collected;
        Pickup(PickupKind kind, double x, double y, int value) {
            this.kind = kind; this.x = x; this.y = y; this.value = value;
        }
    }
}
