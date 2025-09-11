package game;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapPanel extends JPanel {
    private final Game game;

    private final Map<Room, int[]> roomToGrid = new HashMap<>();

    public MapPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(260, 400));
        setBackground(Color.WHITE);
    }

    private void ensureLayout() {
        roomToGrid.clear();
        Map<Room, int[]> coords = game.getRoomCoordinates();
        roomToGrid.putAll(coords);
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ensureLayout();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Set<Room> visited = new HashSet<>(game.getVisitedRooms());
        Room current = game.getCurrentRoomRef();
        Set<Room> visible = new HashSet<>(visited);
        if (current != null) {
            visible.add(current);
            for (Map.Entry<String, Room> e : current.getExits().entrySet()) {
                if (e.getValue() != null) visible.add(e.getValue());
            }
        }

        int cell = 80;
        int radius = 18;

        g2.setColor(new Color(200, 200, 200));
        for (Room r : visible) {
            int[] pos = roomToGrid.get(r);
            if (pos == null) continue;
            int cx = 40 + pos[0] * cell;
            int cy = 40 + pos[1] * cell;
            for (Map.Entry<String, Room> e : r.getExits().entrySet()) {
                Room neighbor = e.getValue();
                if (!visible.contains(neighbor)) continue; 
                int[] npos = roomToGrid.get(neighbor);
                if (npos == null) continue;
                int nx = 40 + npos[0] * cell;
                int ny = 40 + npos[1] * cell;
                g2.drawLine(cx, cy, nx, ny);
            }
        }

        for (Room r : visible) {
            int[] pos = roomToGrid.get(r);
            if (pos == null) continue;
            int x = 40 + pos[0] * cell - radius;
            int y = 40 + pos[1] * cell - radius;
            if (r == current) {
                g2.setColor(new Color(66, 135, 245));
            } else {
                g2.setColor(new Color(120, 120, 120));
            }
            g2.fillRect(x, y, radius * 2, radius * 2);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, radius * 2, radius * 2);

            if (r.getMonster() != null && !r.getMonster().isDead()) {
                g2.setColor(new Color(200, 50, 50));
                g2.fillOval(x + 3, y + 3, 10, 10);
            }
            if (!r.getItems().isEmpty()) {
                g2.setColor(new Color(230, 160, 40));
                g2.fillOval(x + radius * 2 - 13, y + radius * 2 - 13, 10, 10);
            }
        }

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.setColor(Color.DARK_GRAY);
    }
}


