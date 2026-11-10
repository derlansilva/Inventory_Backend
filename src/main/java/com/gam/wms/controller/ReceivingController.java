package com.gam.wms.controller;

import com.gam.wms.dto.*;
import com.gam.wms.model.*;
import com.gam.wms.repository.ManifestRepository;
import com.gam.wms.repository.ReceivingRepository;
import com.gam.wms.repository.ReceivingInvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/receiving")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "https://gammobile.netlify.app", "https://wmsgam.netlify.app"}, allowedHeaders = "*")
public class ReceivingController {

    private final ReceivingRepository receivingRepository;
    private final ReceivingInvoiceRepository invoiceRepository;
    private final ManifestRepository manifestRepository;

    public ReceivingController(ReceivingRepository receivingRepository,
                               ReceivingInvoiceRepository invoiceRepository,
                               ManifestRepository manifestRepository) {
        this.receivingRepository = receivingRepository;
        this.invoiceRepository = invoiceRepository;
        this.manifestRepository = manifestRepository;
    }

    // ==========================================
    // 1. CREATE RECEIVING
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createReceiving(@RequestBody CreateReceivingDto request) {
        System.out.println(">>> INICIANDO CREATE RECEIVING <<<");
        String finalManifestCode = request.manifestCode();
        System.out.println("Manifest Code recebido: " + finalManifestCode);

        // 1. Cria o Manifesto e OBRIGA o banco a salvar ele na hora
        ManifestEntity manifest = manifestRepository.findByManifestCode(finalManifestCode)
                .orElseGet(() -> {
                    System.out.println("Criando NOVO manifesto...");
                    ManifestEntity newManifest = new ManifestEntity();
                    newManifest.setManifestCode(finalManifestCode);
                    newManifest.setStatus(StatusEnum.OPEN);
                    newManifest.setCreatedAt(LocalDateTime.now());
                    return newManifest;
                });

        // Salva o manifesto e pega o ID gerado
        manifest = manifestRepository.saveAndFlush(manifest);

        System.out.println("Manifesto salvo com ID: " + manifest.getId());

        // 2. Cria o Romaneio
        String receivingCode = finalManifestCode;
        ReceivingEntity receiving = receivingRepository.findByReceivingCode(receivingCode)
                .orElseGet(() -> {
                    System.out.println("Criando NOVO romaneio...");
                    ReceivingEntity newReceiving = new ReceivingEntity();
                    newReceiving.setReceivingCode(receivingCode);
                    newReceiving.setStatus(StatusEnum.OPEN);
                    newReceiving.setCreatedAt(LocalDateTime.now());
                    return newReceiving;
                });

        // 3. Adiciona as NFs
        for (VolumeDto vol : request.volumes()) {
            System.out.println("Processando NF: " + vol.nfNumber());
            List<ReceivingInvoiceEntity> existingInvoices = invoiceRepository.findAllByInvoiceNumber(vol.nfNumber());

            ReceivingInvoiceEntity invoice;
            if (!existingInvoices.isEmpty()) {
                invoice = existingInvoices.get(0);
            } else {
                invoice = new ReceivingInvoiceEntity();
                invoice.setInvoiceNumber(vol.nfNumber());
                invoice.setVolumeQuantity(vol.quantidade());
                invoice.setXmlStatus(StatusEnum.WAITING_XML);
            }

            // AQUI: A nota recebe o romaneio
            invoice.setReceiving(receiving);
            receiving.getInvoices().add(invoice);
        }

        // Salva o romaneio (e as notas em cascata)
        receivingRepository.saveAndFlush(receiving);
        System.out.println(">>> FINALIZADO COM SUCESSO <<<");

        return ResponseEntity.ok(Map.of("message", "Manifest " + manifest.getManifestCode() + " created successfully!"));
    }


