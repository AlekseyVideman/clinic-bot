#!/usr/bin/env bash

# ===============================================================
# Скрипт: deploy_cert.sh
# Назначение: Выпуск/обновление сертификата через acme.sh и сборка PKCS#12
# Требования: bash 4+, установленный acme.sh, openssl
# Адаптировано для запуска из systemd (Type=oneshot)
# ===============================================================

# --- Переменные окружения (могут быть переопределены в systemd unit) ---
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/root/.acme.sh"
export LE_WORKING_DIR="/root/.acme.sh"
export OPENSSL_BIN="/usr/bin/openssl" 

# --- Bash Strict Mode ---
set -Eeuo pipefail
IFS=$'\n\t'

# --- Обработчики сигналов для корректного завершения (важно для oneshot) ---
cleanup() {
    log "INFO" "Получен сигнал завершения. Выполняется очистка временных процессов."
    # Если acme.sh запущен в фоне, его нужно принудительно убить
    pkill -f "acme.sh --renew" 2>/dev/null || true
    exit 1
}
trap cleanup SIGTERM SIGINT

# --- Логирование ---
log() {
    local level="$1"
    local message="$2"
    printf '[%s] [%s] %s\n' "$(date --iso-8601=seconds)" "$level" "$message" >&2
}

# --- Конфигурация ---
# Вычисляем абсолютный путь к директории скрипта (без перехода в нее через cd)
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Переменная DOMAIN должна быть передана извне через systemd Environment= или EnvironmentFile=
DOMAIN="${DOMAIN:-}"
readonly DOMAIN

# Пути к сертификатам acme.sh (привязаны к домашней директории root)
readonly CERT_DIR="/root/.acme.sh/${DOMAIN}"
readonly CERT_PEM="${CERT_DIR}/${DOMAIN}.cer"
readonly KEY_PEM="${CERT_DIR}/${DOMAIN}.key"

# Пути к файлам ЦА (используем абсолютные пути от SCRIPT_DIR)
readonly SUB_CA_2024="${SCRIPT_DIR}/Russian_Trusted_Sub_CA_2024.cer"
readonly SUB_CA="${SCRIPT_DIR}/Russian_Trusted_Sub_CA.cer"
readonly ROOT_CA="${SCRIPT_DIR}/Russian_Trusted_Root_CA.cer"

# Пути к выходным файлам (используем абсолютные пути)
readonly CHAIN_FILE="${SCRIPT_DIR}/chain.cer"
readonly PKCS12_FILE="${SCRIPT_DIR}/cert.p12"

# Пароль для PKCS#12
PKCS12_PASS="${PKCS12_PASS:-java}"
readonly PKCS12_PASS

# --- Проверка зависимостей ---
for cmd in bash acme.sh openssl; do
    if ! command -v "$cmd" &> /dev/null; then
        log "ERROR" "Утилита '$cmd' не найдена в PATH."
        exit 127
    fi
done

# --- Проверка свободного места (критично для --standalone) ---
MIN_FREE_MB=500
AVAILABLE_MB=$(df . --output=avail | tail -n1)
if (( AVAILABLE_MB < MIN_FREE_MB )); then
    log "ERROR" "Недостаточно свободного места на диске: ${AVAILABLE_MB}MB доступно, требуется ${MIN_FREE_MB}MB."
    exit 1
fi

# --- Валидация наличия файлов ЦА ---
for file in "$SUB_CA_2024" "$SUB_CA" "$ROOT_CA"; do
    if [[ ! -f "$file" ]]; then
        log "ERROR" "Файл цепочки '$(basename "$file")' не найден в директории $SCRIPT_DIR"
        exit 1
    fi
done

log "INFO" "Начинаю процесс обновления и упаковки сертификата для домена: ${DOMAIN}"

# --- 1. Выпуск/обновление сертификата ---
log "INFO" "Запуск acme.sh для домена ${DOMAIN}..."
if ! acme.sh --renew \
    --domain "${DOMAIN}" \
    --standalone \
    --keylength 2048 \
    --force \
    --always-force-new-domain-key; then
    log "ERROR" "acme.sh завершился с ошибкой."
    exit 1
fi

# Проверяем, создал ли acme.sh нужные файлы
if [[ ! -f "$CERT_PEM" || ! -f "$KEY_PEM" ]]; then
    log "ERROR" "acme.sh завершил работу, но файлы сертификата не найдены."
    exit 1
fi

log "INFO" "Сертификат успешно обновлен."

# --- 2. Сборка цепочки сертификатов ---
log "INFO" "Сборка цепочки сертификатов в ${CHAIN_FILE}..."
{
    cat "$CERT_PEM"
    cat "$SUB_CA_2024"
    cat "$SUB_CA"
    cat "$ROOT_CA"
} > "$CHAIN_FILE"

# --- 3. Создание контейнера PKCS#12 ---
log "INFO" "Создание PKCS#12 контейнера..."
if ! openssl pkcs12 -export \
    -in "$CERT_PEM" \
    -inkey "$KEY_PEM" \
    -certfile "$CHAIN_FILE" \
    -out "$PKCS12_FILE" \
    -name "webhook_cert" \
    -passout "pass:${PKCS12_PASS}"; then
    log "ERROR" "OpenSSL завершился с ошибкой при создании PKCS#12."
    exit 1
fi

log "SUCCESS" "Контейнер успешно создан: ${PKCS12_FILE}"

./restart.sh

exit 0