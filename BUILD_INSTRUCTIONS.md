# Инструкция по сборке Decimal Clock

## Подготовка окружения

### 1. Установка требований

Убедитесь, что у вас установлено:

- **JDK 8 или выше** (рекомендуется JDK 11 или 17)
  ```bash
  java -version
  ```

- **Android SDK** (через Android Studio или command line tools)

### 2. Настройка Android SDK

#### Вариант A: Через Android Studio

1. Скачайте и установите [Android Studio](https://developer.android.com/studio)
2. Откройте Android Studio
3. Перейдите в `Tools > SDK Manager`
4. Установите:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

#### Вариант B: Через Command Line Tools

```bash
# Скачайте Android Command Line Tools
# https://developer.android.com/studio#command-tools

# Установите необходимые пакеты
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 3. Создайте local.properties

В корне проекта создайте файл `local.properties`:

```properties
sdk.dir=/path/to/your/android/sdk
```

**Примеры путей:**
- Windows: `sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
- macOS: `sdk.dir=/Users/YourName/Library/Android/sdk`
- Linux: `sdk.dir=/home/yourname/Android/Sdk`

## Сборка проекта

### Метод 1: Используя существующий Gradle (рекомендуется)

Если у вас уже установлен Gradle:

```bash
# Инициализируйте Gradle Wrapper
gradle wrapper --gradle-version 8.2

# Соберите проект
./gradlew assembleDebug
```

### Метод 2: Скачивание Gradle Wrapper вручную

```bash
# Создайте директорию для wrapper jar
mkdir -p gradle/wrapper

# Скачайте gradle-wrapper.jar
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar \
  -o gradle/wrapper/gradle-wrapper.jar

# Теперь можете собрать проект
./gradlew assembleDebug
```

### Метод 3: Через Android Studio

1. Откройте проект в Android Studio
2. Studio автоматически скачает Gradle Wrapper
3. Нажмите `Build > Make Project` или `Ctrl+F9`
4. Для создания APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

## Типы сборок

### Debug сборка (для разработки)

```bash
./gradlew assembleDebug
```

APK будет создан: `app/build/outputs/apk/debug/app-debug.apk`

### Release сборка (для продакшена)

```bash
./gradlew assembleRelease
```

APK будет создан: `app/build/outputs/apk/release/app-release-unsigned.apk`

**Примечание:** Release APK нужно подписать перед установкой на устройство.

## Подписание Release APK

### 1. Создайте keystore

```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### 2. Добавьте в app/build.gradle

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../my-release-key.jks")
            storePassword "your-store-password"
            keyAlias "my-key-alias"
            keyPassword "your-key-password"
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... rest of release config
        }
    }
}
```

### 3. Соберите подписанный APK

```bash
./gradlew assembleRelease
```

## Установка на устройство

### Через ADB

```bash
# Убедитесь, что устройство подключено
adb devices

# Установите APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Или для переустановки:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Через Android Studio

1. Подключите устройство или запустите эмулятор
2. Нажмите `Run > Run 'app'` или `Shift+F10`

## Устранение проблем

### Проблема: "SDK location not found"

**Решение:** Создайте файл `local.properties` с правильным путем к SDK.

### Проблема: "gradle-wrapper.jar not found"

**Решение:** Выполните одну из команд:

```bash
# Вариант 1: Если установлен Gradle
gradle wrapper

# Вариант 2: Скачайте вручную
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar \
  -o gradle/wrapper/gradle-wrapper.jar
```

### Проблема: "Unsupported class file major version"

**Решение:** Обновите JDK до версии 11 или 17.

### Проблема: Медленная сборка

**Решение:** Добавьте в `gradle.properties`:

```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

## Дополнительные команды

```bash
# Очистить проект
./gradlew clean

# Собрать и установить на подключенное устройство
./gradlew installDebug

# Запустить приложение после установки
adb shell am start -n com.decimalclock/.MainActivity

# Посмотреть логи
adb logcat | grep DecimalClock
```

## Структура выходных файлов

После успешной сборки:

```
app/build/outputs/
├── apk/
│   ├── debug/
│   │   └── app-debug.apk          # Debug APK
│   └── release/
│       └── app-release.apk        # Release APK (подписанный)
└── logs/
    └── manifest-merger-*.txt      # Логи слияния манифестов
```

## Минимальные требования к устройству

- **Min SDK:** Android 5.0 (API 21)
- **Target SDK:** Android 14 (API 34)
- **Архитектуры:** armeabi-v7a, arm64-v8a, x86, x86_64

## Размер APK

- **Debug:** ~2-3 MB
- **Release (минифицированный):** ~1-2 MB
