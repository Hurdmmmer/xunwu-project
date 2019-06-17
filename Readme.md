### `@ModelAttribute` 注解, 用于绑定 form 表单参数
### 使用 h2 数据库
1. 需要再 resources 文件中创建 db 文件夹
2. 创建查询的数据约束, schema.sql 定义数据库的表结构
3. data.sql 定义数据中的数据
4. 配置文件指定 h2 数据库的约束文件(schema.sql, data.sql), 参见 application-test.yml

### spring boot 2.x 静态资源映射
实现 `WebMvcConfiguer` 接口, 重写 `addResourceHandlers` 方法:
```java
    @Configuration
    public class WebMvcConfig implements ApplicationContextAware, WebMvcConfigurer{
    
        private ApplicationContext applicationContext;
    
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        }
    }
```
### 关闭 spring security basic 基础验证
 spring boot 2.x 再 `@SpringBootApplication` 排除即可
 ```java
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class App{
    public static void main(String[] args){
      
    }
}

```
spring boot 1.X 再 `application.yml` 配置文件中配置
```yaml
# 关闭security basic 基本验证
security: # 低版本使用, 2.x 已经废弃
  basic:
    enabled: false
```
### 禁止 spring boot 生成 Whitelabel Error Page
在 `application.yml` 中配置:
```yaml
# 禁止 spring boot 生成错误页面
server:
  error:
    whitelabel:
      enabled: false
```

### spring boot 全局异常处理
1. 设置配置文件取消 spring boot 自动生成的错误页面
```yaml
# 禁止 spring boot 生成错误页面
server:
  error:
    whitelabel:
      enabled: false
```
2. 继承 `ErrorController` 接口,
```java
@Controller
public class AppErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";
    /** 固定错误状态码错误 key */
    private static final String ERROR_STATUS_CODE_STRING = "javax.servlet.error.status_code";

    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @Autowired
    public AppErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Web 页面异常处理
     */
    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public String errorPageHandler(HttpServletRequest request, HttpServletResponse response) {
        int status = response.getStatus();
        switch (status) {
            case 403:
                return "403";
            case 404:
                return "404";
            case 500:
                return "500";
        }
        return "index";
    }
    /**
     * 除 web 页面以外的异常处理, 如 json xml等
     */
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ApiResponse errorApiHandler(HttpServletRequest request, HttpServletResponse response) {
        WebRequest requestAttribute = new StandardServletAsyncWebRequest(request, response);
        // 获取报错信息
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(requestAttribute, false);
        Integer status = getStatus(request);
        Set<Map.Entry<String, Object>> entries = errorAttributes.entrySet();
        String errorMessage = entries.stream().
                filter(e -> e.getKey().equalsIgnoreCase("error") || e.getKey().equalsIgnoreCase("message"))
                .map(e -> e.getValue() + "").collect(Collectors.joining("; "));
        return ApiResponse.ofMessage(status, errorMessage);
    }

    /** 获取错误状态码 */
    private Integer getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(ERROR_STATUS_CODE_STRING);
        if (statusCode != null) {
            return statusCode;
        }
        return 500;
    }
}
```
### 自定义spring security 登录
1. 继承 `AbstractAuthenticationProcessingFilter`
```java
/**
 * 基于短信登录验证
 */
public class SMSLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final static String SPRING_SECURITY_FORM_SMS_CODE_KEY = "telephone";

    /**
     * 配置登录拦截的url, 拦截的请求方法
     */
    public SMSLoginFilter() {
        super(new AntPathRequestMatcher("/login/mobile", "POST"));
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        String mobileNo = request.getParameter(SPRING_SECURITY_FORM_SMS_CODE_KEY);

        if (mobileNo == null) {
            mobileNo = "";
        }
        // 模仿 UsernamePasswordAuthenticationFilter 做法
        SMSAuthenToken smsAuthenToken = new SMSAuthenToken(mobileNo);
        this.setDetails(request, smsAuthenToken);
        return this.getAuthenticationManager().authenticate(smsAuthenToken);
    }
    /**  参照 UsernamePasswordAuthenticationFilter spring security 写法  */
    protected void setDetails(HttpServletRequest request, SMSAuthenToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}

```
2. 创建验证提供者实现接口 `AuthenticationProvider`, 自定义验证支持的 token 对象
```java
/**
 * 短信验证提供者, 
 */
@AllArgsConstructor
public class SMSAuthenProvider implements AuthenticationProvider {
    private IUserService userService;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 使用手机号码查询用户信息
        User dbUser = userService.findUserByName(authentication.getName());
        if (dbUser == null) {
            throw new AuthenticationCredentialsNotFoundException("该用户不存在");
        }
        // 返回自定义短信认证信息
        return new SMSAuthenToken(dbUser, dbUser.getPassword(), dbUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        // 支持 我们自定义的 token
        return SMSAuthenToken.class.isAssignableFrom(aClass);
    }
}
```
创建自定义验证的 token 继续 `AbstractAutenticationToken` 抽象类, 注意构造器即可
```java
public class SMSAuthenToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /** 手机登录, 默认使用手机号码为账号 */
    public SMSAuthenToken(String mobile) {
        super(null);
        this.principal = mobile;
        setAuthenticated(false);
    }

    public SMSAuthenToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }
    // ... 其他跟 spring security 的 UsernamePasswordAuthenticationToken 一致
}
```
3. 配置自定义的认证规则, 继承 `SecurityConfigurerAdapter` 
```java
@Component
public class SmscodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private IUserService userDetailsService;

    @Autowired
    private LoginFailHandler failHandler;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        SMSLoginFilter smsLoginFilter = new SMSLoginFilter();
        // 设置成功失败过滤器
        smsLoginFilter.setAuthenticationFailureHandler(failHandler);
        smsLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        // 设置授权管理器
        smsLoginFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));

        SMSAuthenProvider smsAuthenticationProvider = new SMSAuthenProvider(userDetailsService);

        // 注册 sms 授权提供者, 再 usernamePassWordFilter 之后
        http.authenticationProvider(smsAuthenticationProvider)
                .addFilterAfter(smsLoginFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```
