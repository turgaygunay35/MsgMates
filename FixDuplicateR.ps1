# FixDuplicateR.ps1 - MsgMates
Write-Host "Cleaning build cache..." -ForegroundColor Cyan
./gradlew clean

Write-Host "Showing who brings annotation-experimental (if any)..." -ForegroundColor Cyan
./gradlew :app:dependencyInsight --configuration debugCompileClasspath --dependency androidx.annotation:annotation-experimental

Write-Host "Assembling Debug..." -ForegroundColor Cyan
./gradlew assembleDebug
