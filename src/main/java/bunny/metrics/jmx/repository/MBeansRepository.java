package bunny.metrics.jmx.repository;

import bunny.metrics.jmx.entity.MBean;

import java.util.List;

public interface MBeansRepository {
    List<MBean> getMBean(String mBeanName, boolean onlyHistory);
    List<String> getAllMBeansNames();
}
