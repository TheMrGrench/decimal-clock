# Быстрый старт - Decimal Clock APK

## ✅ Проверка: что уже готово

Проект полностью готов к сборке! Все файлы на месте:
- ✅ Kotlin код (DecimalClockView, MainActivity)
- ✅ Ресурсы (layouts, colors, themes)
- ✅ Gradle конфигурация
- ✅ AndroidManifest

## 📋 Что нужно сделать (5 минут)

### Вариант 1: Через Android Studio (РЕКОМЕНДУЕТСЯ) 👍

Это самый простой способ!

#### Шаг 1: Установите Android Studio
1. Скачайте с [developer.android.com/studio](https://developer.android.com/studio)
2. Установите (следуйте мастеру установки)
3. При первом запуске он установит Android SDK автоматически

#### Шаг 2: Откройте проект
1. `File → Open`
2. Выберите папку `decimal-clock`
3. Android Studio скачает Gradle и зависимости (первый раз займет 2-5 минут)

#### Шаг 3: Соберите APK
**Способ A: Debug APK (для тестирования)**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

После сборки появится уведомление "APK(s) generated successfully" с кнопкой `locate`.

APK будет здесь: `app/build/outputs/apk/debug/app-debug.apk`

**Способ B: Release APK (для продакшена)**
```
Build → Generate Signed Bundle / APK → APK → Next
```
Нужно будет создать keystore (следуйте мастеру).

#### Шаг 4: Установите на телефон

**4a. Через USB:**
1. Включите "Режим разработчика" на Android:
   - Настройки → О телефоне → 7 раз нажмите "Номер сборки"
2. Включите "Отладка по USB":
   - Настройки → Для разработчиков → Отладка по USB
3. Подключите телефон к компьютеру
4. В Android Studio нажмите Run ▶️ или:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

**4b. Без USB (через файл):**
1. Скопируйте `app-debug.apk` на телефон (через Google Drive, email, Telegram и т.д.)
2. На телефоне откройте APK файл
3. Разрешите установку из неизвестных источников (если попросит)
4. Нажмите "Установить"

---

### Вариант 2: Через командную строку (для опытных)

#### Требования:
- JDK 11 или 17
- Android SDK

#### Шаг 1: Установите Android SDK

**Linux/Mac:**
```bash
# Скачайте Command Line Tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip -d ~/android-sdk
cd ~/android-sdk/cmdline-tools
mkdir latest
mv bin lib latest/

# Установите пакеты
~/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

**Windows:**
Скачайте [Android Command Line Tools](https://developer.android.com/studio#command-tools)

#### Шаг 2: Создайте local.properties

В корне проекта `decimal-clock/`:

**Linux:**
```bash
echo "sdk.dir=$HOME/android-sdk" > local.properties
```

**Windows:**
```cmd
echo sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk > local.properties
```

#### Шаг 3: Инициализируйте Gradle Wrapper

```bash
cd decimal-clock
gradle wrapper --gradle-version 8.2
# Или если gradle не установлен, скачайте jar:
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar \
  -o gradle/wrapper/gradle-wrapper.jar
```

#### Шаг 4: Соберите APK

**Debug версия:**
```bash
./gradlew assembleDebug
```

**Release версия:**
```bash
./gradlew assembleRelease
```

APK будет в `app/build/outputs/apk/debug/app-debug.apk`

#### Шаг 5: Установите на телефон

```bash
# Через USB с включенной отладкой
adb install app/build/outputs/apk/debug/app-debug.apk

# Или просто скопируйте APK файл на телефон
```

---

## 🎯 Что делает APK

После установки вы получите приложение с:

- ⏰ Десятичными часами (10h, 100m, 100s)
- 🎨 6 цветовыми темами с градиентами
- 💎 Glassmorphism дизайном циферблата
- ⚙️ Плавной анимацией минутной/часовой стрелок
- ⏱️ Шаговым движением секундной стрелки
- 💾 Сохранением настроек
- 🕐 Показом обычного времени (можно скрыть)

---

## ❓ Проблемы и решения

### "SDK location not found"
**Решение:** Создайте файл `local.properties` с путем к Android SDK

### "Gradle wrapper not found"
**Решение:** Выполните `gradle wrapper` или откройте проект в Android Studio

### "Installation failed"
**Решение:**
- Включите "Установка из неизвестных источников"
- Или включите "Отладка по USB" и используйте adb

### "Build failed"
**Решение:**
- Убедитесь, что установлены:
  - JDK 11/17
  - Android SDK Platform 34
  - Build Tools 34.0.0

---

## 📱 Минимальные требования

- **Для сборки:** JDK 11+, Android SDK 34
- **Для установки:** Android 5.0 (API 21) и выше

---

## 🔥 Самый быстрый способ (TL;DR)

1. Скачайте Android Studio
2. Откройте проект `decimal-clock`
3. Нажмите Build → Build APK(s)
4. Скопируйте APK на телефон
5. Установите

**Время: 5-10 минут**

---

Создано с ❤️ на основе HTML прототипа десятичных часов
