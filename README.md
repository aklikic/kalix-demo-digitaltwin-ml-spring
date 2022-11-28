# No aggregation
```
curl -XPOST -d '{
  "name": "12345"
}' http://localhost:9000/dt/1/create -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw1": "0.92",
  "raw2": "0.328501935"
}' http://localhost:9000/dt/1/metric -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw1": "0.72",
  "raw2": "0.002764904"
}' http://localhost:9000/dt/1/metric -H "Content-Type: application/json"
```
```
curl -XGET http://localhost:9000/dt/1 -H "Content-Type: application/json"
```
```
curl -XPOST http://localhost:9000/dt/1/set-maintenance-performed -H "Content-Type: application/json"
```


#Aggregation
```
curl -XPOST -d '{
  "name": "54321"
}' http://localhost:9000/dt/2/create -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw": "0.92"
}' http://localhost:9000/dt/2/aggregation/metric-raw-1 -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw": "0.328501935"
}' http://localhost:9000/dt/2/aggregation/metric-raw-2 -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw": "0.72"
}' http://localhost:9000/dt/2/aggregation/metric-raw-1 -H "Content-Type: application/json"
```
```
curl -XPOST -d '{
  "raw": "0.002764904"
}' http://localhost:9000/dt/2/aggregation/metric-raw-2 -H "Content-Type: application/json"
```
```
curl -XGET http://localhost:9000/dt/2 -H "Content-Type: application/json"
```
```
curl -XPOST http://localhost:9000/dt/2/set-maintenance-performed -H "Content-Type: application/json"
```