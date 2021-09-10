package plus.datacenter.core.services;

import plus.datacenter.core.entities.forms.Record;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * 增强记录服务
 */
public interface EnhancedRecordService {

    /**
     * 搜索关联记录
     *
     * @param records  被关联的记录集合
     * @param clientId 应用 ID
     * @return 关联的表单记录
     */
    Flux<Record> searchAssociatedRecords(Collection<Record> records, String clientId);

    /**
     * 根据记录 ID 搜索关联记录
     *
     * @param recordIds 被关联的记录集合
     * @param clientId  应用 ID
     * @return 关联的表单记录
     */
    Flux<Record> searchAssociatedRecordByIds(Collection<String> recordIds, String clientId);

    /**
     * 获取完整的记录对象
     *
     * @param records
     * @param clientId 应用 ID
     * @return
     */
    Flux<Record> getFullRecords(Collection<Record> records, String clientId);

    /**
     * 获取完整的记录对象
     *
     * @param ids
     * @param clientId 应用 ID
     * @return
     */
    Flux<Record> getFullRecordByIds(Collection<String> ids, String clientId);
}
