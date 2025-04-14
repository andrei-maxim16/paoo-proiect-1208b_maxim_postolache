import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class GameMap extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,5,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,4,0,1},
            {1,0,1,1,0,1,1,0,0,1,1,0,0,1,1,0,0,1,0,1},
            {1,0,3,0,2,0,1,0,0,8,0,0,1,0,2,0,3,0,0,1},
            {1,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,0,1},
            {1,0,0,0,1,0,1,1,1,1,1,1,0,1,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,2,0,0,0,1,1,0,0,2,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private final GameFrame parentFrame;
    private int playerX = 1, playerY = 1; // tile position
    private int offsetX = 0, offsetY = 0;
    private Image nyxSprite; // pixel offset
    private boolean isAnimating = false;

    public GameMap(GameFrame parentFrame) {
        try {
            nyxSprite = new ImageIcon("assets/sprites/nyx_sprite.png").getImage();
        } catch (Exception e) {
            System.out.println("Nyx sprite not found.");
        }
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 10, 80, 30);
        add(backButton);

        backButton.addActionListener(e -> {
            FadePanel fadeOut = new FadePanel();
            fadeOut.setBounds(0, 0, getWidth(), getHeight());
            fadeOut.setOpaque(false);
            parentFrame.setGlassPane(fadeOut);
            fadeOut.setVisible(true);

            new Thread(() -> {
                for (int i = 0; i <= 20; i++) {
                    float alpha = i / 20f;
                    fadeOut.setOpacity(alpha);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                parentFrame.dispose();
                new MainMenu();
            }).start();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                switch (map[row][col]) {
                    case 0 -> g.setColor(Color.LIGHT_GRAY); // Podea
                    case 1 -> g.setColor(Color.DARK_GRAY);  // Perete
                    case 2 -> g.setColor(Color.RED);        // Capcană electrică
                    case 3 -> g.setColor(new Color(30, 30, 60)); // Zonă de umbră
                    case 4 -> g.setColor(Color.GREEN);      // Terminal hacking
                    case 5 -> g.setColor(Color.LIGHT_GRAY); // Spawn (fundal)
                    case 6 -> g.setColor(Color.ORANGE);     // Obiect colectabil
                    case 8 -> g.setColor(Color.YELLOW);     // Cameră / Senzor
                    default -> g.setColor(Color.PINK);      // Tile necunoscut
                }
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        // Desenează jucătorul (cerc mov animat)
        if (nyxSprite != null) {
            g.drawImage(nyxSprite, playerX * TILE_SIZE + offsetX, playerY * TILE_SIZE + offsetY, TILE_SIZE, TILE_SIZE, this);
        } else {
            g.setColor(new Color(150, 0, 255));
            g.fillOval(playerX * TILE_SIZE + offsetX + 4, playerY * TILE_SIZE + offsetY + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(map[0].length * TILE_SIZE, map.length * TILE_SIZE);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (isAnimating) return;

        final int[] dx = {0}, dy = {0};
        switch (e.getKeyChar()) {
            case 'w' -> dy[0] = -1;
            case 's' -> dy[0] = 1;
            case 'a' -> dx[0] = -1;
            case 'd' -> dx[0] = 1;
        }
        int newX = playerX + dx[0];
        int newY = playerY + dy[0];

        if (map[newY][newX] != 1) {
            isAnimating = true;
            int steps = 2;
            int stepSize = TILE_SIZE / steps;
            Timer timer = new Timer(10, null);
            final int[] count = {0};
            timer.addActionListener(evt -> {
                offsetX -= dx[0] * stepSize;
                offsetY -= dy[0] * stepSize;
                count[0]++;
                repaint();
                if (count[0] >= steps) {
                    timer.stop();
                    playerX = newX;
                    playerY = newY;
                    offsetX = 0;
                    offsetY = 0;
                    isAnimating = false;
                    repaint();
                }
            });
            offsetX = dx[0] * TILE_SIZE;
            offsetY = dy[0] * TILE_SIZE;
            timer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}