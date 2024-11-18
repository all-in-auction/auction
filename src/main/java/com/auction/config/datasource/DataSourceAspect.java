package com.auction.config.datasource;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
public class DataSourceAspect {

    @Before("@annotation(transactional)")
    public void setDataSourceType(Transactional transactional) {
        if (transactional.readOnly()) {
            DataSourceContextHolder.setDataSourceType("SLAVE");
        } else {
            DataSourceContextHolder.setDataSourceType("MASTER");
        }
    }
}
