# Decimal Clock - Android App

Приложение для Android с десятичными часами. Показывает время в десятичной системе (10 часов в сутках, 100 минут в часе, 100 секунд в минуте).

## Особенности

- 🎨 **6 цветовых тем** с градиентами
- 🕐 **Десятичное время**: 10 часов, 100 минут, 100 секунд
- 💎 **Glassmorphism дизайн** циферблата
- ⚡ **Плавная анимация** минутной и часовой стрелок
- 🎯 **Шаговое движение** секундной стрелки (тикает)
- 💾 **Сохранение настроек** (цветовая тема, видимость времени)
- 📱 **Material Design 3**

## Требования

- Android SDK 34
- Gradle 8.2+
- JDK 8+

## Сборка проекта

### 1. Установите Android SDK

Скачайте и установите Android SDK или Android Studio с официального сайта.

### 2. Настройте local.properties

Создайте файл `local.properties` в корне проекта:

```properties
sdk.dir=/path/to/your/android/sdk
```

Например:
- Windows: `sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
- Linux/Mac: `sdk.dir=/home/username/Android/Sdk`

### 3. Соберите APK

```bash
# Debug версия
./gradlew assembleDebug

# Release версия
./gradlew assembleRelease
```

APK будет создан в `app/build/outputs/apk/`

### 4. Установите на устройство

```bash
# Через adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Структура проекта

```
decimal-clock/
├── app/
│   ├── src/main/
│   │   ├── java/com/decimalclock/
│   │   │   ├── MainActivity.kt           # Основная активность
│   │   │   └── DecimalClockView.kt       # Custom View с Canvas
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml     # UI разметка
│   │   │   ├── values/
│   │   │   │   ├── colors.xml            # Цветовые темы
│   │   │   │   ├── strings.xml           # Строки
│   │   │   │   └── themes.xml            # Material Theme
│   │   │   └── drawable/                 # Градиенты и фоны
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## Технические детали

### Десятичное время

Формула конвертации:
```kotlin
val totalSec = hours * 3600 + minutes * 60 + seconds + milliseconds / 1000.0
val decimalFraction = totalSec / 86400.0 * 100000.0

val decimalHours = decimalFraction / 10000      // 0-9
val decimalMinutes = (decimalFraction % 10000) / 100  // 0-99
val decimalSeconds = decimalFraction % 100      // 0-99
```

### Анимация стрелок

- **Секундная стрелка**: обновляется только при изменении целого значения (шаговое движение)
- **Минутная и часовая**: используют дробные значения для плавного движения
- Обновление каждые 100ms для точности

### Custom View

`DecimalClockView` использует Canvas API для рисования:
- Циферблат с прозрачным фоном и blur эффектом
- 10 цифр (0-9) расположенных по кругу
- Метки: тонкие каждые 2 позиции, жирные на цифрах
- 3 стрелки разной длины и толщины

## Иконки приложения

⚠️ **Важно**: Добавьте иконки приложения в директории `app/src/main/res/mipmap-*/`

Используйте [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/) для генерации иконок.

## Лицензия

MIT License

## Автор

Создано на основе HTML прототипа десятичных часов.
