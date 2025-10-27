package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList,Integer> {
    @Query("SELECT bl FROM BlackList bl WHERE bl.deviceId=:deviceId AND bl.deviceName=:deviceName")
    BlackList findByDeviceIdAndDeviceName(String deviceId,String deviceName);
}
