package com.gam.wms.controller;

import com.gam.wms.dto.ReturnManifestRequestDTO;
import com.gam.wms.model.ManifestEntity;
import com.gam.wms.model.ReturnItemEntity;
import com.gam.wms.repository.ReturnItemRepository;
import com.gam.wms.service.ReturnManifestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/returns")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}) // Libera explicitamente as portas do frontend
public class ManifestController {

    private final ReturnManifestService returnManifestService;
    private final ReturnItemRepository returnItemRepository;

    public ManifestController(ReturnManifestService returnManifestService, ReturnItemRepository returnItemRepository) {
        this.returnManifestService = returnManifestService;
        this.returnItemRepository = returnItemRepository;
    }

    @PostMapping("/manifest")
    public ResponseEntity<String> saveReturnManifest(@RequestBody ReturnManifestRequestDTO request) {

        System.out.println("TESTANDO SE ESTA BATENDO AQUI ");
        try {
            ManifestEntity saved = returnManifestService.processReturnManifest(request);
            return ResponseEntity.ok("Romaneio " + saved.getManifestCode() + " e produtos gravados com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar romaneio: " + e.getMessage());
        }
    }

    @GetMapping("/manifest/{manifestCode}")
    public ResponseEntity<ManifestEntity> getManifestByCode(@PathVariable("manifestCode") String manifestCode) {

        System.out.println("TESTANDO SE ESTA BATENDO AQUI ");

        Optional<ManifestEntity> manifest = returnManifestService.findByManifestCode(manifestCode);
        return manifest.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PatchMapping("/items/{itemId}/scan")
    public ResponseEntity<ReturnItemEntity> scanItem(@PathVariable Long itemId) {
        Optional<ReturnItemEntity> itemOpt = returnItemRepository.findById(itemId);

        if (itemOpt.isPresent()) {
            ReturnItemEntity item = itemOpt.get();

            // Usando noteQty e checkedQty conforme a sua ReturnItemEntity real
            int currentChecked = item.getCheckedQty() != null ? item.getCheckedQty() : 0;
            int totalNote = item.getNoteQty() != null ? item.getNoteQty() : 0;

            if (currentChecked < totalNote) {
                item.setCheckedQty(currentChecked + 1);
                ReturnItemEntity updated = returnItemRepository.save(item);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.badRequest().build(); // Quantidade máxima da nota já atingida
            }
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/manifest/update")
    public ResponseEntity<ManifestEntity> saveOrUpdate(@RequestBody ManifestEntity payload) {
        System.out.println("Atualizando o manifesto com código: " + payload.getManifestCode());
        ManifestEntity updated = returnManifestService.saveOrUpdateManifest(payload);
        return ResponseEntity.ok(updated);
    }


}