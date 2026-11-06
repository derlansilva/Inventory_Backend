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
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ManifestController {

    private final ReturnManifestService returnManifestService;
    private final ReturnItemRepository returnItemRepository;

    public ManifestController(ReturnManifestService returnManifestService,
                              ReturnItemRepository returnItemRepository) {
        this.returnManifestService = returnManifestService;
        this.returnItemRepository = returnItemRepository;
    }

    // ------------------------------------------------------------
    // POST /api/v1/returns/manifest — cria romaneio (front envia)
    // ------------------------------------------------------------
    @PostMapping("/manifest")
    public ResponseEntity<String> saveReturnManifest(@RequestBody ReturnManifestRequestDTO request) {
        try {
            System.out.println("=== JSON RECEBIDO DO FRONT ===");
            // Adicione este print (precisa do ObjectMapper):

            System.out.println("=============================");

            ManifestEntity saved = returnManifestService.processReturnManifest(request);
            return ResponseEntity.ok("Romaneio " + saved.getManifestCode() + " criado com sucesso!");
        } catch (Exception e) {
            System.err.println("=== ERRO FATAL ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // GET /api/v1/returns/manifest/{code} — coletor consulta
    // ------------------------------------------------------------
    @GetMapping("/manifest/{manifestCode}")
    public ResponseEntity<ManifestEntity> getManifestByCode(@PathVariable("manifestCode") String manifestCode) {
        Optional<ManifestEntity> manifest = returnManifestService.findByManifestCode(manifestCode);
        return manifest.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------
    // PATCH /api/v1/returns/items/{id}/scan — bipagem (online)
    // Aceita body {"qty": N} opcional; default = 1
    // ------------------------------------------------------------
    public record ScanRequest(Integer qty) {}

    @PatchMapping("/items/{itemId}/scan")
    public ResponseEntity<?> scanItem(@PathVariable Long itemId,
                                      @RequestBody(required = false) ScanRequest body) {
        int increment = (body != null && body.qty() != null && body.qty() > 0) ? body.qty() : 1;

        Optional<ReturnItemEntity> itemOpt = returnItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ReturnItemEntity item = itemOpt.get();

        // Bloqueia se romaneio já foi finalizado
        String status = item.getInvoice().getManifest().getStatus();
        if ("CONCLUIDO".equals(status)) {
            return ResponseEntity.status(409).body("Romaneio já finalizado.");
        }

        int current = item.getCheckedQty() != null ? item.getCheckedQty() : 0;
        int total   = item.getNoteQty()    != null ? item.getNoteQty()    : 0;

        if (current + increment > total) {
            return ResponseEntity.badRequest()
                    .body("Excede a nota. Restam " + (total - current) + " unidade(s).");
        }

        item.setCheckedQty(current + increment);
        return ResponseEntity.ok(returnItemRepository.save(item));
    }

    // ------------------------------------------------------------
    // POST /api/v1/returns/manifest/update — sync offline do coletor
    // ------------------------------------------------------------
    @PostMapping("/manifest/update")
    public ResponseEntity<?> saveOrUpdate(@RequestBody ManifestEntity payload) {
        try {
            ManifestEntity updated = returnManifestService.saveOrUpdateManifest(payload);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------------------------------------------------
    // POST /api/v1/returns/manifest/{code}/close — NOVO
    // Valida tudo conferido e marca CONCLUIDO
    // ------------------------------------------------------------
    @PostMapping("/manifest/{manifestCode}/close")
    public ResponseEntity<?> closeManifest(@PathVariable String manifestCode) {
        try {
            ManifestEntity closed = returnManifestService.closeManifest(manifestCode);
            return ResponseEntity.ok(closed);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------------------------------------------------
    // POST /api/v1/returns/manifest/{code}/reopen — OPCIONAL
    // ------------------------------------------------------------
    @PostMapping("/manifest/{manifestCode}/reopen")
    public ResponseEntity<?> reopenManifest(@PathVariable String manifestCode) {
        try {
            ManifestEntity reopened = returnManifestService.reopenManifest(manifestCode);
            return ResponseEntity.ok(reopened);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}