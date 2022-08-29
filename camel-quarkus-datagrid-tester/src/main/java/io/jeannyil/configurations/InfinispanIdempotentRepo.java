package io.jeannyil.configurations;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.component.infinispan.remote.InfinispanRemoteIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;

@ApplicationScoped
public class InfinispanIdempotentRepo {

    @Inject
    RemoteCacheManager remoteCacheManager;

    @ConfigProperty(name="datagrid.caches.idempotency")
    String idempotencyCache;
    
    @Produces
    @Named("infinispanRepo")
    public InfinispanRemoteIdempotentRepository createInfinispanIdempotentRepo() {
        InfinispanRemoteIdempotentRepository repo = new InfinispanRemoteIdempotentRepository(idempotencyCache);
        repo.setCacheContainer(remoteCacheManager);
        return repo;

    }
}
