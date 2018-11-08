# eif-pro 开箱即用的后台解决方案

## 前端与后端请求规范  

### 目前支持的协议   

|协议类型 | 地址                                        | 是否支持
|--------        |-------                                        |-----------
|LINK   |/eif-pro/link.eif | 是
|RSA    |/eif-pro/link.eif | 是

> 以上列出的协议都表示支持的协议，未被列出的协议则表示不被支持的协议

### 基础请求描述
服务器Link地址:    
LINK  http://127.0.0.1:8080/eif-pro/link.eif  

服务器公钥地址:  
RSA   http://127.0.0.1:8080/eif-pro/link.eif  
 
    
假设服务器的地址如上，那么客户端请求服务器的JSON格式应该如下

```
POST /eif-pro/link.eif HTTP/1.1
Accept: application/json, text/javascript, */*; q=0.01
Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
Accept-Encoding: gzip, deflate
Content-Type: application/json; charset=UTF-8
Host: 127.0.0.1:8080
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.5.3 (Java/1.8.0_144)
{
  AK:"",
  AD:
  --- 以下应该是加密啊后的字符串内容
  {
			route:"spring::beanName#method",
			token:"A74D7A6BCC3E6DC86A27AC4DF3852ED5",
			time:1541577428482,
			data:""
	}
}
```

### AK和AD的参数描述 

前端发送请求给后端的时候会有一个AK 参数和一个AD的参数，这是一个由AES-128-CBC的方式进行加密的的内容，而AK则是发送给服务器的AD需要解密的密钥   

>  当发送数据之前，先发送RSA 请求给服务器 http://127.0.0.1:8080/eif-pro/link.eif 会获取公钥，然后通过公钥加密AK的密钥，通常为了安全每次请求都应该生成32位随机的密钥 在org.eif.resources.cert里面配置了RSA的公私钥
