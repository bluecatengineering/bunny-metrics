package bunny.metrics.jmx.repository;

import bunny.metrics.jmx.entity.MBean;

import java.util.List;
import java.util.Optional;

public interface MBeansRepository {
    Optional<MBean> getMBean(String mBean, boolean onlyHistory);
    List<String> getAllMBeansNames();
}
