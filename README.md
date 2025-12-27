distributed-rate-limiter/
│
├── src/
│   ├── main/
│   │   ├── java/com/ratelimiter/
│   │   │   │
│   │   │   ├── RateLimiterApplication.java       
│   │   │   │
│   │   │   ├── config/                             
│   │   │   │   └── RedisConfig.java                
│   │   │   │
│   │   │   ├── model/                               
│   │   │   │   ├── RateLimitResult.java             
│   │   │   │   └── RateLimitContext.java            
│   │   │   │
│   │   │   ├── algorithm/                         
│   │   │   │   └── TokenBucketAlgorithm.java       
│   │   │   │
│   │   │   ├── service/                            
│   │   │   │   └── RateLimiterService.java     
│   │   │   │
│   │   │   ├── interceptor/                        
│   │   │   │   └── RateLimitInterceptor.java        
│   │   │   │
│   │   │   ├── util/                               
│   │   │   │   └── IdentifierExtractor.java        
│   │   │   │
│   │   │   ├── controller/                          
│   │   │   │   ├── DemoController.java             
│   │   │   │   └── AdminController.java             
│   │   │   │
│   │   │   └── exception/                          
│   │   │       ├── RateLimitExceededException.java  
│   │   │       └── GlobalExceptionHandler.java      
│   │   │
│   │   └── resources/
│   │       └── application.properties                
│   │
│   └── test/                                        
│
├── pom.xml                                         
└── README.md                                        
