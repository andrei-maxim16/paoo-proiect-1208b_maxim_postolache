import javax.swing.*;
import java.awt.*;

class GameFrame extends JFrame {
    public GameFrame(boolean withFadeIn) {
        setTitle("Shadow Heist - Map");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GameMap gameMap = new GameMap(this);

        // Setăm culoarea de fundal ca să nu mai apară spațiul gri/alb

        setContentPane(gameMap);

        setUndecorated(false);
        pack();                    // Ajustează fereastra după dimensiunea hărții
        setResizable(false);       // Nu permitem redimensionarea
        setLocationRelativeTo(null); // Centrează pe ecran
        setVisible(true);
        setSize(736,576);
        if (withFadeIn) {
            FadePanel fade = new FadePanel();
            fade.setBounds(0, 0, getWidth(), getHeight());
            fade.setOpaque(false);
            setGlassPane(fade);
            fade.setVisible(true);

            new Thread(() -> {
                for (int i = 20; i >= 0; i--) {
                    float alpha = i / 20f;
                    fade.setOpacity(alpha);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                fade.setVisible(false);
            }).start();
        }
    }

    public GameFrame() {
        this(false);
    }
}
