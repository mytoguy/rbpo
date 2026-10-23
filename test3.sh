#!/bin/bash

BASE_URL="http://localhost:8080/api"

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Динамические даты для абонемента (сегодня и через год)
START_DATE=$(date +%Y-%m-%d)
END_DATE=$(date -d "+1 year" +%Y-%m-%d)

echo -e "${YELLOW}=== 1. CRUD ДЛЯ ПОЛЬЗОВАТЕЛЕЙ (USERS) ===${NC}"
# CREATE
USER_ID=$(curl -s -X POST "$BASE_URL/users" -H "Content-Type: application/json" -d '{"name":"Иван"}' | jq -r '.id')
echo "Create: Создан пользователь ID=$USER_ID"
# READ ALL
USERS_COUNT=$(curl -s "$BASE_URL/users" | jq '. | length')
echo "Read All: Количество пользователей = $USERS_COUNT"
# READ BY ID
USER_NAME=$(curl -s "$BASE_URL/users/$USER_ID" | jq -r '.name')
echo "Read By ID: Имя пользователя = $USER_NAME"
# UPDATE
curl -s -X PATCH "$BASE_URL/users/$USER_ID" -H "Content-Type: application/json" -d '{"name":"Иван Обновленный"}' > /dev/null
UPDATED_NAME=$(curl -s "$BASE_URL/users/$USER_ID" | jq -r '.name')
echo "Update: Новое имя = $UPDATED_NAME"

echo -e "\n${YELLOW}=== 2. CRUD ДЛЯ ЗОН (ZONES) ===${NC}"
# CREATE
ZONE_ID=$(curl -s -X POST "$BASE_URL/parking-zones" -H "Content-Type: application/json" -d '{"capacity":5,"hourlyRate":100.00}' | jq -r '.id')
echo "Create: Создана зона ID=$ZONE_ID"
# READ ALL
ZONES_COUNT=$(curl -s "$BASE_URL/parking-zones" | jq '. | length')
echo "Read All: Количество зон = $ZONES_COUNT"
# UPDATE
curl -s -X PATCH "$BASE_URL/parking-zones/$ZONE_ID" -H "Content-Type: application/json" -d '{"hourlyRate":200.00}' > /dev/null
NEW_RATE=$(curl -s "$BASE_URL/parking-zones/$ZONE_ID" | jq -r '.hourlyRate')
echo "Update: Новый тариф = $NEW_RATE"

echo -e "\n${YELLOW}=== 3. CRUD ДЛЯ МАШИН (VEHICLES) ===${NC}"
# CREATE
VEHICLE_ID=$(curl -s -X POST "$BASE_URL/vehicles?ownerId=$USER_ID" -H "Content-Type: application/json" -d '{"licensePlate":"А001АА77"}' | jq -r '.id')
echo "Create: Создана машина ID=$VEHICLE_ID"
# READ ALL
VEHICLES_COUNT=$(curl -s "$BASE_URL/vehicles" | jq '. | length')
echo "Read All: Количество машин = $VEHICLES_COUNT"
# UPDATE
curl -s -X PATCH "$BASE_URL/vehicles/$VEHICLE_ID" -H "Content-Type: application/json" -d '{"licensePlate":"В999ВВ99"}' > /dev/null
NEW_PLATE=$(curl -s "$BASE_URL/vehicles/$VEHICLE_ID" | jq -r '.licensePlate')
echo "Update: Новый номер = $NEW_PLATE"

echo -e "\n${YELLOW}=== 4. CRUD ДЛЯ АБОНЕМЕНТОВ (PASSES) ===${NC}"
# CREATE (Используем динамические даты: $START_DATE и $END_DATE)
PASS_ID=$(curl -s -X POST "$BASE_URL/parking-passes?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID&startDate=$START_DATE&endDate=$END_DATE" | jq -r '.id')
echo "Create: Создан абонемент ID=$PASS_ID (с $START_DATE по $END_DATE)"
# READ ALL
PASSES_COUNT=$(curl -s "$BASE_URL/parking-passes" | jq '. | length')
echo "Read All: Количество абонементов = $PASSES_COUNT"

