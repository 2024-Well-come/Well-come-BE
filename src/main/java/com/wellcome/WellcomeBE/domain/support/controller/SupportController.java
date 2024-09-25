package com.wellcome.WellcomeBE.domain.support.controller;

import com.wellcome.WellcomeBE.domain.support.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;


    @PostMapping()
    public ResponseEntity<?> createSupport(@RequestParam List<Long> id, @RequestParam String type) {
        supportService.createSupport(id, type);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{supportId}")
    public ResponseEntity<Void> deleteSupport(@PathVariable Long supportId, @RequestParam String type) {
        supportService.deleteSupport(supportId, type);
        return ResponseEntity.ok().build();
    }
}