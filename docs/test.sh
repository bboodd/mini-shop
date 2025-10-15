for i ({1..5000}) {
  curl -X PUT http://localhost:8080/api/products/$i \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"맥북 프로 $i\",
      \"description\": \"M3 칩셋\",
      \"price\": 2890000,
      \"stock\": 5,
      \"category\": \"전자기기\"
    }" &
}
wait
echo "✅ 모든 요청 완료!"
