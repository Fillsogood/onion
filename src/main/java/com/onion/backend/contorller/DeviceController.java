package com.onion.backend.contorller;

import com.onion.backend.dto.SignUpUserDto;
import com.onion.backend.dto.WriteDeviceDto;
import com.onion.backend.entity.Device;
import com.onion.backend.entity.User;
import com.onion.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceController {
  private final UserService userService;

  @Autowired
  public DeviceController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("")
  public ResponseEntity<List<Device>> getDevices() {
    return ResponseEntity.ok(userService.getDevices());
  }

  @PostMapping("")
  public ResponseEntity<Device> createDevice(@Valid @RequestBody WriteDeviceDto writeDeviceDto) {
    return ResponseEntity.ok(userService.createDevice(writeDeviceDto));
  }
}
