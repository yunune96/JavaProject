package game;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
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
    private boolean clearHandled = false;
    private final HighscoreRepository repo;

    public GameUI() {
        super("텍스트 어드벤처");
        this.game = new Game();
        int seed = (int)(System.currentTimeMillis() & 0x7fffffff);
        this.game.setupGame(seed);

        this.repo = new HighscoreRepository();
        this.repo.init();

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
        JButton btnEquip = new JButton("장착");
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

        JPanel utilityColumn = new JPanel(new GridBagLayout());
        utilityColumn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        GridBagConstraints uc = new GridBagConstraints();
        uc.fill = GridBagConstraints.BOTH;
        uc.insets = new Insets(3, 3, 3, 3);

        uc.gridy = 0; uc.gridx = 0; uc.gridwidth = 3; uc.weightx = 1.0; utilityColumn.add(btnLook, uc);
        uc.gridy = 0; uc.gridx = 3; uc.gridwidth = 3; uc.weightx = 1.0; utilityColumn.add(btnInv, uc);

        uc.gridy = 1; uc.gridx = 0; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnPick, uc);
        uc.gridy = 1; uc.gridx = 2; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnUse, uc);
        uc.gridy = 1; uc.gridx = 4; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnTalk, uc);

        uc.gridy = 2; uc.gridx = 0; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnEquip, uc);
        uc.gridy = 2; uc.gridx = 2; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnAttack, uc);
        uc.gridy = 2; uc.gridx = 4; uc.gridwidth = 2; uc.weightx = 1.0; utilityColumn.add(btnQuit, uc);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        JPanel controlsRow = new JPanel(new BorderLayout());
        controlsRow.add(utilityColumn, BorderLayout.CENTER);
        controlsRow.add(arrowPad, BorderLayout.EAST);
        bottomPanel.add(controlsRow, BorderLayout.CENTER);

        JPanel centerSplit = new JPanel(new GridLayout(1, 2));
        centerSplit.add(new JScrollPane(outputArea));
        MapPanel mapPanel = new MapPanel(game);
        centerSplit.add(mapPanel);

        setLayout(new BorderLayout());
        add(centerSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1024, 700));
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        appendLine("게임 시작! 하단 방향키와 버튼으로 조작하세요.");
        appendLine("이번 맵 시드: " + game.getSeedId());
        appendLine(game.getCurrentDescription());

        btnLook.addActionListener(this::onLook);
        btnNorth.addActionListener(e -> { onMove("북쪽"); mapPanel.refresh(); });
        btnSouth.addActionListener(e -> { onMove("남쪽"); mapPanel.refresh(); });
        btnEast.addActionListener(e -> { onMove("동쪽"); mapPanel.refresh(); });
        btnWest.addActionListener(e -> { onMove("서쪽"); mapPanel.refresh(); });
        btnQuit.addActionListener(e -> onQuit());
        btnInv.addActionListener(e -> { appendLine(sendCommand("인벤토리")); mapPanel.refresh(); });
        btnPick.addActionListener(e -> { onPick(); mapPanel.refresh(); });
        btnUse.addActionListener(e -> { onUse(); mapPanel.refresh(); });
        btnEquip.addActionListener(e -> { onEquip(); mapPanel.refresh(); });
        btnTalk.addActionListener(e -> { appendLine(sendCommand("대화")); mapPanel.refresh(); });
        btnAttack.addActionListener(e -> { appendLine(sendCommand("공격")); mapPanel.refresh(); });
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
            appendLine(sendCommand("줍기 " + choice));
        }
    }

    private void onUse() {
        java.util.List<String> items = game.getNonWeaponInventoryItemNames();
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

    private void onEquip() {
        java.util.List<String> items = game.getWeaponInventoryItemNames();
        if (items.isEmpty()) {
            appendLine("장착할 무기가 없습니다.");
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "어떤 무기를 장착하시겠습니까?",
                "장착",
                JOptionPane.QUESTION_MESSAGE,
                null,
                items.toArray(new String[0]),
                items.get(0)
        );
        if (choice != null) {
            appendLine(game.handleCommand("장착 " + choice));
        }
    }

    private String sendCommand(String cmd) {
        String result = game.handleCommand(cmd);
        if (result == null) result = "";
        if (game.isCleared() && !clearHandled) {
            clearHandled = true;
            onGameCleared();
        }
        return result;
    }

    private void onQuit() {
        appendLine("게임을 종료합니다.");
        dispose();
    }

    private void onGameCleared() {
        try {
            if (!this.isEnabled()) return;
            this.setEnabled(false);
            String nickname;
            while (true) {
                nickname = javax.swing.JOptionPane.showInputDialog(this, "축하합니다! 닉네임을 입력해 주세요 :", "클리어 기록", javax.swing.JOptionPane.PLAIN_MESSAGE);
                if (nickname == null) {
                    break;
                }
                nickname = nickname.trim();
                if (nickname.matches("[A-Za-z]{3}")) break;
                javax.swing.JOptionPane.showMessageDialog(this, "영문 3글자만 입력해주세요.");
            }
            long elapsed = game.getElapsedMillis();
            java.time.LocalDateTime clearedAt = java.time.LocalDateTime.now();
            if (nickname != null) {
                String nick = nickname.toUpperCase();
                repo.insertRecord(nick, elapsed, clearedAt, game.getSeedId());
                java.util.List<String> top = repo.listTop(5);
                int myRank = repo.computeRank(elapsed);
                StringBuilder sb = new StringBuilder();
                sb.append("Ranking :\n");
                for (String line : top) sb.append(line).append('\n');
                sb.append('\n');
                String myMedal = HighscoreRepository.medalForRank(myRank);
                sb.append("내 순위 : ")
                  .append(String.format("%s%d) %s  %s", myMedal, myRank, nick, HighscoreRepository.formatElapsed(elapsed)))
                  .append('\n');
                javax.swing.JOptionPane.showMessageDialog(this, sb.toString(), "하이스코어", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "기록 저장 중 오류: " + ex.getMessage());
        } finally {
            this.setEnabled(true);
        }
    }

    private void appendLine(String text) {
        outputArea.append(text + System.lineSeparator());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameUI().setVisible(true));
    }
}


