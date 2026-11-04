package com.gam.wms.controller;

import com.gam.wms.dto.ReturnManifestRequestDTO;
import com.gam.wms.model.ManifestEntity;
import com.gam.wms.service.ReturnManifestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/returns")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}) // Libera explicitamente as portas do frontend
public class ManifestController {

    private final ReturnManifestService returnManifestService;


    public ManifestController(ReturnManifestService returnManifestService) {
        this.returnManifestService = returnManifestService;
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
}