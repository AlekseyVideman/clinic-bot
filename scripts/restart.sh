#!/bin/bash

# Замените my-app.jar на уникальную часть имени вашего процесса
PROCESS_NAME="clinic_bot-all.jar"

# Замените на полный путь к вашему jar-файлу
JAR_PATH="/root/bot/clinic_bot-all.jar"

echo "Останавливаем $PROCESS_NAME..."
pkill -f "$PROCESS_NAME"

# Даем процессу секунду на завершение
sleep 1

echo "Запускаем $JAR_PATH..."
nohup java -jar "$JAR_PATH" > /dev/null 2>&1 &