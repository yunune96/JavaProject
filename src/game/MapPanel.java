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

/**
 * 간단한 미니맵 패널: 방문한 방과 현재 위치를 표시합니다.
 * 방은 격자상의 노드로 그려지고, 출구 방향(동/서/남/북)에 따라 선으로 연결됩니다.
 */
public class MapPanel extends JPanel {
    private final Game game;

    // 좌표 배치: 선형 맵(0..9)을 x축으로 배치
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
        // 가시 범위: 방문 방 + 현재 방의 인접 방(직접 연결된 방)
        Set<Room> visible = new HashSet<>(visited);
        if (current != null) {
            visible.add(current);
            for (Map.Entry<String, Room> e : current.getExits().entrySet()) {
                if (e.getValue() != null) visible.add(e.getValue());
            }
        }

        int cell = 80; // 격자 한 칸 크기
        int radius = 18;

        // 연결선 그리기 (가시 범위 내 방 간 출구)
        g2.setColor(new Color(200, 200, 200));
        for (Room r : visible) {
            int[] pos = roomToGrid.get(r);
            if (pos == null) continue;
            int cx = 40 + pos[0] * cell;
            int cy = 40 + pos[1] * cell;
            for (Map.Entry<String, Room> e : r.getExits().entrySet()) {
                Room neighbor = e.getValue();
                if (!visible.contains(neighbor)) continue; // 가시 범위 내에서만 연결
                int[] npos = roomToGrid.get(neighbor);
                if (npos == null) continue;
                int nx = 40 + npos[0] * cell;
                int ny = 40 + npos[1] * cell;
                g2.drawLine(cx, cy, nx, ny);
            }
        }

        // 노드(방) 그리기 (가시 범위)
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
        }

        // 범례/레이블
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("파랑 = 현재 위치", 10, getHeight() - 12);
    }
}