    // ==========================================
    // 2. GET MANIFESTS
    // ==========================================
    @GetMapping("/manifests")
    public ResponseEntity<List<Map<String, Object>>> getManifests() {
        List<ManifestEntity> manifests = manifestRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (ManifestEntity manifest : manifests) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", manifest.getId());
            map.put("manifestCode", manifest.getManifestCode());
            map.put("date", manifest.getCreatedAt() != null ? manifest.getCreatedAt().toString() : LocalDateTime.now().toString());

            List<ReceivingInvoiceEntity> allInvoices = new ArrayList<>();

            // AQUI ESTÁ A MÁGICA: Busca o Romaneio com o mesmo código do Manifesto
            String expectedReceivingCode = manifest.getManifestCode();
            Optional<ReceivingEntity> receivingOpt = receivingRepository.findByReceivingCode(expectedReceivingCode);

            if (receivingOpt.isPresent()) {
                allInvoices.addAll(receivingOpt.get().getInvoices());
            }

            int totalVolumes = allInvoices.stream().mapToInt(inv -> inv.getVolumeQuantity() != null ? inv.getVolumeQuantity() : 0).sum();
            map.put("volumes", totalVolumes);

            List<Map<String, Object>> invoicesMap = allInvoices.stream()
                    .map(inv -> {
                        Map<String, Object> invMap = new HashMap<>();
                        invMap.put("invoiceNumber", inv.getInvoiceNumber());
                        invMap.put("status", inv.getXmlStatus());
                        return invMap;
                    })
                    .collect(Collectors.toList());

            map.put("invoices", invoicesMap);
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
    // ==========================================
    // 3. UPLOAD XML
    // ==========================================
    @PostMapping("/upload-xml/{manifestCode}/{invoiceNumber}")
    @Transactional
    public ResponseEntity<?> uploadXml(@PathVariable String manifestCode, @PathVariable String invoiceNumber, @RequestBody UploadXmlDto xmlData) {
        try {
            List<ReceivingInvoiceEntity> existingInvoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);
            ReceivingInvoiceEntity invoice;

            if (!existingInvoices.isEmpty()) {
                boolean isAlreadyImported = existingInvoices.stream()
                        .anyMatch(inv -> StatusEnum.IMPORTED.equals(inv.getXmlStatus()) ||
                                StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()) ||
                                StatusEnum.CLOSED.equals(inv.getXmlStatus()));

                if (isAlreadyImported) {
                    return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " has already been imported!");
                }
                invoice = existingInvoices.get(0);
            } else {
                invoice = new ReceivingInvoiceEntity();
                invoice.setInvoiceNumber(invoiceNumber);
            }

            ManifestEntity manifest = manifestRepository.findByManifestCode(manifestCode)
                    .orElseThrow(() -> new RuntimeException("Manifest not found: " + manifestCode));

            String receivingCode = manifestCode;

            Optional<ReceivingEntity> receivingOpt = receivingRepository.findByReceivingCode(receivingCode);

            ReceivingEntity receiving;
            if (receivingOpt.isPresent()) {
                receiving = receivingOpt.get();
            } else {
                receiving = new ReceivingEntity();
                receiving.setReceivingCode(receivingCode);
                receiving.setManifest(manifest);
                receiving.setStatus(StatusEnum.OPEN);
                receiving.setCreatedAt(LocalDateTime.now());
                receiving = receivingRepository.saveAndFlush(receiving);
            }

            invoice.setReceiving(receiving);

            if (xmlData.items() != null) {
                for (ItemDto itemDto : xmlData.items()) {
                    ReceivingItemEntity item = new ReceivingItemEntity();
                    item.setReceivingInvoice(invoice);
                    item.setEan(itemDto.ean() != null && !itemDto.ean().isEmpty() ? itemDto.ean() : itemDto.code());
                    item.setMaterialCode(itemDto.code());
                    item.setDescription(itemDto.desc());
                    item.setNoteQuantity(itemDto.noteQty());
                    item.setBatch(itemDto.batch() != null ? itemDto.batch() : "S/L");
                    invoice.getItems().add(item);
                }
            }

