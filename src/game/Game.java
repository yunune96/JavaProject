package game;

public class Game {
    private Room currentRoom;
    private Player player;
    private boolean running;
    private Room startRoom;
    private final java.util.Set<Room> visitedRooms = new java.util.HashSet<>();
    private final java.util.Map<Room, int[]> roomToCoordinates = new java.util.HashMap<>();

    public void setupGame() {
        setupGame(1);
    }

    public void setupGame(int seedId) {
        Room[][] grid = new Room[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                String desc = "빈 방";
                grid[y][x] = new Room(desc);
                roomToCoordinates.put(grid[y][x], new int[]{x, y});
            }
        }
        if (seedId == 1) {
            connect(grid, 0,0, 1,0);
            connect(grid, 1,0, 2,0);
            connect(grid, 2,0, 2,1);
            connect(grid, 2,1, 3,1);
            connect(grid, 3,1, 3,2);
            connect(grid, 3,2, 2,2);
            connect(grid, 2,2, 1,2);
            connect(grid, 1,2, 1,3);
            connect(grid, 1,3, 2,3);
            connect(grid, 2,3, 3,3);
            connect(grid, 0,1, 1,1);
            connect(grid, 0,2, 0,1);
            connect(grid, 0,2, 1,2);

            grid[0][0].setDescription("성의 입구. 사방으로 길이 얽혀 있다.");
            grid[0][2].addItem(new Item("단검", "가벼운 무기."));
            grid[1][2].addItem(new Item("포션", "체력을 회복(연출용)."));
            grid[1][0].setMonster(new Monster("해골 병사", 20, 5, false));
            grid[2][2].setMonster(new Monster("동굴 트롤", 30, 6, false));
            grid[2][3].setMonster(new Monster("보스의 경비", 25, 6, false));
            grid[3][3].setMonster(new Monster("어둠의 군주", 60, 8, true));
            grid[0][0].setNpc("수호자", "보스는 (3,3)에 있다. 미로를 헤쳐 나가라.");
            this.startRoom = grid[0][0];
            currentRoom = grid[0][0];
        } else {
            connect(grid, 0,0, 0,1);
            connect(grid, 0,1, 1,1);
            connect(grid, 1,1, 2,1);
            connect(grid, 2,1, 2,2);
            connect(grid, 2,2, 3,2);
            connect(grid, 3,2, 3,3);
            connect(grid, 1,1, 1,0);
            connect(grid, 1,0, 2,0);
            connect(grid, 2,0, 3,0);
            connect(grid, 2,2, 1,2);
            connect(grid, 1,2, 1,3);

            grid[0][0].setDescription("성의 후문. 남쪽으로 어두운 통로가 보인다.");
            grid[3][0].setDescription("무너진 벽 너머 밝은 빛.");
            grid[1][0].addItem(new Item("단검", "가벼운 무기."));
            grid[1][2].addItem(new Item("포션", "체력을 회복(연출용)."));
            grid[2][1].setMonster(new Monster("좀비 병사", 18, 4, false));
            grid[2][2].setMonster(new Monster("미노타우로스", 35, 7, false));
            grid[3][3].setMonster(new Monster("어둠의 군주", 60, 8, true));
            grid[0][0].setNpc("정찰병", "보스는 남동쪽 끝에 있다. 조심해라.");
            this.startRoom = grid[0][0];
            currentRoom = grid[0][0];
        }
        player = new Player(currentRoom);
        running = true;
        visitedRooms.clear();
        visitedRooms.add(currentRoom);
    }

    public String getCurrentDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentRoom.getDescription());
        if (!currentRoom.getItems().isEmpty()) {
            sb.append(System.lineSeparator()).append("아이템: ");
            for (int i = 0; i < currentRoom.getItems().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(currentRoom.getItems().get(i).getName());
            }
        }
        if (currentRoom.getMonster() != null) {
            Monster m = currentRoom.getMonster();
            sb.append(System.lineSeparator()).append("몬스터: ").append(m.getName()).append(" (체력 ").append(m.getHealth()).append(")");
        }
        if (currentRoom.hasNpc()) {
            sb.append(System.lineSeparator()).append("누군가가 여기에 있습니다. '대화' 해보세요.");
        }
        // 출구 요약
        java.util.Set<String> exits = currentRoom.getExits().keySet();
        if (!exits.isEmpty()) {
            sb.append(System.lineSeparator()).append("출구: ");
            int i = 0; for (String d : exits) { if (i++ > 0) sb.append(", "); sb.append(d); }
        }
        return sb.toString();
    }

    public boolean isRunning() {
        return running;
    }

    public String handleCommand(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";

        String[] words = trimmed.toLowerCase().split("\\s+");
        String command = words[0];
        int spaceIdx = trimmed.indexOf(' ');
        String arg = spaceIdx >= 0 ? trimmed.substring(spaceIdx + 1) : null;

        switch (command) {
            case "종료":
                running = false;
                return "게임을 종료합니다.";
            case "이동":
                if (words.length > 1) {
                    return movePlayer(words[1]);
                } else {
                    return "어느 방향으로 이동할지 입력해주세요. (예: 이동 동쪽)";
                }
            case "봐":
                return currentRoom.getDescription();
            case "인벤토리":
                return listInventory();
            case "줍기":
                if (arg != null) {
                    return pickUp(arg);
                } else {
                    return "무엇을 줍겠습니까?";
                }
            case "사용":
                if (arg != null) {
                    return useItem(arg);
                } else {
                    return "무엇을 사용하시겠습니까?";
                }
            case "대화":
                return talk();
            case "공격":
                return attack();
            case "현재 상태":
                return currentRoom.getDescription();
            default:
                return "알 수 없는 명령입니다.";
        }
    }

    public String movePlayer(String direction) {
        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom != null) {
            if (nextRoom.isLocked()) {
                return "문이 잠겨 있습니다. 무언가가 필요해 보입니다.";
            }
            currentRoom = nextRoom;
            player.moveTo(nextRoom);
            visitedRooms.add(currentRoom);
            return currentRoom.getDescription();
        } else {
            return "그 방향으로는 갈 수 없습니다.";
        }
    }

    public String pickUp(String itemName) {
        Item item = currentRoom.takeItemByName(itemName);
        if (item == null) {
            return "그런 아이템은 여기 없습니다.";
        }
        player.addItem(item);
        if (item.getName().equalsIgnoreCase("보물")) {
            running = false;
            return "보물을 획득했습니다! 축하합니다. 게임 클리어!";
        }
        return item.getName() + "을(를) 주웠습니다.";
    }

    public String useItem(String itemName) {
        Room north = currentRoom.getExit("북쪽");
        Room south = currentRoom.getExit("남쪽");
        Room east = currentRoom.getExit("동쪽");
        Room west = currentRoom.getExit("서쪽");

        boolean hadItem = player.removeItemByName(itemName);
        if (!hadItem) {
            return "그 아이템을 가지고 있지 않습니다.";
        }

        Room[] neighbors = new Room[] { north, south, east, west };
        for (Room r : neighbors) {
            if (r != null && r.isLocked()) {
                if (r.unlock(itemName)) {
                    return "문이 열렸습니다.";
                }
            }
        }
        player.addItem(new Item(itemName, ""));
        return "여기서는 그 아이템을 사용할 수 없습니다.";
    }

    public String talk() {
        return currentRoom.talkToNpc();
    }

    public String attack() {
        Monster m = currentRoom.getMonster();
        if (m == null) {
            return "공격할 대상이 없습니다.";
        }
        m.damage(10);
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append("에게 10의 피해를 입혔습니다. (남은 체력: ").append(m.getHealth()).append(")");
        if (m.isDead()) {
            currentRoom.clearMonster();
            if (m.isBoss()) {
                running = false;
                sb.append(System.lineSeparator()).append("보스를 처치했습니다! 게임 클리어!");
            } else {
                sb.append(System.lineSeparator()).append("적을 물리쳤습니다.");
            }
            return sb.toString();
        }
        player.damage(m.getAttackDamage());
        sb.append(System.lineSeparator()).append(m.getName()).append("의 반격! ").append(m.getAttackDamage()).append(" 피해를 입었습니다. (체력: ").append(player.getHealth()).append(")");
        if (player.getHealth() <= 0) {
            running = false;
            sb.append(System.lineSeparator()).append("당신은 쓰러졌습니다. 게임 오버.");
        }
        return sb.toString();
    }

    private String listInventory() {
        if (player.getInventory().isEmpty()) {
            return "인벤토리가 비어 있습니다.";
        }
        StringBuilder sb = new StringBuilder("인벤토리: ");
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(player.getInventory().get(i).getName());
        }
        return sb.toString();
    }

    public java.util.List<String> getCurrentItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Item i : currentRoom.getItems()) {
            names.add(i.getName());
        }
        return names;
    }

    public java.util.List<String> getInventoryItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Item i : player.getInventory()) {
            names.add(i.getName());
        }
        return names;
    }

    public Room getCurrentRoomRef() {
        return currentRoom;
    }

    public Room getStartRoomRef() {
        return startRoom;
    }

    public java.util.Set<Room> getVisitedRooms() {
        return java.util.Collections.unmodifiableSet(visitedRooms);
    }

    public java.util.Map<Room, int[]> getRoomCoordinates() {
        return java.util.Collections.unmodifiableMap(roomToCoordinates);
    }

    private void connect(Room[][] grid, int x1, int y1, int x2, int y2) {
        Room a = grid[y1][x1];
        Room b = grid[y2][x2];
        if (x2 == x1 + 1 && y2 == y1) {
            a.setExit("동쪽", b);
            b.setExit("서쪽", a);
        } else if (x2 == x1 - 1 && y2 == y1) {
            a.setExit("서쪽", b);
            b.setExit("동쪽", a);
        } else if (y2 == y1 + 1 && x2 == x1) {
            a.setExit("남쪽", b);
            b.setExit("북쪽", a);
        } else if (y2 == y1 - 1 && x2 == x1) {
            a.setExit("북쪽", b);
            b.setExit("남쪽", a);
        }
    }

    // setDesc 보조는 불필요해져서 제거
}


