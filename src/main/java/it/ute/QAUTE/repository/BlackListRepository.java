package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.BlackList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList,Integer> {
    @Query("SELECT bl FROM BlackList bl WHERE bl.deviceId=:deviceId AND bl.deviceName=:deviceName")
    BlackList findByDeviceIdAndDeviceName(String deviceId,String deviceName);
    @Modifying
    @Transactional
    @Query("DELETE FROM BlackList bl WHERE bl.deviceId = :deviceId AND bl.deviceName = :deviceName")
    void deleteByDeviceIdAndDeviceName(@Param("deviceId") String deviceId,
                                       @Param("deviceName") String deviceName);
}
