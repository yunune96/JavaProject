package game;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

public class GameUI extends JFrame {
    private final Game game;
    private final JTextArea outputArea;

    public GameUI() {
        super("텍스트 어드벤처");
        this.game = new Game();
        int seed = Math.random() < 0.5 ? 1 : 2;
        this.game.setupGame(seed);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JButton btnLook = new JButton("현재 상태");
        JButton btnNorth = new JButton("↑");
        JButton btnSouth = new JButton("↓");
        JButton btnEast = new JButton("→");
        JButton btnWest = new JButton("←");
        JButton btnQuit = new JButton("종료");
        JButton btnPick = new JButton("줍기");
        JButton btnUse = new JButton("사용");
        JButton btnInv = new JButton("인벤토리");
        JButton btnTalk = new JButton("대화");
        JButton btnAttack = new JButton("공격");

        JPanel arrowPad = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        c.gridx = 1; c.gridy = 0; arrowPad.add(btnNorth, c);
        c.gridx = 0; c.gridy = 1; arrowPad.add(btnWest, c);
        c.gridx = 2; c.gridy = 1; arrowPad.add(btnEast, c);
        c.gridx = 1; c.gridy = 2; arrowPad.add(btnSouth, c);

        JPanel utilityPanel = new JPanel();
        utilityPanel.add(btnLook);
        utilityPanel.add(btnInv);
        utilityPanel.add(btnPick);
        utilityPanel.add(btnUse);
        utilityPanel.add(btnTalk);
        utilityPanel.add(btnAttack);
        utilityPanel.add(btnQuit);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel controlsRow = new JPanel(new BorderLayout());
        controlsRow.add(arrowPad, BorderLayout.CENTER);
        controlsRow.add(utilityPanel, BorderLayout.EAST);
        bottomPanel.add(controlsRow, BorderLayout.CENTER);

        JPanel centerSplit = new JPanel(new GridLayout(1, 2));
        centerSplit.add(new JScrollPane(outputArea));
        MapPanel mapPanel = new MapPanel(game);
        centerSplit.add(mapPanel);

        setLayout(new BorderLayout());
        add(centerSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(640, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        appendLine("게임 시작! 하단 방향키와 버튼으로 조작하세요.");
        appendLine(game.getCurrentDescription());

        btnLook.addActionListener(this::onLook);
        btnNorth.addActionListener(e -> { onMove("북쪽"); mapPanel.refresh(); });
        btnSouth.addActionListener(e -> { onMove("남쪽"); mapPanel.refresh(); });
        btnEast.addActionListener(e -> { onMove("동쪽"); mapPanel.refresh(); });
        btnWest.addActionListener(e -> { onMove("서쪽"); mapPanel.refresh(); });
        btnQuit.addActionListener(e -> onQuit());
        btnInv.addActionListener(e -> appendLine(sendCommand("인벤토리")));
        btnPick.addActionListener(e -> onPick());
        btnUse.addActionListener(e -> onUse());
        btnTalk.addActionListener(e -> appendLine(sendCommand("대화")));
        btnAttack.addActionListener(e -> appendLine(sendCommand("공격")));
    }

    private void onLook(ActionEvent e) {
        appendLine(game.getCurrentDescription());
    }

    private void onMove(String direction) {
        String result = game.handleCommand("이동 " + direction);
        if (!result.isEmpty()) {
            appendLine(result);
        }
    }

    private void onPick() {
        java.util.List<String> items = game.getCurrentItemNames();
        if (items.isEmpty()) {
            appendLine("여기에는 주울 수 있는 것이 없습니다.");
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "어떤 아이템을 줍겠습니까?",
                "줍기",
                JOptionPane.QUESTION_MESSAGE,
                null,
                items.toArray(new String[0]),
                items.get(0)
        );
        if (choice != null) {
            appendLine(game.handleCommand("줍기 " + choice));
        }
    }

    private void onUse() {
        java.util.List<String> items = game.getInventoryItemNames();
        if (items.isEmpty()) {
            appendLine("사용할 아이템이 없습니다.");
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "어떤 아이템을 사용하시겠습니까?",
                "사용",
                JOptionPane.QUESTION_MESSAGE,
                null,
                items.toArray(new String[0]),
                items.get(0)
        );
        if (choice != null) {
            appendLine(game.handleCommand("사용 " + choice));
        }
    }

    private String sendCommand(String cmd) {
        String result = game.handleCommand(cmd);
        return result == null ? "" : result;
    }

    private void onQuit() {
        appendLine("게임을 종료합니다.");
        dispose();
    }

    private void appendLine(String text) {
        outputArea.append(text + System.lineSeparator());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameUI().setVisible(true));
    }
}