4. 注入我们自定义的认证规则
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    // ... 以上代码省略
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 添加短信验证码过滤器, 再账号密码之前认证
        http.addFilterBefore(smsCodeFilter(), UsernamePasswordAuthenticationFilter.class);
        // 其他配置代码省略
        http.apply(smscodeAuthenticationSecurityConfig) ; // 配置短信登录
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin(); // 开启同源策略 iframe 就可以使用了
    }
    // ... 以下代码省略
}
```
### elasticsearch 创建索引
http://IP:9200/xunwu
<br>PUT 请求, 发送 JSON 格式: 
```json
{
  "setting" :{
    "number_of_replica": 0, // 0 为不设置备份
    "analysis" : {  // 设置 IK 分词器
        "analyzer" : {
          "ik" : {
              "tokenizer" : "ik_max_word"
            }
         }
    }
  },
  "mappings" :{ 
    "house":{  
      "dynamic": false,   // 取消动态创建索引
      "properties" :{  
        "title":{
          "type": "text",
          "analyzer": "ik_max_word",  // 使用IK分词器
          "search_analyzer": "ik_max_word" // 搜索时用的分词器
        },
        "houseId": {
          "type": "long"
        },
        "price":{
          "type":"integer"
        },
        "area" :{
          "type":"integer"
        },
        "createTime":{
          "type":"date",
          "format":"strict_date_optional_time||epoch_millis" 
        },
        "lastUpdateTime": {
          "type":"date",
          "format":"strict_date_optional_time||epoch_millis" 
        },
        "cityEnName": {
          "type":"keyword"
        },
        "regionEnName":{
          "type":"keyword"
        },
        "direction":{
          "type":"integer"
        },
        "distanceToSubway":{
          "type":"integer"
        },
        "subwayLineName":{
          "type":"keyword"
        },
        "subwayStationName":{
          "type":"keyword"
        },
        "tags": {
          "type":"text"
        },
        "street":{
          "type":"keyword"
        },
        "district":{
          "type":"keyword"
        },
        "description":{
          "type":"text",
          "analyzer": "ik_max_word",  // 使用IK分词器
          "search_analyzer": "ik_max_word" // 搜索时用的分词器
        },
        "layoutDesc":{
          "type":"text",
          "analyzer": "ik_max_word",  // 使用IK分词器
          "search_analyzer": "ik_max_word" // 搜索时用的分词器
        },
        "traffic" :{
          "type":"text",
          "analyzer": "ik_max_word",  // 使用IK分词器
          "search_analyzer": "ik_max_word" // 搜索时用的分词器
        },
        "roundService" :{
          "type":"text",
          "analyzer": "ik_max_word",  // 使用IK分词器
          "search_analyzer": "ik_max_word" // 搜索时用的分词器
        },
        "rentWay" :{
          "type":"integer"
        },
        "suggest": {
          "type": "completion"      // es 建议类型
        }
      }
    }
  }
}
```

