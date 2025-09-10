## 텍스트 어드벤처 게임 (Java)

간단한 텍스트 기반 어드벤처 게임 예제입니다. `Room`, `Item`, `Player`, `Game` 클래스로 구성되어 있으며 기본 명령어(종료, 이동, 현재 상황)를 지원합니다.

### 기술 스택
>![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

### 프로젝트 구조

```
src/
  game/
    Game.java
    Room.java
    Item.java
    Player.java
out/ (컴파일 산출물)
```

### 빌드

PowerShell에서 다음을 실행하세요.

```bash
javac -d out src/game/*.java
```

### 실행

```bash
java -cp out game.Game
```

### GUI 실행

```bash
java -cp out game.GameUI
```

### 사용 예시

- 이동: `이동 동쪽`, `이동 북쪽`
- 둘러보기: `현재 상태`
- 종료: `종료`

### 자동 테스트(예시)

입력을 파이프로 전달하여 간단히 테스트할 수 있습니다.

```bash
Set-Content -Path temp-input.txt -Value "봐`r`n이동 동쪽`r`n종료"
Get-Content temp-input.txt | java -cp out game.Game
Remove-Item temp-input.txt
```

### 확장 아이디어

- 아이템 줍기/사용 명령어 추가 (예: `줍기 열쇠`, `사용 열쇠`)
- 특정 아이템이 있어야만 열리는 문/퍼즐
- NPC 대화 및 힌트 시스템
- 체력/이벤트/간단한 전투 시스템
