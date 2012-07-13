package org.mifosng.platform.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TomcatJdbcDataSourcePerTenantService implements DataSourcePerTenantService {

	private final Map<Long, DataSource> tenantToDataSourceMap = new ConcurrentHashMap<Long, DataSource>(1);
	private final DataSource tenantDataSource;
	
	@Autowired
	public TomcatJdbcDataSourcePerTenantService(final @Qualifier("tenantDataSourceJndi") DataSource tenantDataSource) {
		this.tenantDataSource = tenantDataSource;
	}
	
	@Override
	public DataSource retrieveTenantAwareDataSource() {
		
		// default to tenant database datasource
		DataSource tenantDataSource = this.tenantDataSource;
		
		MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
		if (tenant != null) {
			// if tenant information available switch to appropriate datasource for that tenant.
			if (this.tenantToDataSourceMap.containsKey(tenant.getId())) {
				tenantDataSource = this.tenantToDataSourceMap.get(tenant.getId());
			} else {
				tenantDataSource = createNewDataSourceFor(tenant);
				this.tenantToDataSourceMap.put(tenant.getId(), tenantDataSource);
			}			
		}
		
		return tenantDataSource;
	}

	private DataSource createNewDataSourceFor(final MifosPlatformTenant tenant) {
    	// see http://www.tomcatexpert.com/blog/2010/04/01/configuring-jdbc-pool-high-concurrency
    	PoolConfiguration poolConfiguration = new PoolProperties();
    	poolConfiguration.setDriverClassName("com.mysql.jdbc.Driver");
    	poolConfiguration.setName(tenant.getSchemaName() + "_pool");
    	poolConfiguration.setUrl("jdbc:mysql://localhost:3306/" + tenant.getSchemaName());
    	poolConfiguration.setUsername("root");
    	poolConfiguration.setPassword("mysql");
    	
    	poolConfiguration.setInitialSize(5);
    	poolConfiguration.setMaxActive(5);
    	poolConfiguration.setMinIdle(1);
    	poolConfiguration.setMaxIdle(4);
    	
    	poolConfiguration.setSuspectTimeout(60);
    	poolConfiguration.setTimeBetweenEvictionRunsMillis(30000);
    	poolConfiguration.setMinEvictableIdleTimeMillis(60000);
    	
    	poolConfiguration.setTestOnBorrow(true);
    	poolConfiguration.setValidationQuery("SELECT 1");
    	poolConfiguration.setValidationInterval(30000);

    	poolConfiguration.setRemoveAbandoned(true);
    	poolConfiguration.setRemoveAbandonedTimeout(60);
    	poolConfiguration.setLogAbandoned(true);
    	poolConfiguration.setAbandonWhenPercentageFull(50);
    	
    	poolConfiguration.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");
    	
    	return new org.apache.tomcat.jdbc.pool.DataSource(poolConfiguration);
	}
}