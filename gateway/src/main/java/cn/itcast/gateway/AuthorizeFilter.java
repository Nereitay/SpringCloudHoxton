package cn.itcast.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * - order值越小，优先级越高
 * - 当过滤器的order值一样时，会按照
 * defaultFilter （由上到下 1， 2， 3）>
 * 路由过滤器 （由上到下 1， 2， 3）>
 * GlobalFilter (Ordered/@Order) 的顺序执行
 *
 * RouteDefinitionRouteLocator#getFilters()方法是先加载defaultFilters，
 * 然后再加载某个route的filters，然后合并
 *
 * FilteringWebHandler#handle()方法会加载全局过滤器，
 * 与前面的过滤器合并后根据order排序，组织过滤器链
 *
 */
@Component
//@Order(-1)
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取请求参数
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> params = request.getQueryParams();
        // 2. 获取参数中的authorization参数
        String auth = params.getFirst("authorization");
        // 3. 判断参数是否等于admin
        if ("admin".equals(auth)) {
            // 4. 是，放行
            return chain.filter(exchange);
        }
        // 5. 否，拦截
        // 5.1 设置状态码
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // 5.2 拦截请求
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
