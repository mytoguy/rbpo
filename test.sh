#!/bin/bash

# ============================================
# Тестовый скрипт для API "Парковка с абонементами"
# ============================================

BASE_URL="http://localhost:8080/api"

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Функция для красивого вывода заголовков
print_step() {
    echo ""
    echo -e "${BLUE}=================================================${NC}"
    echo -e "${BLUE}>>> $1${NC}"
    echo -e "${BLUE}=================================================${NC}"
}

print_result() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

# Проверка, что приложение запущено
echo -e "${YELLOW}Проверяем доступность приложения на $BASE_URL...${NC}"
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users" | grep -q "200"; then
    echo -e "${RED}Ошибка: приложение не отвечает на $BASE_URL${NC}"
    echo "Убедитесь, что 'mvn spring-boot:run' запущен."
    exit 1
fi
echo -e "${GREEN}Приложение доступно!${NC}"


# ============================================
# 1. USER - создание пользователя
# ============================================
print_step "1. Создание пользователя (POST /users)"
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/users" \
    -H "Content-Type: application/json" \
    -d '{"name": "Иван"}')
echo "Ответ: $USER_RESPONSE"
USER_ID=$(echo "$USER_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$USER_ID" ]; then
    print_result "Пользователь создан с id=$USER_ID"
else
    print_error "Не удалось создать пользователя"
    exit 1
fi


# ============================================
# 2. VEHICLE - создание машины
# ============================================
print_step "2. Создание машины (POST /vehicles?ownerId=$USER_ID)"
VEHICLE_RESPONSE=$(curl -s -X POST "$BASE_URL/vehicles?ownerId=$USER_ID" \
    -H "Content-Type: application/json" \
    -d '{"licensePlate": "A123BC"}')
echo "Ответ: $VEHICLE_RESPONSE"
VEHICLE_ID=$(echo "$VEHICLE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
if [ -n "$VEHICLE_ID" ]; then
    print_result "Машина создана с id=$VEHICLE_ID"
else
    print_error "Не удалось создать машину"
    exit 1
fi


# ============================================
# 3. PARKING ZONE - создание зоны
# ============================================
print_step "3. Создание парковочной зоны (POST /parking-zones)"
ZONE_RESPONSE=$(curl -s -X POST "$BASE_URL/parking-zones" \
    -H "Content-Type: application/json" \
    -d '{"capacity": 10, "hourlyRate": 150.00}')
echo "Ответ: $ZONE_RESPONSE"
ZONE_ID=$(echo "$ZONE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$ZONE_ID" ]; then
    print_result "Зона создана с id=$ZONE_ID"
else
    print_error "Не удалось создать зону"
    exit 1
fi


# ============================================
# 4. PARKING SESSION - начало парковки
# ============================================
print_step "4. Начало парковочной сессии (POST /parking-sessions)"
SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/parking-sessions?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID")
echo "Ответ: $SESSION_RESPONSE"
SESSION_ID=$(echo "$SESSION_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
if [ -n "$SESSION_ID" ]; then
    print_result "Сессия открыта с id=$SESSION_ID"
else
    print_error "Не удалось открыть сессию"
    exit 1
fi


# ============================================
# 5. PARKING SESSION - завершение + расчёт
# ============================================
print_step "5. Завершение сессии (PATCH /parking-sessions/$SESSION_ID)"
sleep 1
END_RESPONSE=$(curl -s -X PATCH "$BASE_URL/parking-sessions/$SESSION_ID")
echo "Ответ: $END_RESPONSE"
if echo "$END_RESPONSE" | grep -q "totalCost"; then
    print_result "Сессия завершена, сумма рассчитана"
else
    print_error "Не удалось завершить сессию"
fi


# ============================================
# 6. PARKING PASS - создание абонемента
# ============================================
print_step "6. Создание абонемента (POST /parking-passes)"
TODAY=$(date +%Y-%m-%d)
FUTURE=$(date -d "+30 days" +%Y-%m-%d)
PASS_RESPONSE=$(curl -s -X POST "$BASE_URL/parking-passes?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID&startDate=$TODAY&endDate=$FUTURE")
echo "Ответ: $PASS_RESPONSE"
PASS_ID=$(echo "$PASS_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$PASS_ID" ]; then
    print_result "Абонемент создан с id=$PASS_ID"
else
    print_error "Не удалось создать абонемент"
fi


# ============================================
# 7. GET - чтение всех сущностей
# ============================================
print_step "7. Чтение всех сущностей"

echo ""
echo "GET /users:"
curl -s "$BASE_URL/users" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/users"

echo ""
echo "GET /vehicles:"
curl -s "$BASE_URL/vehicles" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/vehicles"

echo ""
echo "GET /parking-zones:"
curl -s "$BASE_URL/parking-zones" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/parking-zones"

echo ""
echo "GET /parking-sessions:"
curl -s "$BASE_URL/parking-sessions" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/parking-sessions"

echo ""
echo "GET /parking-passes:"
curl -s "$BASE_URL/parking-passes" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/parking-passes"

print_result "Чтение выполнено"


# ============================================
# 8. PATCH - обновление машины
# ============================================
print_step "8. Обновление машины (PATCH /vehicles/$VEHICLE_ID)"
PATCH_RESPONSE=$(curl -s -X PATCH "$BASE_URL/vehicles/$VEHICLE_ID" \
    -H "Content-Type: application/json" \
    -d '{"licensePlate": "X999YZ"}')
echo "Ответ: $PATCH_RESPONSE"
if echo "$PATCH_RESPONSE" | grep -q "X999YZ"; then
    print_result "Машина обновлена"
else
    print_error "Не удалось обновить машину"
fi


# ============================================
# 9. DELETE - каскадное удаление
# ============================================
print_step "9. Каскадное удаление пользователя (DELETE /users/$USER_ID)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/users/$USER_ID")
echo "HTTP статус: $HTTP_CODE (ожидается 204)"
if [ "$HTTP_CODE" = "204" ]; then
    print_result "Пользователь удалён (статус 204)"
else
    print_error "Неверный статус при удалении: $HTTP_CODE"
fi

echo ""
echo "Проверяем, что машина удалилась каскадом:"
VEHICLES_AFTER=$(curl -s "$BASE_URL/vehicles")
echo "GET /vehicles: $VEHICLES_AFTER"
if [ "$VEHICLES_AFTER" = "[]" ]; then
    print_result "Каскадное удаление работает!"
else
    print_error "Машины остались в БД: $VEHICLES_AFTER"
fi


# ============================================
# ИТОГ
# ============================================
print_step "ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ"
echo -e "${GREEN}Если все пункты [OK] — задание выполнено корректно!${NC}"
