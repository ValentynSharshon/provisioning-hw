package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    @GetMapping(path = "provisioning/{macAddress}")
    public String getProvisionConfigFile(@PathVariable("macAddress") String macAddress) throws Exception {
        log.info("Trying to get provision file for device with MAC address: {}", macAddress);
        return provisioningService.getProvisioningFile(macAddress);
    }
}
