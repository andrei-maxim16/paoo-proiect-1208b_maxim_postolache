import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

class GameMap extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 8;
    private Image tileset;

    private final GameFrame parentFrame;
    private int playerX = 1, playerY = 1;
    private int offsetX = 0, offsetY = 0;
    private Image nyxSprite;
    private boolean isAnimating = false;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int[][] layer2;

    public GameMap(GameFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/void-tiles.png").getImage();
        layer1 = loadCSV("assets/maps/harta_principala._Tile Layer 1.csv");
        layer2 = loadCSV("assets/maps/harta_principala._Tile Layer 2.csv");

        try {
            nyxSprite = new ImageIcon("assets/sprites/nyx_sprite.png").getImage();
        } catch (Exception e) {
            System.out.println("Nyx sprite not found.");
        }

        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Inițializare meniului de pauză
        pauseMenu = new PauseMenuPanel(
                () -> { parentFrame.dispose(); new MainMenu(); },
                () -> { JOptionPane.showMessageDialog(this, "Load not implemented yet."); },
                () -> { JOptionPane.showMessageDialog(this, "Options coming soon!"); },
                () -> { System.exit(0); }
        );
        pauseMenu.setBounds(0, 0, 736, 576);
        pauseMenu.setVisible(false);
        add(pauseMenu);
    }

    private int[][] loadCSV(String path) {
        List<int[]> rows = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i].trim());
                }
                rows.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows.toArray(new int[0][0]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawLayer(g, layer1);
        drawLayer(g, layer2);

        if (nyxSprite != null) {
            g.drawImage(nyxSprite, playerX * TILE_SIZE + offsetX, playerY * TILE_SIZE + offsetY, TILE_SIZE, TILE_SIZE, this);
        }
    }

    private void drawLayer(Graphics g, int[][] layer) {
        for (int row = 0; row < layer.length; row++) {
            for (int col = 0; col < layer[0].length; col++) {
                int tileId = layer[row][col];
                if (tileId != 0) {
                    int tileCol = tileId % TILES_PER_ROW;
                    int tileRow = tileId / TILES_PER_ROW;
                    g.drawImage(tileset,
                            col * TILE_SIZE, row * TILE_SIZE, (col + 1) * TILE_SIZE, (row + 1) * TILE_SIZE,
                            tileCol * TILE_SIZE, tileRow * TILE_SIZE, (tileCol + 1) * TILE_SIZE, (tileRow + 1) * TILE_SIZE,
                            this);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(layer1[0].length * TILE_SIZE, layer1.length * TILE_SIZE);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            menuVisible = !menuVisible;
            pauseMenu.setVisible(menuVisible);

            if (menuVisible) {
                pauseMenu.requestFocusInWindow();  // Asigură focus pe meniu
            } else {
                requestFocusInWindow();  // Revenim cu focus pe hartă
            }

            repaint();  // Actualizăm vizual componenta
            return;
        }

        if (isAnimating || menuVisible) return;

        final int[] dx = {0}, dy = {0};
        switch (e.getKeyChar()) {
            case 'w' -> dy[0] = -1;
            case 's' -> dy[0] = 1;
            case 'a' -> dx[0] = -1;
            case 'd' -> dx[0] = 1;
        }

        int newX = playerX + dx[0];
        int newY = playerY + dy[0];

        if (newY >= 0 && newY < layer1.length && newX >= 0 && newX < layer1[0].length) {
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
