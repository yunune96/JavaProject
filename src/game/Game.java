package game;

public class Game {
    private Room currentRoom;
    private Player player;
    private boolean running;
    private Room startRoom;
    private int worldSeed;
    private final java.util.Set<Room> visitedRooms = new java.util.HashSet<>();
    private final java.util.Map<Room, int[]> roomToCoordinates = new java.util.HashMap<>();
    private long startTimeMs;
    private long endTimeMs;
    private boolean cleared;

    public void setupGame() {
        setupGame(2);
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
        this.worldSeed = seedId;
        generateRandomDungeon(grid, 4, 4, seedId);
        this.startRoom = grid[0][0];
        currentRoom = grid[0][0];
        player = new Player(currentRoom);
        running = true;
        visitedRooms.clear();
        visitedRooms.add(currentRoom);
        startTimeMs = System.currentTimeMillis();
        endTimeMs = 0L;
        cleared = false;
    }

    public int getSeedId() {
        return worldSeed;
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

    public boolean isCleared() {
        return cleared;
    }

    public long getElapsedMillis() {
        long end = endTimeMs > 0 ? endTimeMs : System.currentTimeMillis();
        return Math.max(0L, end - startTimeMs);
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
            case "장착":
                if (arg != null) {
                    boolean ok = player.equipWeapon(arg);
                    if (ok) {
                        return "" + arg + "을(를) 장착했습니다. (공격력: " + (player.getAttackDamage()+3) + ")";
                    } else {
                        return "장착할 수 없습니다. 인벤토리에 없거나 무기가 아닙니다.";
                    }
                } else {
                    return "무엇을 장착하시겠습니까?";
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
        Monster cur = currentRoom.getMonster();
        if (cur != null && !cur.isDead()) {
            return "몬스터가 길을 가로막습니다. 먼저 처치하세요. (몬스터: " + cur.getName() + ")";
        }
        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom != null) {
            if (nextRoom.isLocked()) {
                return "문이 잠겨 있습니다. 무언가가 필요해 보입니다.";
            }
            currentRoom = nextRoom;
            player.moveTo(nextRoom);
            visitedRooms.add(currentRoom);
            String desc = currentRoom.getDescription();
            Monster mHere = currentRoom.getMonster();
            if (mHere != null && !mHere.isDead()) {
                desc += System.lineSeparator() + "몬스터가 나타났다: " + mHere.getName() + " (체력 " + mHere.getHealth() + ")";
            }
            boolean nearBoss = false;
            for (Room r : new Room[]{currentRoom.getExit("북쪽"), currentRoom.getExit("남쪽"), currentRoom.getExit("동쪽"), currentRoom.getExit("서쪽")}) {
                if (r != null && r.getMonster() != null && r.getMonster().isBoss()) {
                    nearBoss = true; break;
                }
            }
            if (nearBoss) {
                desc += System.lineSeparator() + "강한 기운이 느껴진다. 보스의 기척이 가까이 있다!";
            }
            return desc;
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
            cleared = true;
            endTimeMs = System.currentTimeMillis();
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

        if ("포션".equalsIgnoreCase(itemName)) {
            int heal = 100 - player.getHealth();
            if (heal > 0) player.heal(heal);
            return "포션을 사용했습니다. 체력이 모두 회복되었습니다. (체력: " + player.getHealth() + ")";
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
        int dmg = player.getAttackDamage();
        m.damage(dmg);
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append("에게 ").append(dmg).append("의 피해를 입혔습니다. (남은 체력: ").append(m.getHealth()).append(")");
        if (m.isDead()) {
            currentRoom.clearMonster();
            if (m.isBoss()) {
                running = false;
                cleared = true;
                endTimeMs = System.currentTimeMillis();
                sb.append(System.lineSeparator()).append("보스를 처치했습니다! 게임 클리어!");
            } else {
                sb.append(System.lineSeparator()).append("적을 물리쳤습니다.");
            }
            return sb.toString();
        }
        int monsterBase = m.getAttackDamage();
        int monsterHit = Math.max(0, monsterBase - new java.util.Random().nextInt(3)); 
        player.damage(monsterHit);
        sb.append(System.lineSeparator()).append(m.getName()).append("의 반격! ").append(monsterHit).append(" 피해를 입었습니다. (체력: ").append(player.getHealth()).append(")");
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

    public java.util.List<String> getWeaponInventoryItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Item i : player.getInventory()) {
            if (player.isWeaponName(i.getName())) {
                names.add(i.getName());
            }
        }
        return names;
    }

    public java.util.List<String> getNonWeaponInventoryItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Item i : player.getInventory()) {
            if (!player.isWeaponName(i.getName())) {
                names.add(i.getName());
            }
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

    private void generateRandomDungeon(Room[][] grid, int width, int height, long seed) {
        java.util.Random rng = new java.util.Random(seed);
        carveMazeWithBacktracker(grid, width, height, rng);
        addRandomLoops(grid, width, height, 2, rng);
        assignDescriptions(grid, width, height, rng);
        placeBossKeyMonstersAndItems(grid, width, height, rng);
        grid[0][0].setNpc("정찰병", "보스는 남동쪽 어딘가에 있다. 열쇠는 길 위에 있다.");
    }

    private void carveMazeWithBacktracker(Room[][] grid, int width, int height, java.util.Random rng) {
        boolean[][] visited = new boolean[height][width];
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        int cx = 0, cy = 0;
        visited[cy][cx] = true;
        stack.push(new int[]{cx, cy});

        while (!stack.isEmpty()) {
            cx = stack.peek()[0];
            cy = stack.peek()[1];

            int[][] dirs = new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };
            shuffleArray(dirs, rng);

            boolean moved = false;
            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (visited[ny][nx]) continue;
                connect(grid, cx, cy, nx, ny);
                visited[ny][nx] = true;
                stack.push(new int[]{nx, ny});
                moved = true;
                break;
            }
            if (!moved) {
                stack.pop();
            }
        }
    }

