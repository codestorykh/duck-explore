```curl
curl --location 'http://localhost:8080/jakarta/deposit' \
--header 'Content-Type: application/json' \
--header 'Accept-Language: km' \
--data '{
    "accountNo": "0892",
    "amount": "AD",
    "currency": "KHR ",
    "referenceId": "20261006-000001"
}'
```