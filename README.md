# 🕐 Neon Clock — Android App

Красивые неоновые часы с анимированными кольцами для Android.

## Что умеет
- Полноэкранное отображение времени с неоновым свечением (cyan)
- Мигающий разделитель каждую секунду
- Дата по-русски
- Три анимированных дуговых кольца: часы (cyan), минуты (magenta), секунды (green)
- Сетка на фоне в стиле киберпанк

## Как собрать

### В AIDE (на телефоне)
1. Распакуйте ZIP
2. В AIDE: File → Open Project → выберите папку NeonClock
3. Нажмите Build & Run

### В Termux
```bash
pkg install gradle
cd NeonClock
gradle assembleDebug
# APK будет в: app/build/outputs/apk/debug/app-debug.apk
```

### В Android Studio (на ПК)
1. File → Open → выберите папку NeonClock
2. Run → Run 'app'