    private void addRandomLoops(Room[][] grid, int width, int height, int loopCount, java.util.Random rng) {
        for (int i = 0; i < loopCount; i++) {
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            int[][] dirs = new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };
            shuffleArray(dirs, rng);
            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                connect(grid, x, y, nx, ny);
                break;
            }
        }
    }

    private void assignDescriptions(Room[][] grid, int width, int height, java.util.Random rng) {
        String[] early = new String[] {
            "낡은 회랑. 낮은 속삭임이 들린다.",
            "작은 전당. 촛불이 깜빡인다.",
            "좁은 복도. 먼지가 뿌옇다."
        };
        String[] mid = new String[] {
            "축축한 회랑. 시체가 꿈틀거린다.",
            "파손된 무기고. 철 냄새가 난다.",
            "갈라진 바닥. 발밑에서 바람이 샌다."
        };
        String[] late = new String[] {
            "큰 전투의 흔적. 땅이 움푹 패였다.",
            "어두운 복도. 긴장감이 감돈다.",
            "침식된 전당. 바닥이 끈적거린다."
        };

        int[][] dist = computeDistanceMap(grid, width, height, 0, 0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 && y == 0) {
                    grid[y][x].setDescription("성의 후문. 사방이 낡아 보인다.");
                    continue;
                }
                int d = dist[y][x];
                String txt;
                if (d <= 2) {
                    txt = early[rng.nextInt(early.length)];
                } else if (d <= 4) {
                    txt = mid[rng.nextInt(mid.length)];
                } else {
                    txt = late[rng.nextInt(late.length)];
                }
                grid[y][x].setDescription(txt);
            }
        }
    }

    private void placeBossKeyMonstersAndItems(Room[][] grid, int width, int height, java.util.Random rng) {
        int[][] dist = computeDistanceMap(grid, width, height, 0, 0);
        int bx = 0, by = 0, maxd = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dist[y][x] > maxd) { maxd = dist[y][x]; bx = x; by = y; }
            }
        }
        java.util.List<int[]> path = shortestPath(grid, width, height, 0, 0, bx, by);
        Room bossRoom = grid[by][bx];
        bossRoom.setDescription("☠️ 보스의 방! 어둠의 기운이 요동친다.");
        bossRoom.setMonster(new Monster("☠️ 어둠의 군주", 60, 8, true));
        bossRoom.lockWithItem("보스열쇠");

        int keyX = -1, keyY = -1;
        if (path.size() >= 3) {
            java.util.List<Integer> candidates = new java.util.ArrayList<>();
            for (int i = 1; i <= path.size() - 2; i++) candidates.add(i);
            java.util.Collections.shuffle(candidates, rng);
            for (int idx : candidates) {
                int[] k = path.get(idx);
                Room r = grid[k[1]][k[0]];
                if (canPlaceItem(r)) {
                    r.addItem(new Item("보스열쇠", "보스의 방을 여는 열쇠."));
                    keyX = k[0]; keyY = k[1];
                    break;
                }
            }
        }
        if (keyX == -1) {
            outer: for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((x == 0 && y == 0) || (x == bx && y == by)) continue;
                    Room r = grid[y][x];
                    if (canPlaceItem(r)) { r.addItem(new Item("보스열쇠", "보스의 방을 여는 열쇠.")); keyX = x; keyY = y; break outer; }
                }
            }
        }

        java.util.List<int[]> deadEnds = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int exits = grid[y][x].getExits().size();
                if (!(x == 0 && y == 0) && exits <= 1) deadEnds.add(new int[]{x,y});
            }
        }
        if (!deadEnds.isEmpty()) {
            java.util.Collections.shuffle(deadEnds, rng);
            for (int[] de : deadEnds) {
                Room r = grid[de[1]][de[0]];
                if (canPlaceItem(r)) { r.addItem(new Item("금화", "빛나는 금화 몇 닢.")); break; }
            }
        }
        if (path.size() > 1) {
            for (int i = 1; i < path.size(); i++) {
                int[] p = path.get(i);
                if ((p[0] == bx && p[1] == by) || (p[0] == keyX && p[1] == keyY)) continue;
                Room r = grid[p[1]][p[0]];
                if (canPlaceItem(r)) { r.addItem(new Item("단검", "가벼운 무기.")); break; }
            }
        }
        if (path.size() > 2) {
            int midIdx = path.size() / 2;
            int[] midp = path.get(midIdx);
            Room rMid = grid[midp[1]][midp[0]];
            if (canPlaceItem(rMid) && !(midp[0] == keyX && midp[1] == keyY)) {
                rMid.addItem(new Item("포션", "체력을 회복(연출용)."));
            } else {
                for (int i = 1; i < path.size() - 1; i++) {
                    int[] p = path.get(i);
                    if ((p[0] == keyX && p[1] == keyY)) continue;
                    Room r = grid[p[1]][p[0]];
                    if (canPlaceItem(r)) { r.addItem(new Item("포션", "체력을 회복(연출용).")); break; }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 && y == 0) continue;
                if (x == bx && y == by) continue;
                Room r = grid[y][x];
                boolean hasKey = false;
                for (Item it : r.getItems()) { if ("보스열쇠".equals(it.getName())) { hasKey = true; break; } }
                if (hasKey) continue;
                if (!canPlaceMonster(r)) continue;

                int d = dist[y][x];
                double p;
                String name;
                int hp, atk;
                if (d <= 2) { p = 0.35; name = "🦊 야생 늑대"; hp = 16; atk = 4; }
                else if (d <= 4) { p = 0.45; name = "💀 해골 병사"; hp = 22; atk = 5; }
                else { p = 0.55; name = "🐮 미노타우로스"; hp = 35; atk = 7; }
                if (rng.nextDouble() < p) {
                    r.setMonster(new Monster(name, hp, atk, false));
                }
            }
        }
    }

    private boolean canPlaceItem(Room r) {
        return r.getMonster() == null && r.getItems().isEmpty();
    }

    private boolean canPlaceMonster(Room r) {
        return r.getMonster() == null && r.getItems().isEmpty();
    }

    private int[][] computeDistanceMap(Room[][] grid, int width, int height, int sx, int sy) {
        int[][] dist = new int[height][width];
        for (int y = 0; y < height; y++) java.util.Arrays.fill(dist[y], -1);
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] c = q.removeFirst();
            int x = c[0], y = c[1];
            Room room = grid[y][x];
            int[][] neigh = new int[][] { {x+1,y,"동쪽".hashCode()}, {x-1,y,"서쪽".hashCode()}, {x,y+1,"남쪽".hashCode()}, {x,y-1,"북쪽".hashCode()} };
            if (room.getExit("동쪽") == null) neigh[0][0] = Integer.MIN_VALUE;
            if (room.getExit("서쪽") == null) neigh[1][0] = Integer.MIN_VALUE;
            if (room.getExit("남쪽") == null) neigh[2][0] = Integer.MIN_VALUE;
            if (room.getExit("북쪽") == null) neigh[3][0] = Integer.MIN_VALUE;
            for (int[] n : neigh) {
                int nx = n[0];
                int ny = n[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (dist[ny][nx] != -1) continue;
                dist[ny][nx] = dist[y][x] + 1;
                q.add(new int[]{nx, ny});
            }
        }
        return dist;
    }

    private java.util.List<int[]> shortestPath(Room[][] grid, int width, int height, int sx, int sy, int tx, int ty) {
        int[][] dist = new int[height][width];
        for (int y = 0; y < height; y++) java.util.Arrays.fill(dist[y], -1);
        int[][] px = new int[height][width];
        int[][] py = new int[height][width];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        dist[sy][sx] = 0; px[sy][sx] = -1; py[sy][sx] = -1;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] c = q.removeFirst();
            int x = c[0], y = c[1];
            if (x == tx && y == ty) break;
            Room room = grid[y][x];
            int[][] neigh = new int[][] { {x+1,y,"동쪽".hashCode()}, {x-1,y,"서쪽".hashCode()}, {x,y+1,"남쪽".hashCode()}, {x,y-1,"북쪽".hashCode()} };
            if (room.getExit("동쪽") == null) neigh[0][0] = Integer.MIN_VALUE;
            if (room.getExit("서쪽") == null) neigh[1][0] = Integer.MIN_VALUE;
            if (room.getExit("남쪽") == null) neigh[2][0] = Integer.MIN_VALUE;
            if (room.getExit("북쪽") == null) neigh[3][0] = Integer.MIN_VALUE;
            for (int[] n : neigh) {
                int nx = n[0];
                int ny = n[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (dist[ny][nx] != -1) continue;
                dist[ny][nx] = dist[y][x] + 1;
                px[ny][nx] = x; py[ny][nx] = y;
                q.add(new int[]{nx, ny});
            }
        }
        java.util.LinkedList<int[]> path = new java.util.LinkedList<>();
        int x = tx, y = ty;
        if (dist[ty][tx] == -1) return path;
        while (x != -1 && y != -1) {
            path.addFirst(new int[]{x, y});
            int nx = px[y][x];
            int ny = py[y][x];
            x = nx; y = ny;
        }
        return path;
    }

    private void shuffleArray(int[][] arr, java.util.Random rng) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int[] tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

}
