function Load-DotEnv([string] $path) {
    if (-not (Test-Path $path)) { return }
    Get-Content $path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line) { return }
        if ($line.StartsWith('#') -or $line.StartsWith(';')) { return }
        if ($line.StartsWith('export ')) { $line = $line.Substring(7).Trim() }
        $idx = $line.IndexOf('=')
        if ($idx -lt 1) { return }
        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $current = [System.Environment]::GetEnvironmentVariable($key, 'Process')
        if ([string]::IsNullOrEmpty($current)) {
            [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}

# .env 로드 (프로세스 환경변수가 우선, 없을 때만 .env 값 적용)
$dotenvPath = Join-Path $PSScriptRoot ".env"
Load-DotEnv $dotenvPath

# 기본값 설정: 누락된 값만 채움
$env:MYSQL_HOST = $env:MYSQL_HOST ?? "127.0.0.1"
$env:MYSQL_PORT = $env:MYSQL_PORT ?? "3306"
$env:MYSQL_DB = $env:MYSQL_DB ?? "game"
$env:MYSQL_USER = $env:MYSQL_USER ?? "root"

# 비밀번호는 스크립트에 직접 넣지 않고, 없으면 입력받기(Enter만 누르면 빈 비밀번호)
if (-not $env:MYSQL_PASSWORD) {
    $secure = Read-Host -AsSecureString -Prompt "MYSQL_PASSWORD (입력 생략 시 빈 비밀번호)"
    try {
        $env:MYSQL_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
    } catch {}
}

# 컴파일 (필요 시)
if (-not (Test-Path "out")) { New-Item -ItemType Directory -Path "out" | Out-Null }
javac -d out src/game/*.java

# 실행
java -cp "out;lib/*" game.GameUI


