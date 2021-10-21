package trino.sidecar.jmx.repository;

import trino.sidecar.jmx.entity.MBean;

import java.util.List;
import java.util.Optional;

public interface MBeansRepository {
    Optional<MBean> getMBean(String mBean, boolean b);
    List<String> getAllMBeansNames();
}
