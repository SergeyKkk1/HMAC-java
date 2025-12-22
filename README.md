## HMAC-SHA256 sing simple service implementation
Service which allows you to create HMAC-SHA256 signature for some text message and validate it later.

### Running the application
1. Run this command from HMAC-java folder to build app
```
docker build -t hmac-app .
```
2. Run your docker image
```
docker run -p 8080:8080 hmac-app
```
### How to generate new secret
To generate new secret you need to run Hmac service, you'll see in console message:
```
Do you want to rotate secret? (y/n)
```
You can answer it `y`(rotates secret), `n`(does not rotates secret), `exit`(stops secret rotation process completely and
you won't be able to rotate your secret while the current docker image is running)

You can rotate secret as many times as you want while you don't enter `exit` command in your terminal.

### Config.json file format
```json
{
  "hmacAlg": "HmacSHA256",
  "secret": "IkRvd24gdGhlIHJhYmJpdCBob2xlLiI=",
  "listenPort": 8080,
  "maxMsgSizeBytes": 1048576
}
```
1. "hmacAlg" parameter should contain name of algorithm, which you want to use, currently supported: "HmacSHA256"
2. "secret" parameter contains secret which is used to generate signature for your messages, if you are using HmacSHA256
algorithm or other 256 bit algorithms, the most suitable word is a randomly generated 32 bytes string encoded in base64, this
parameter can be changed using CLI, see: [How to generate new secret](#how-to-generate-new-secret)
3. "listenPort" - port that app is started on
4. "maxMsgSizeBytes" - maximum length of message which app is going to handle

### Http request examples
1. Sing message
```
curl -sS -X POST http://localhost:8080/sign \
  -H 'Content-Type: application/json' \
  -d '{"msg":"hello"}'
```
2. Validate signature
```
curl -sS -X POST http://localhost:8080/verify \
  -H 'Content-Type: application/json' \
  -d '{"msg":"hello","signature":"<скопировать из /sign>"}'
```

### Limitations of education implementation
It is important to mention that Hmac is not asymmetric digital sign, it does not have multikey validation 
and secret rotation is really simple, so you can't use this implementation as reference for real production projects