            invoice.setXmlStatus(StatusEnum.IMPORTED);
            invoiceRepository.saveAndFlush(invoice);
            receivingRepository.save(receiving);

            return ResponseEntity.ok(Map.of("message", "XML for Invoice " + invoiceNumber + " imported into Manifest " + manifestCode));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. CHECK INVOICE STATUS
    // ==========================================
    @GetMapping("/check-invoice/{invoiceNumber}")
    public ResponseEntity<?> checkInvoiceStatus(@PathVariable String invoiceNumber) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        if (invoices.isEmpty()) {
            return ResponseEntity.ok().build(); // Pode seguir, NF nova no sistema
        }

        // Se a nota já existe, só bloqueia se ela já estiver em conferência ou finalizada.
        // Se ela estiver WAITING_XML, significa que foi bipada antes e não deu em nada, então deixa seguir.
        boolean isBlocked = invoices.stream().anyMatch(inv ->
                StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()) ||
                        StatusEnum.CLOSED.equals(inv.getXmlStatus()) ||
                        StatusEnum.IMPORTED.equals(inv.getXmlStatus())
        );

        if (isBlocked) {
            ReceivingInvoiceEntity invoice = invoices.get(0);
            return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " is already in progress or closed (Status: " + invoice.getXmlStatus() + ").");
        }

        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 5. GET INVOICE FOR SCANNING
    // ==========================================
    @GetMapping("/scan-invoice/{invoiceNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getInvoiceForScanning(@PathVariable String invoiceNumber) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        if (invoices.isEmpty()) {
            return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " was not found in the database.");
        }

        ReceivingInvoiceEntity invoice = invoices.get(0);

        List<Map<String, Object>> itemsDto = invoice.getItems().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("materialCode", item.getMaterialCode());
            map.put("ean", item.getEan());
            map.put("description", item.getDescription());
            map.put("noteQuantity", item.getNoteQuantity());
            map.put("scannedQuantity", item.getScannedQuantity() != null ? item.getScannedQuantity() : 0);
            map.put("batch", item.getBatch() != null ? item.getBatch() : "S/L");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "invoiceNumber", invoice.getInvoiceNumber(),
                "receivingCode", invoice.getReceiving() != null ? invoice.getReceiving().getReceivingCode() : "NO RECEIVING",
                "items", itemsDto
        ));
    }

    // ==========================================
    // 6. SCAN ITEM
    // ==========================================
    @PostMapping("/scan-item/{invoiceNumber}")
    @Transactional
    public ResponseEntity<?> scanItem(@PathVariable String invoiceNumber, @RequestBody ScanItemDto request) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        ReceivingInvoiceEntity invoice = invoices.stream()
                .filter(inv -> StatusEnum.IMPORTED.equals(inv.getXmlStatus()) || StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invoice not found or XML not imported."));

        if (StatusEnum.IMPORTED.equals(invoice.getXmlStatus())) {
            invoice.setXmlStatus(StatusEnum.IN_PROGRESS);
        }

        String ean = request.eanCode();

        ReceivingItemEntity targetItem = invoice.getItems().stream()
                .filter(item -> (item.getEan() != null && item.getEan().equals(ean)) || item.getMaterialCode().equals(ean))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product " + ean + " does not belong to this Invoice!"));

        int currentScanned = targetItem.getScannedQuantity() != null ? targetItem.getScannedQuantity() : 0;

        if (currentScanned >= targetItem.getNoteQuantity()) {
            return ResponseEntity.badRequest().body("Quantity exceeded! The invoice only has " + targetItem.getNoteQuantity() + " units.");
        }

        targetItem.setScannedQuantity(currentScanned + 1);
        receivingRepository.save(invoice.getReceiving());

        return ResponseEntity.ok(Map.of(
                "message", "Item scanned successfully!",
                "materialCode", targetItem.getMaterialCode(),
                "description", targetItem.getDescription(),
                "scanned", targetItem.getScannedQuantity(),
                "expected", targetItem.getNoteQuantity()
        ));
    }
}