echo -e "\n${YELLOW}=== 5. БИЗНЕС-ОПЕРАЦИИ И ПРОВЕРКИ ===${NC}"
echo -e "-> Проверка: Нельзя начать парковку при наличии активного абонемента (затрагивает 2 сущности)"

# Начинаем сессию
SESSION_ID=$(curl -s -X POST "$BASE_URL/parking-sessions?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID" | jq -r '.id')
echo -e "БО 1: Сессия начата ID=$SESSION_ID"

# Пытаемся припарковать ту же машину снова (должна быть ошибка)
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/parking-sessions?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "500" ]; then
    echo -e "${GREEN}УСПЕХ: Система заблокировала повторную парковку${NC}"
else
    echo -e "${RED}ОШИБКА: Система позволила припарковать машину дважды!${NC}"
fi

# Завершаем сессию
sleep 1
RESPONSE=$(curl -s -X PATCH "$BASE_URL/parking-sessions/$SESSION_ID")
COST=$(echo "$RESPONSE" | jq -r '.totalCost')
if [ "$COST" == "0.00" ] || [ "$COST" == "0" ]; then
    echo -e "${GREEN}УСПЕХ: Стоимость составила 0, так как сработал абонемент${NC}"
else
    echo -e "${RED}ОШИБКА: Абонемент не сработал, стоимость: $COST${NC}"
fi

echo -e "\n-> Проверка: Нельзя удалить зону с активным абонементом"
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/parking-zones/$ZONE_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "500" ]; then
    echo -e "${GREEN}УСПЕХ: Система заблокировала удаление зоны (Ошибка 500)${NC}"
else
    echo -e "${RED}ОШИБКА: Система удалила зону с абонементом!${NC}"
fi

echo -e "\n-> Проверка: Нельзя передать машину, пока она на парковке"
SESSION2_ID=$(curl -s -X POST "$BASE_URL/parking-sessions?vehicleId=$VEHICLE_ID&zoneId=$ZONE_ID" | jq -r '.id')
USER2_ID=$(curl -s -X POST "$BASE_URL/users" -H "Content-Type: application/json" -d '{"name":"Петр"}' | jq -r '.id')

RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/vehicles/$VEHICLE_ID/transfer?newOwnerId=$USER2_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "500" ]; then
    echo -e "${GREEN}УСПЕХ: Система заблокировала передачу (Ошибка 500)${NC}"
else
    echo -e "${RED}ОШИБКА: Система позволила передать припаркованную машину!${NC}"
fi

# Завершаем вторую сессию (чтобы потом можно было удалить)
curl -s -X PATCH "$BASE_URL/parking-sessions/$SESSION2_ID" > /dev/null

echo -e "\n-> Успешная передача машины (не на парковке)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/vehicles/$VEHICLE_ID/transfer?newOwnerId=$USER2_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "200" ]; then
    echo -e "${GREEN}УСПЕХ: Машина успешно передана${NC}"
else
    echo -e "${RED}ОШИБКА: Не удалось передать машину${NC}"
fi

echo -e "\n${YELLOW}=== 6. ОЧИСТКА (DELETE) ===${NC}"
# Удаляем все созданное (проверяем Delete)
curl -s -X DELETE "$BASE_URL/parking-passes/$PASS_ID" > /dev/null
echo "Delete: Абонемент удален"
curl -s -X DELETE "$BASE_URL/parking-zones/$ZONE_ID" > /dev/null
echo "Delete: Зона удалена (теперь можно, абонемента нет)"
curl -s -X DELETE "$BASE_URL/vehicles/$VEHICLE_ID" > /dev/null
echo "Delete: Машина удалена"
curl -s -X DELETE "$BASE_URL/users/$USER_ID" > /dev/null
curl -s -X DELETE "$BASE_URL/users/$USER2_ID" > /dev/null
echo "Delete: Пользователи удалены"

echo -e "\n${YELLOW}=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===${NC}"