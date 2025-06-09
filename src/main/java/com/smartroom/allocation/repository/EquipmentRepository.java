package com.smartroom.allocation.repository;

import com.smartroom.allocation.entity.Equipment;
import com.smartroom.allocation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Find equipment by name
    List<Equipment> findByName(String name);

    // Find equipment by room
    List<Equipment> findByRoom(Room room);

    // Find available equipment
    List<Equipment> findByWorkingTrue();
}