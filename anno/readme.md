//当前module也是java library, 如果是android module, apt-module无法依赖当前module
//当前module主要用于定义相关注解
//不能将注解的定义放在app module中,由于app module与apt module不能相互依赖，所以单独实现一个module